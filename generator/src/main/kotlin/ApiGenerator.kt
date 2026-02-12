package org.litote.openapi.ktor.client.generator

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
            args.getOrNull(1) ?: error("please provide the path to the output directory as the second argument")
        )
    )
}

/**
 * Executes the generation of API client and data models based on an OpenAPI specification.
 *
 * Loads the OpenAPI file, generates the HTTP client using Ktor, and creates data model classes.
 * Any exceptions are caught and printed to the console.
 *
 * @param configuration Custom generator configuration settings
 */
public fun generate(
    configuration: ApiGeneratorConfiguration
) {
    try {
        logger.debug { "Generating API for $configuration" }

        val fileContent = ApiModel.parseOpenApiFile(configuration)
        val clientGenerator = ClientGenerator(fileContent)
        val modelGenerator = ModelGenerator(fileContent)
        clientGenerator.generate()
        modelGenerator.generate()

    } catch (e: Throwable) {
        logger.error(e) { "Error while generating API for $configuration" }
    }
}

private val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger {}
