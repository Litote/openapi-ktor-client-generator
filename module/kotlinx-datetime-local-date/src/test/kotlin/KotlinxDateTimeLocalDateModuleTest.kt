package org.litote.openapi.ktor.client.generator.module.kotlinxdatetimelocaldate

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.StringFormatType
import org.litote.openapi.ktor.client.generator.stringFormatTypes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class KotlinxDateTimeLocalDateModuleTest {
    @Test
    fun `GIVEN KotlinxDateTimeLocalDateModule WHEN processTypeMapping THEN maps date format to LocalDate`() {
        val mapping = listOf(KotlinxDateTimeLocalDateModule()).stringFormatTypes()

        assertEquals(mapOf("date" to StringFormatType("kotlinx.datetime.LocalDate")), mapping)
    }

    @Test
    fun `GIVEN module id WHEN getModule THEN KotlinxDateTimeLocalDateModule is loaded through ServiceLoader`() {
        assertIs<KotlinxDateTimeLocalDateModule>(ApiGeneratorModule.getModule("KotlinxDateTimeLocalDateModule"))
    }
}
