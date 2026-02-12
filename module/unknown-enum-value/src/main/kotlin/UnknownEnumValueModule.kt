package org.litote.openapi.ktor.client.generator.module.unknown.enum.value

import community.flock.kotlinx.openapi.bindings.simpleName
import kotlinx.serialization.json.JsonConfiguration
import org.litote.openapi.ktor.client.generator.ApiClientConfigurationGenerator
import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.ApiModelGenerator

internal class UnknownEnumValueModule : ApiGeneratorModule {

    override fun process(generator: ApiClientConfigurationGenerator) {
        generator.jsonDefaultValueProperties[JsonConfiguration::coerceInputValues.name] = "true"
    }

    override fun process(generator: ApiModelGenerator) {
        generator.defaultEnumValue = "UNKNOWN_"
    }
}