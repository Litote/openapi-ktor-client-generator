package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import org.litote.openapi.ktor.client.generator.client.ClientFileContext
import org.litote.openapi.ktor.client.generator.client.ClientGenerationContext
import org.litote.openapi.ktor.client.generator.client.OperationBuilder
import org.litote.openapi.ktor.client.generator.client.ParameterExtractor
import org.litote.openapi.ktor.client.generator.client.ResponseBuilder
import org.litote.openapi.ktor.client.generator.shared.sanitizeToIdentifier
import org.litote.openapi.ktor.client.generator.shared.tagToCamelCase
import java.io.File

/**
 * Generates Ktor HTTP client classes from OpenAPI operations.
 *
 * This class is responsible for generating client classes that group operations by tag.
 * Each client class contains methods for each API operation.
 *
 * The generation is delegated to specialized builders:
 * - [ParameterExtractor] - Extracts and converts OpenAPI parameters
 * - [ResponseBuilder] - Builds response sealed classes and subclasses
 * - [OperationBuilder] - Builds individual operation methods
 */
public class ApiClientGenerator internal constructor(
    public val apiModel: ApiModel,
) {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    public val clientConfigurationClass: ClassName =
        ClassName(apiModel.configuration.clientPackage, "ClientConfiguration")

    public val clientConfigurationCompanionClass: ClassName =
        ClassName(apiModel.configuration.clientPackage, "ClientConfiguration", "Companion")

    // Delegate builders
    private val parameterExtractor = ParameterExtractor(apiModel)
    private val responseBuilder = ResponseBuilder(apiModel)
    private val operationBuilder =
        OperationBuilder(
            apiModel = apiModel,
            parameterExtractor = parameterExtractor,
            responseBuilder = responseBuilder,
            clientConfigurationClass = clientConfigurationClass,
        )

    /**
     * Builds a client class for the given context (tag name and operations).
     */
    internal fun buildClient(context: ClientGenerationContext): ClientFileContext {
        val clientName = clientNameForTag(context.name)

        val clientBuilder =
            TypeSpec
                .classBuilder(clientName)
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter(
                            ParameterSpec
                                .builder("configuration", clientConfigurationClass)
                                .defaultValue(
                                    "%M",
                                    MemberName(clientConfigurationCompanionClass, "defaultClientConfiguration"),
                                ).build(),
                        ).build(),
                ).addProperty(
                    PropertySpec
                        .builder("configuration", clientConfigurationClass)
                        .addModifiers(KModifier.PRIVATE)
                        .initializer("configuration")
                        .build(),
                )

        context.operations.forEach { operationInfo ->
            operationBuilder.analyseOperation(context, operationInfo)
        }

        context
            .operations
            .flatMap { operation ->
                operation.parameters
                    .filter { it.additionalTypeName != null }
                    .distinctBy { it.additionalTypeName }
                    .map { p -> p.additionalTypeName to p }
            }.groupBy { (typeName, _) ->
                typeName
            }.values
            .flatMap { values ->
                if (values.size == 1) {
                    listOf(values.first().second.additionalTypeSpec(context))
                } else {
                    values.map { (typeName, parameter) ->
                        val operation = parameter.operation
                        val newName = "${operation.methodName(context)}$typeName"
                        operation.parameters.forEachIndexed { index, parameter ->
                            val parameterAdditionalType = parameter.additionalTypeName
                            val parameterType = parameter.parameterType
                            if (parameterAdditionalType != null && typeName == parameterAdditionalType) {
                                val newClassName = ClassName("", newName)
                                operation.parameters[index] =
                                    parameter.copy(
                                        parameterType =
                                            if (parameterType is ClassName) {
                                                newClassName.run {
                                                    copy(
                                                        nullable = parameterType.isNullable,
                                                    )
                                                }
                                            } else {
                                                (parameterType as? ParameterizedTypeName)
                                                    ?.copy(
                                                        nullable = parameterType.isNullable,
                                                        typeArguments = listOf(newClassName),
                                                    )
                                                    ?: error("Unexpected parameter type $parameterType")
                                            },
                                        defaultValue =
                                            parameterDefaultLiteral(
                                                parameter.parameter.schema,
                                                newClassName,
                                            ),
                                    )
                            }
                        }

                        parameter
                            .additionalTypeSpec(context, newName)
                    }
                }
            }.filterNotNull()
            .distinct()
            .forEach { clientBuilder.addType(it) }

        context.operations.forEach { operationInfo ->
            operationBuilder.buildOperation(context, operationInfo, clientBuilder, clientName)
        }

        return ClientFileContext(context, clientBuilder.build())
    }

    /**
     * Writes the generated client class to a file.
     */
    internal fun writeFile(context: ClientFileContext) {
        val clientName = clientNameForTag(context.name)
        val fileSpec =
            FileSpec
                .builder(apiModel.configuration.clientPackage, clientName)
                .apply {
                    if (context.hasHeaders) {
                        addAliasedImport(headerMember, ALIAS_HEADER)
                    }
                    if (context.hasPathComponents) {
                        addImport("io.ktor.http", "encodeURLPathPart")
                    }
                }.addType(context.clientClass)
                .build()

        val basePath = File(apiModel.outputDirectory).resolve("src/main/kotlin")
        logger.debug { "Writing $clientName to $basePath" }
        fileSpec.writeTo(basePath)
    }

    private fun clientNameForTag(tag: String): String {
        val tagName = tag.sanitizeToIdentifier().tagToCamelCase()
        val clientBaseName = tagName.removeSuffix("Controller")
        return "${clientBaseName}Client"
    }
}
