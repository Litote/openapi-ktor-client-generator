package org.litote.openapi.ktor.client.generator.module.unknown.enum.value

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ApiModelGeneratorConfig

internal class UnknownEnumValueModule : ApiGeneratorModule {
    override fun process(generator: ApiConfigurationGeneratorConfig) {
        generator.jsonDefaultValueProperties["coerceInputValues"] = "true"
    }

    override fun process(generator: ApiModelGeneratorConfig) {
        generator.defaultEnumValue = "UNKNOWN_"
    }
}
