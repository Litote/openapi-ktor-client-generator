package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.oshai.kotlinlogging.KotlinLogging
import org.litote.openapi.ktor.client.generator.ApiGeneratorConfiguration
import org.litote.openapi.ktor.client.generator.adapter.writer.KotlinPoetFileWriter
import org.litote.openapi.ktor.client.generator.domain.ClientSpec
import org.litote.openapi.ktor.client.generator.port.ClientGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.FileSystemWriter

/**
 * Generates Ktor HTTP client classes from OpenAPI operations.
 *
 * This class is responsible for generating client classes that group operations by tag.
 * Each client class contains methods for each API operation.
 */
public class ApiClientGenerator internal constructor(
    public val configuration: ApiGeneratorConfiguration,
    private val fileSystemWriter: FileSystemWriter = KotlinPoetFileWriter(),
) : ClientGeneratorConfig {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    public val clientConfigurationClass: ClassName =
        ClassName(configuration.clientPackage, "ClientConfiguration")

    public val clientConfigurationCompanionClass: ClassName =
        ClassName(configuration.clientPackage, "ClientConfiguration", "Companion")

    /**
     * Builds a client class for the given spec (name and operations).
     */
    internal fun buildClient(spec: ClientSpec): ClientFileContext {
        val clientName = spec.name

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

        val context =
            ClientGenerationContext(
                name = clientName,
                operations = spec.operations,
            )

        val modelGenerator = ApiModelGenerator(configuration.modelPackage, configuration.outputDirectory)
        val operationBuilder =
            OperationBuilder(
                modelGenerator = modelGenerator,
                responseBuilder = ResponseBuilder(),
                clientConfigurationClass = clientConfigurationClass,
                modelPackage = configuration.modelPackage,
            )

        // Add all additional inline models first (across all operations), then build operations.
        // Group by base name (pre-rename) to preserve the old ordering where all types with the
        // same base name (e.g. "ExcludeTypes") were grouped together.
        val allParamsWithAdditionalModels =
            spec.operations.flatMap { op ->
                op.parameters.mapNotNull { p ->
                    p.additionalModel?.let { Triple(p.additionalModelBaseName ?: it.name, it.name, it) }
                }
            }
        // Stable sort: group by base name (first occurrence order), then by model name within group
        val seenBaseNames = LinkedHashMap<String, MutableList<Pair<String, org.litote.openapi.ktor.client.generator.domain.ModelSpec>>>()
        allParamsWithAdditionalModels.forEach { (baseName, modelName, model) ->
            seenBaseNames.getOrPut(baseName) { mutableListOf() }.also { list ->
                if (list.none { it.first == modelName }) list.add(modelName to model)
            }
        }
        seenBaseNames.values
            .flatten()
            .mapNotNull { (_, model) -> modelGenerator.buildModel(model) }
            .forEach { clientBuilder.addType(it) }

        // Then build all operations
        spec.operations.forEach { op ->
            operationBuilder.buildOperation(context, op, clientBuilder, clientName)
        }

        return ClientFileContext(context, clientBuilder.build())
    }

    /**
     * Writes the generated client class to a file.
     */
    internal fun writeFile(context: ClientFileContext) {
        val clientName = context.name
        val fileSpec =
            FileSpec
                .builder(configuration.clientPackage, clientName)
                .apply {
                    if (context.hasHeaders) {
                        addAliasedImport(headerMember, ALIAS_HEADER)
                    }
                    if (context.hasPathComponents) {
                        addImport("io.ktor.http", "encodeURLPathPart")
                    }
                    if (context.hasSseOperations) {
                        addImport("io.ktor.client.plugins.sse", "sse")
                        addImport("io.ktor.client.plugins.sse", "ClientSSESession")
                    }
                }.addType(context.clientClass)
                .build()

        fileSystemWriter.write(fileSpec.toGeneratedFile(), configuration.outputDirectory)
    }
}
