package org.litote.openapi.ktor.client.generator.module.unknown.enum.value

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.ConfigurationGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ModelGeneratorConfig

internal class UnknownEnumValueModule : ApiGeneratorModule {
    override fun process(generator: ConfigurationGeneratorConfig) {
        generator.jsonDefaultValueProperties["coerceInputValues"] = "true"
    }

    override fun process(generator: ModelGeneratorConfig) {
        generator.defaultEnumValue = "UNKNOWN_"
    }
}
