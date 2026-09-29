package org.litote.openapi.ktor.client.generator.module.kotlintimeinstant

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.ApiTypeMappingConfig
import org.litote.openapi.ktor.client.generator.port.StringFormatType

/** Maps OpenAPI `string`/`date-time` to `kotlin.time.Instant`. */
internal class KotlinTimeInstantModule : ApiGeneratorModule {
    override fun processTypeMapping(config: ApiTypeMappingConfig) {
        config.stringFormatTypes["date-time"] = StringFormatType("kotlin.time.Instant")
    }
}
