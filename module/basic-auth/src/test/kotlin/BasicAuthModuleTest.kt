package org.litote.openapi.ktor.client.generator.module.basicauth

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig
import kotlin.test.Test

class BasicAuthModuleTest {
    @Test
    fun `GIVEN BasicAuthModule WHEN processConfiguration THEN adds accessToken parameter and an httpClientAuthorization statement`() {
        val addedParams = mutableListOf<String>()
        val addedImports = mutableListOf<Pair<String, String>>()
        val addedStatements = mutableListOf<String>()
        val config =
            mockk<ApiConfigurationGeneratorConfig>(relaxed = true) {
                every { additionalStringParameters } returns addedParams
                every { additionalImports } returns addedImports
                every { httpClientAuthorizationStatements } returns addedStatements
            }
        val module = BasicAuthModule()

        module.processConfiguration(config)

        assert(addedParams.contains("accessToken")) { "accessToken should be added to additionalStringParameters" }
        assert(
            addedStatements ==
                listOf("""accessToken?.let { token -> defaultRequest { header("Authorization", "Bearer " + token) } }"""),
        ) { "the bearer statement should be added to httpClientAuthorizationStatements" }
        verify(exactly = 0) { config.httpClientAuthorizationDefaultValue = any() }
        assert(addedImports.contains("io.ktor.client.request" to "header")) {
            "additionalImports should contain io.ktor.client.request.header"
        }
    }
}
