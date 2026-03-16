package org.litote.openapi.ktor.client.generator

import kotlin.test.Test
import kotlin.test.assertTrue

class GenerationTest {
    @Test
    fun `GIVEN simple openapi json file WHEN generating client THEN generation succeeds with expected counts`() {
        val config =
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/openapi.json",
                outputDirectory = "build/openapi",
            )

        // When
        val result = generate(config)

        // Then
        assertTrue(result.isSuccess, "Generation should succeed")
        val success = result.getOrThrow()
        assertTrue(success.clientsGenerated > 0, "Should generate at least one client")
        assertTrue(success.modelsGenerated > 0, "Should generate at least one model")
    }

    @Test
    fun `GIVEN non-existent openapi file WHEN generating client THEN generation fails`() {
        val config =
            ApiGeneratorConfiguration(
                openApiFile = "non-existent-file.json",
                outputDirectory = "build/openapi",
            )

        // When
        val result = generate(config)

        // Then
        assertTrue(result.isFailure, "Generation should fail for non-existent file")
        val failure = result as GenerationResult.Failure
        assertTrue(failure.message.contains("non-existent-file.json"), "Error message should contain filename")
    }
}
