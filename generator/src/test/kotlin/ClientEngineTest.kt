package org.litote.openapi.ktor.client.generator

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ClientEngineTest {
    private val testSpec = "src/test/resources/openapi.json"

    private fun generateClientConfiguration(engine: String? = null): String {
        val outputDir = Files.createTempDirectory("client-engine-test").toFile()
        try {
            val configuration = ApiGeneratorConfiguration(openApiFile = testSpec, outputDirectory = outputDir.absolutePath)
            val result = generate(if (engine == null) configuration else configuration.copy(engine = engine))
            assertTrue(result.isSuccess, "Generation should succeed")
            return outputDir
                .walkTopDown()
                .first { it.name == "ClientConfiguration.kt" }
                .readText()
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `GIVEN no engine WHEN generating THEN ClientConfiguration uses CIO`() {
        val content = generateClientConfiguration()

        assertTrue(content.contains("import io.ktor.client.engine.cio.CIO"), content)
        assertTrue(content.contains("public val engine: HttpClientEngineFactory<*> = CIO,"), content)
        assertTrue(content.contains("public val client: HttpClient = HttpClient(engine) { httpClientConfig() },"), content)
    }

    @Test
    fun `GIVEN a qualified engine WHEN generating THEN ClientConfiguration uses this engine`() {
        val content = generateClientConfiguration("io.ktor.client.engine.okhttp.OkHttp")

        assertTrue(content.contains("import io.ktor.client.engine.okhttp.OkHttp"), content)
        assertTrue(content.contains("public val engine: HttpClientEngineFactory<*> = OkHttp,"), content)
        assertFalse(content.contains("CIO"), content)
    }

    @Test
    fun `GIVEN the platform engine WHEN generating THEN ClientConfiguration lets Ktor select the engine`() {
        val content = generateClientConfiguration(ApiGeneratorConfiguration.PLATFORM_ENGINE)

        assertTrue(content.contains("public val engine: HttpClientEngineFactory<*>? = null,"), content)
        assertTrue(
            content.contains("engine?.let { HttpClient(it) { httpClientConfig() } } ?: HttpClient { httpClientConfig() }"),
            content,
        )
        assertFalse(content.contains("CIO"), content)
    }

    @Test
    fun `GIVEN an engine without package WHEN generating THEN generation fails`() {
        val outputDir = Files.createTempDirectory("client-engine-invalid-test").toFile()
        try {
            val result =
                generate(ApiGeneratorConfiguration(openApiFile = testSpec, outputDirectory = outputDir.absolutePath, engine = "CIO"))

            assertIs<GenerationResult.Failure>(result)
        } finally {
            outputDir.deleteRecursively()
        }
    }
}
