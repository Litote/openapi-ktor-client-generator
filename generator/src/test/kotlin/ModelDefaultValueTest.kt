package org.litote.openapi.ktor.client.generator

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

class ModelDefaultValueTest {
    private val config =
        ApiGeneratorConfiguration(
            openApiFile = "src/test/resources/non-object-schemas.json",
            outputDirectory = "build/non-object-schema-test",
            basePackage = "nonobject.test",
        )

    private fun generateRouteModel(): String {
        val outputDir = Files.createTempDirectory("non-object-schemas-test").toFile()
        try {
            generate(config.copy(outputDirectory = outputDir.absolutePath))
            return outputDir
                .walkTopDown()
                .first { it.isFile && it.name == "Route.kt" }
                .readText()
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `GIVEN integer default on number property WHEN generating THEN default is a floating point literal`() {
        val content = generateRouteModel()

        assertTrue(content.contains("maxMatchingDistance: Double? = 25.0"), content)
        assertTrue(content.contains("ratio: Float? = 2.0F"), content)
        assertTrue(content.contains("count: Long? = 3"), content)
    }
}
