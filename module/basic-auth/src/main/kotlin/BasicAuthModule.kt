package org.litote.openapi.ktor.client.generator.module.basicauth

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig

internal class BasicAuthModule : ApiGeneratorModule {
    override fun processConfiguration(generator: ApiConfigurationGeneratorConfig) {
        generator.additionalStringParameters.add("accessToken")
        generator.httpClientAuthorizationDefaultValue =
            """{ accessToken?.let { token -> defaultRequest { header("Authorization", "Bearer " + token) } } }"""
        generator.additionalImports.add("io.ktor.client.request" to "header")
    }
}
