package org.litote.openapi.ktor.client.generator.module.kotlinuuid

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.ApiTypeMappingConfig
import org.litote.openapi.ktor.client.generator.port.StringFormatType

/** Maps OpenAPI `string`/`uuid` to `kotlin.uuid.Uuid`. */
internal class KotlinUuidModule : ApiGeneratorModule {
    override fun processTypeMapping(config: ApiTypeMappingConfig) {
        config.stringFormatTypes["uuid"] = StringFormatType("kotlin.uuid.Uuid")
    }
}
