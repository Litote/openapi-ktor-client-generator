package org.litote.openapi.ktor.client.generator.module.basicauth

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig
import kotlin.test.Test

class BasicAuthModuleTest {
    @Test
    fun `GIVEN BasicAuthModule WHEN processConfiguration THEN adds accessToken parameter and sets httpClientAuthorizationDefaultValue`() {
        val addedParams = mutableListOf<String>()
        val config =
            mockk<ApiConfigurationGeneratorConfig>(relaxed = true) {
                every { additionalStringParameters } returns addedParams
            }
        val module = BasicAuthModule()

        module.processConfiguration(config)

        assert(addedParams.contains("accessToken")) { "accessToken should be added to additionalStringParameters" }
        verify {
            config.httpClientAuthorizationDefaultValue =
                """{ accessToken?.let { token -> defaultRequest { header("Authorization", "Bearer " + token) } } }"""
        }
    }
}
