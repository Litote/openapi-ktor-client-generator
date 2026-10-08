package org.litote.openapi.ktor.client.generator

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig
import simple.api.client.Client
import simple.api.client.ClientConfiguration
import simple.api.model.TestRequest
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserAgentTest {
    private val testSpec = "src/test/resources/openapi.json"

    private fun generateClientConfiguration(configuration: (String) -> ApiGeneratorConfiguration): String {
        val outputDir = Files.createTempDirectory("user-agent-test").toFile()
        try {
            val result = generate(configuration(outputDir.absolutePath))
            assertTrue(result.isSuccess, "Generation should succeed")
            return outputDir
                .walkTopDown()
                .first { it.name == "ClientConfiguration.kt" }
                .readText()
        } finally {
            outputDir.deleteRecursively()
        }
    }

    /** Calls the generated client and returns the `User-Agent` header seen by the plugins installed after the default ones. */
    private fun capturedUserAgent(configuration: (capture: HttpClientConfig<*>.() -> Unit) -> ClientConfiguration): String? {
        var userAgent: String? = null
        val capturingPlugin =
            createClientPlugin("CapturingPlugin") {
                onRequest { request, _ ->
                    userAgent = request.headers[HttpHeaders.UserAgent]
                    throw CancellationException("request captured")
                }
            }
        val client = Client(configuration { install(capturingPlugin) })
        val latch = CountDownLatch(1)
        suspend { client.postTestWithTestId(TestRequest("name"), "id") }
            .startCoroutine(Continuation(EmptyCoroutineContext) { latch.countDown() })
        assertTrue(latch.await(10, TimeUnit.SECONDS), "Call should complete")
        return userAgent
    }

    @Test
    fun `GIVEN no user agent WHEN generating THEN ClientConfiguration has a nullable userAgent parameter defaulting to null`() {
        val content = generateClientConfiguration { ApiGeneratorConfiguration(openApiFile = testSpec, outputDirectory = it) }

        assertTrue(content.contains("public val userAgent: String? = null,"), content)
        assertTrue(content.contains("defaultHttpClientConfig(baseUrl, json, logLevel, userAgent, httpClientAuthorization)"), content)
        assertTrue(content.contains("install(UserAgent)"), content)
    }

    @Test
    fun `GIVEN a user agent WHEN generating THEN it is the default value of the userAgent parameter`() {
        val content =
            generateClientConfiguration {
                ApiGeneratorConfiguration(
                    openApiFile = testSpec,
                    outputDirectory = it,
                    userAgent = "MyApp/1.0 (+https://example.com/\"contact\")",
                )
            }

        assertTrue(
            content.contains("public val userAgent: String? = \"MyApp/1.0 (+https://example.com/\\\"contact\\\")\","),
            content,
        )
    }

    @Test
    fun `GIVEN a user agent WHEN calling a generated client THEN the User-Agent header is sent`() {
        val userAgent =
            capturedUserAgent { capture ->
                ClientConfiguration(userAgent = "MyApp/1.0", httpClientAuthorization = capture)
            }

        assertEquals("MyApp/1.0", userAgent)
    }

    @Test
    fun `GIVEN no user agent WHEN calling a generated client THEN no User-Agent header is added by the configuration`() {
        val userAgent = capturedUserAgent { capture -> ClientConfiguration(httpClientAuthorization = capture) }

        assertNull(userAgent)
    }

    @Test
    fun `GIVEN two modules adding authorization statements WHEN generating THEN both statements are applied`() {
        val tokenModule =
            object : ApiGeneratorModule {
                override fun processConfiguration(generator: ApiConfigurationGeneratorConfig) {
                    generator.httpClientAuthorizationStatements.add("""defaultRequest { header("X-Token", "token") }""")
                }
            }
        val traceModule =
            object : ApiGeneratorModule {
                override fun processConfiguration(generator: ApiConfigurationGeneratorConfig) {
                    generator.httpClientAuthorizationStatements.add("""defaultRequest { header("X-Trace", "trace") }""")
                }
            }

        val content =
            generateClientConfiguration {
                ApiGeneratorConfiguration(openApiFile = testSpec, outputDirectory = it, modules = listOf(tokenModule, traceModule))
            }

        val tokenIndex = content.indexOf("""defaultRequest { header("X-Token", "token") }""")
        val traceIndex = content.indexOf("""defaultRequest { header("X-Trace", "trace") }""")
        assertTrue(tokenIndex >= 0 && traceIndex > tokenIndex, content)
    }

    @Test
    fun `GIVEN a default value and statements WHEN generating THEN the default value is applied before the statements`() {
        val legacyModule =
            object : ApiGeneratorModule {
                override fun processConfiguration(generator: ApiConfigurationGeneratorConfig) {
                    generator.httpClientAuthorizationDefaultValue = """{ defaultRequest { header("X-Legacy", "legacy") } }"""
                }
            }
        val statementModule =
            object : ApiGeneratorModule {
                override fun processConfiguration(generator: ApiConfigurationGeneratorConfig) {
                    generator.httpClientAuthorizationStatements.add("""defaultRequest { header("X-Trace", "trace") }""")
                }
            }

        val content =
            generateClientConfiguration {
                ApiGeneratorConfiguration(
                    openApiFile = testSpec,
                    outputDirectory = it,
                    modules = listOf(legacyModule, statementModule),
                )
            }

        val legacyIndex = content.indexOf("""{ defaultRequest { header("X-Legacy", "legacy") } }""")
        val legacyCallIndex = content.indexOf("defaultAuthorization()")
        val traceIndex = content.indexOf("""defaultRequest { header("X-Trace", "trace") }""")
        assertTrue(legacyIndex >= 0 && legacyCallIndex > legacyIndex && traceIndex > legacyCallIndex, content)
    }
}
