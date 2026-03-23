package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

class BasicAuthModuleIntegrationTest {
    private val testSpec = "src/test/resources/openapi.json"

    @Test
    fun `GIVEN bearer auth module WHEN generating THEN ClientConfiguration has accessToken and httpClientAuthorization parameters`() {
        val outputDir = Files.createTempDirectory("basic-auth-module-test").toFile()
        try {
            val module =
                object : ApiGeneratorModule {
                    override fun processConfiguration(generator: ApiConfigurationGeneratorConfig) {
                        generator.additionalStringParameters.add("accessToken")
                        generator.httpClientAuthorizationDefaultValue =
                            """{ accessToken?.let { token -> defaultRequest { header("Authorization", "Bearer " + token) } } }"""
                    }
                }
            val config =
                ApiGeneratorConfiguration(
                    openApiFile = testSpec,
                    outputDirectory = outputDir.absolutePath,
                    modules = listOf(module),
                )

            val result = generate(config)

            assertTrue(result.isSuccess, "Generation should succeed")
            val clientConfigFile =
                outputDir
                    .walkTopDown()
                    .first { it.name == "ClientConfiguration.kt" }
            val content = clientConfigFile.readText()
            assertTrue(content.contains("accessToken: String?"), "ClientConfiguration should have accessToken parameter")
            assertTrue(content.contains("httpClientAuthorization:"), "ClientConfiguration should have httpClientAuthorization parameter")
            assertTrue(content.contains("\"Bearer \""), "httpClientAuthorization default should contain Bearer token logic")
            assertTrue(
                content.contains("defaultHttpClientConfig(baseUrl, json, logLevel, httpClientAuthorization)"),
                "httpClientConfig default should reference logLevel and httpClientAuthorization",
            )
            assertTrue(
                content.contains("httpClientAuthorization()"),
                "defaultHttpClientConfig body should call httpClientAuthorization()",
            )
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `GIVEN no module WHEN generating THEN ClientConfiguration has httpClientAuthorization with empty default`() {
        val outputDir = Files.createTempDirectory("no-auth-module-test").toFile()
        try {
            val config =
                ApiGeneratorConfiguration(
                    openApiFile = testSpec,
                    outputDirectory = outputDir.absolutePath,
                )

            val result = generate(config)

            assertTrue(result.isSuccess, "Generation should succeed")
            val clientConfigFile =
                outputDir
                    .walkTopDown()
                    .first { it.name == "ClientConfiguration.kt" }
            val content = clientConfigFile.readText()
            assertTrue(content.contains("httpClientAuthorization:"), "ClientConfiguration should have httpClientAuthorization parameter")
            assertTrue(
                content.contains("httpClientAuthorization: HttpClientConfig<*>.() -> Unit = {}"),
                "httpClientAuthorization default should be empty lambda",
            )
            assertTrue(
                content.contains("httpClientAuthorization()"),
                "defaultHttpClientConfig body should call httpClientAuthorization()",
            )
        } finally {
            outputDir.deleteRecursively()
        }
    }
}
