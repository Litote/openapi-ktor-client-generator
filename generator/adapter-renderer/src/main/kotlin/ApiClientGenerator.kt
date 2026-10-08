package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.litote.openapi.ktor.client.generator.ApiGeneratorConfiguration
import org.litote.openapi.ktor.client.generator.adapter.writer.KotlinPoetFileWriter
import org.litote.openapi.ktor.client.generator.domain.ClientSpec
import org.litote.openapi.ktor.client.generator.port.ApiClientGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ApiFileSystemWriter
import org.litote.openapi.ktor.client.generator.port.StringFormatType
import org.litote.openapi.ktor.client.generator.stringFormatTypes

/**
 * Generates Ktor HTTP client classes from OpenAPI operations.
 *
 * For each client (operations grouped by tag), it generates an interface declaring the operations and
 * the response types, a `Default*` implementation calling Ktor, and a factory function named like the interface.
 */
public class ApiClientGenerator public constructor(
    public val configuration: ApiGeneratorConfiguration,
    private val fileSystemWriter: ApiFileSystemWriter = KotlinPoetFileWriter(),
) : ApiClientGeneratorConfig {
    private val stringFormatTypes: Map<String, StringFormatType> = configuration.modules.stringFormatTypes()

    public val clientConfigurationClass: ClassName =
        ClassName(configuration.configPackage, "ClientConfiguration")

    public val clientConfigurationCompanionClass: ClassName =
        ClassName(configuration.configPackage, "ClientConfiguration", "Companion")

    private companion object {
        const val DEFAULT_IMPLEMENTATION_PREFIX = "Default"
    }

    /**
     * Builds a client interface, its default implementation and its factory function for the given spec (name and operations).
     */
    public fun buildClient(spec: ClientSpec): ClientFileContext {
        val transformedSpec = configuration.modules.fold(spec) { acc, m -> m.transformClientSpec(acc) }
        val clientName = transformedSpec.name

        val interfaceClassName = ClassName(configuration.clientPackage, clientName)
        val configurationParameter =
            ParameterSpec
                .builder("configuration", clientConfigurationClass)
                .defaultValue(
                    "%M",
                    MemberName(clientConfigurationCompanionClass, "defaultClientConfiguration"),
                ).build()

        val interfaceBuilder = TypeSpec.interfaceBuilder(clientName)
        val implementationClassName = ClassName(configuration.clientPackage, "$DEFAULT_IMPLEMENTATION_PREFIX$clientName")
        val clientBuilder =
            TypeSpec
                .classBuilder(implementationClassName)
                .addSuperinterface(interfaceClassName)
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter(configurationParameter)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("configuration", clientConfigurationClass)
                        .addModifiers(KModifier.PRIVATE)
                        .initializer("configuration")
                        .build(),
                )
        val factory =
            FunSpec
                .builder(clientName)
                .addParameter(configurationParameter)
                .returns(interfaceClassName)
                .addStatement("return %T(configuration)", implementationClassName)
                .build()

        val context =
            ClientGenerationContext(
                name = clientName,
                operations = transformedSpec.operations,
            )

        val modelGenerator =
            ApiModelGenerator(
                configuration.generationModelPackage,
                configuration.outputDirectory,
                fileSystemWriter = fileSystemWriter,
                modelPackageOverrides = configuration.modelPackageOverrides,
                fallbackModelPackage = configuration.resolvedModelPackage,
                modules = configuration.modules,
                clientPackage = configuration.clientPackage,
            )
        val operationBuilder =
            OperationBuilder(
                modelGenerator = modelGenerator,
                responseBuilder = ResponseBuilder(stringFormatTypes),
                clientConfigurationClass = clientConfigurationClass,
                modelPackage = configuration.resolvedModelPackage,
                clientPackage = configuration.clientPackage,
                modelPackageOverrides = configuration.modelPackageOverrides,
                stringFormatTypes = stringFormatTypes,
            )

        // Add all additional inline models first (across all operations), then build operations.
        // Group by base name (pre-rename) to preserve the old ordering where all types with the
        // same base name (e.g. "ExcludeTypes") were grouped together.
        val allParamsWithAdditionalModels =
            transformedSpec.operations.flatMap { op ->
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
            .forEach { interfaceBuilder.addType(it) }

        // Then build all operations
        transformedSpec.operations.forEach { op ->
            operationBuilder.buildOperation(context, op, interfaceBuilder, clientBuilder, clientName)
        }
        buildParameterSerializationHelpers(context.parameterHelpers).forEach { clientBuilder.addFunction(it) }

        return ClientFileContext(context, interfaceBuilder.build(), factory, clientBuilder.build())
    }

    /**
     * Writes the generated client class to a file.
     */
    public fun writeFile(context: ClientFileContext) {
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
                }.addType(context.clientInterface)
                .addFunction(context.clientFactory)
                .addType(context.clientClass)
                .build()

        fileSystemWriter.write(fileSpec.toGeneratedFile(), configuration.outputDirectory)
    }
}
