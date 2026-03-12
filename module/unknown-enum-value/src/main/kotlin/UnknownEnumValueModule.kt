package org.litote.openapi.ktor.client.generator.module.unknown.enum.value

import kotlinx.serialization.json.JsonConfiguration
import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.ConfigurationGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ModelGeneratorConfig

internal class UnknownEnumValueModule : ApiGeneratorModule {
    override fun process(generator: ConfigurationGeneratorConfig) {
        generator.jsonDefaultValueProperties[JsonConfiguration::coerceInputValues.name] = "true"
    }

    override fun process(generator: ModelGeneratorConfig) {
        generator.defaultEnumValue = "UNKNOWN_"
    }
}
