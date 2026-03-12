package org.litote.openapi.ktor.client.generator

import io.github.oshai.kotlinlogging.KotlinLogging
import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.adapter.renderer.ApiClientConfigurationGenerator
import org.litote.openapi.ktor.client.generator.adapter.renderer.ApiClientGenerator
import org.litote.openapi.ktor.client.generator.adapter.renderer.ApiModelGenerator
import org.litote.openapi.ktor.client.generator.application.GenerateCodeService
import org.litote.openapi.ktor.client.generator.port.ClientRenderer
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

        val configRenderer =
            ApiClientConfigurationGenerator(spec.clientConfiguration, configuration)
                .apply { configuration.modules.forEach { it.process(this) } }

        val clientGen =
            ApiClientGenerator(configuration)
                .apply { configuration.modules.forEach { it.process(this) } }
        val clientRenderer =
            ClientRenderer { clientSpec ->
                val context = clientGen.buildClient(clientSpec)
                clientGen.writeFile(context)
            }

        val modelGen =
            ApiModelGenerator(configuration.modelPackage, configuration.outputDirectory)
                .apply { configuration.modules.forEach { it.process(this) } }
        val modelRenderer =
            ModelRenderer { modelSpec ->
                val typeSpec = modelGen.buildModel(modelSpec)
                modelGen.writeFile(modelSpec.name, typeSpec)
            }

        val result = GenerateCodeService(configRenderer, clientRenderer, modelRenderer).generate(spec)
        if (result is GenerationResult.Success) {
            logger.info { "Generation completed: ${result.clientsGenerated} clients, ${result.modelsGenerated} models" }
        }
        result
    } catch (e: Throwable) {
        logger.error(e) { "Error while generating API for $configuration" }
        GenerationResult.Failure(e, "Failed to generate API for ${configuration.openApiFile}: ${e.message}")
    }

private val logger = KotlinLogging.logger {}
