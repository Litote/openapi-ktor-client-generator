package org.litote.openapi.ktor.client.generator

import io.github.oshai.kotlinlogging.KotlinLogging
import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.adapter.renderer.ApiClientConfigurationGenerator
import org.litote.openapi.ktor.client.generator.adapter.renderer.ApiClientGenerator
import org.litote.openapi.ktor.client.generator.adapter.renderer.ApiModelGenerator
import org.litote.openapi.ktor.client.generator.application.GenerateCodeService
import org.litote.openapi.ktor.client.generator.application.GenerationSpecPartitioner
import org.litote.openapi.ktor.client.generator.port.ClientRenderer
import org.litote.openapi.ktor.client.generator.port.ConfigurationRenderer
import org.litote.openapi.ktor.client.generator.port.ModelRenderer

/**
 * Main entry point for the API client generator.
 *
 * @param args Command-line arguments:
 *             - args[0]: path to the OpenAPI specification file
 *             - args[1]: output directory
 */
public fun main(vararg args: String) {
    generate(
        ApiGeneratorConfiguration(
            args.getOrNull(0)
                ?: error("please provide the path to the OpenAPI specification file as the first argument"),
            args.getOrNull(1) ?: error("please provide the path to the output directory as the second argument"),
        ),
    )
}

/**
 * Result of the API generation process.
 */
public sealed class GenerationResult {
    /**
     * Successful generation.
     * @param clientsGenerated Number of client files generated
     * @param modelsGenerated Number of model files generated
     */
    public data class Success(
        val clientsGenerated: Int,
        val modelsGenerated: Int,
    ) : GenerationResult()

    /**
     * Failed generation.
     * @param error The exception that caused the failure
     * @param message A descriptive error message
     */
    public data class Failure(
        val error: Throwable,
        val message: String,
    ) : GenerationResult()

    /**
     * Returns true if the generation was successful.
     */
    public val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if the generation failed.
     */
    public val isFailure: Boolean get() = this is Failure

    /**
     * Returns the success result or null if failed.
     */
    public fun getOrNull(): Success? = this as? Success

    /**
     * Returns the success result or throws the error if failed.
     */
    public fun getOrThrow(): Success =
        when (this) {
            is Success -> this
            is Failure -> throw error
        }
}

/**
 * Parses an OpenAPI specification and returns the names of all generated clients.
 *
 * @param openApiFilePath Path to the OpenAPI specification file
 * @return List of client names derived from the spec tags
 */
public fun parseClientNames(openApiFilePath: String): List<String> {
    val configuration = ApiGeneratorConfiguration(openApiFile = openApiFilePath)
    val spec = OpenApiSpecificationParser().parse(configuration, configuration.operationFilter)
    return spec.clients.map { it.name }
}

/**
 * Executes the generation of API client and data models based on an OpenAPI specification.
 *
 * Loads the OpenAPI file, generates the HTTP client using Ktor, and creates data model classes.
 *
 * @param configuration Custom generator configuration settings
 * @return [GenerationResult] indicating success with statistics or failure with error details
 */
public fun generate(configuration: ApiGeneratorConfiguration): GenerationResult =
    try {
        logger.debug { "Generating API for $configuration" }
        val parser = OpenApiSpecificationParser()
        val spec = parser.parse(configuration, configuration.operationFilter)

        val clientGen =
            ApiClientGenerator(configuration)
                .apply { configuration.modules.forEach { it.process(this) } }
        val clientRenderer =
            ClientRenderer { clientSpec ->
                val context = clientGen.buildClient(clientSpec)
                clientGen.writeFile(context)
            }

        val modelGen =
            ApiModelGenerator(configuration.resolvedModelPackage, configuration.outputDirectory)
                .apply { configuration.modules.forEach { it.process(this) } }
        val modelRenderer =
            ModelRenderer { modelSpec ->
                val typeSpec = modelGen.buildModel(modelSpec)
                modelGen.writeFile(modelSpec.name, typeSpec)
            }

        val (activeSpec, activeConfigRenderer, activeClientRenderer) =
            if (!configuration.splitByClient) {
                val configRenderer =
                    ApiClientConfigurationGenerator(spec.clientConfiguration, configuration)
                        .apply { configuration.modules.forEach { it.process(this) } }
                Triple(spec, configRenderer as ConfigurationRenderer, clientRenderer)
            } else {
                val partitioned = GenerationSpecPartitioner().partition(spec)
                val targetName = configuration.targetClientName
                if (targetName == null) {
                    val configRenderer =
                        ApiClientConfigurationGenerator(spec.clientConfiguration, configuration)
                            .apply { configuration.modules.forEach { it.process(this) } }
                    val noopClientRenderer = ClientRenderer { }
                    Triple(partitioned.shared, configRenderer as ConfigurationRenderer, noopClientRenderer)
                } else {
                    val perClientSpec =
                        partitioned.perClient.find { it.clientName == targetName }
                            ?: return GenerationResult.Failure(
                                IllegalArgumentException("Client '$targetName' not found"),
                                "Client '$targetName' not found in spec ${configuration.openApiFile}",
                            )
                    val noopConfigRenderer =
                        object : ConfigurationRenderer {
                            override fun render() = Unit
                        }
                    Triple(perClientSpec.spec, noopConfigRenderer, clientRenderer)
                }
            }

        val result = GenerateCodeService(activeConfigRenderer, activeClientRenderer, modelRenderer).generate(activeSpec)
        if (result is GenerationResult.Success) {
            logger.info { "Generation completed: ${result.clientsGenerated} clients, ${result.modelsGenerated} models" }
        }
        result
    } catch (e: Throwable) {
        logger.error(e) { "Error while generating API for $configuration" }
        GenerationResult.Failure(e, "Failed to generate API for ${configuration.openApiFile}: ${e.message}")
    }

private val logger = KotlinLogging.logger {}
