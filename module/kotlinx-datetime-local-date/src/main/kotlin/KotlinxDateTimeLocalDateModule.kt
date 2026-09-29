package org.litote.openapi.ktor.client.generator.module.kotlinxdatetimelocaldate

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.ApiTypeMappingConfig
import org.litote.openapi.ktor.client.generator.port.StringFormatType

/** Maps OpenAPI `string`/`date` to `kotlinx.datetime.LocalDate` (requires `org.jetbrains.kotlinx:kotlinx-datetime`). */
internal class KotlinxDateTimeLocalDateModule : ApiGeneratorModule {
    override fun processTypeMapping(config: ApiTypeMappingConfig) {
        config.stringFormatTypes["date"] = StringFormatType("kotlinx.datetime.LocalDate")
    }
}
