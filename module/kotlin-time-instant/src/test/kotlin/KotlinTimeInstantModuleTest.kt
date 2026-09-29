package org.litote.openapi.ktor.client.generator.module.kotlintimeinstant

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.StringFormatType
import org.litote.openapi.ktor.client.generator.stringFormatTypes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class KotlinTimeInstantModuleTest {
    @Test
    fun `GIVEN KotlinTimeInstantModule WHEN processTypeMapping THEN maps date-time format to Instant`() {
        val mapping = listOf(KotlinTimeInstantModule()).stringFormatTypes()

        assertEquals(mapOf("date-time" to StringFormatType("kotlin.time.Instant")), mapping)
    }

    @Test
    fun `GIVEN module id WHEN getModule THEN KotlinTimeInstantModule is loaded through ServiceLoader`() {
        assertIs<KotlinTimeInstantModule>(ApiGeneratorModule.getModule("KotlinTimeInstantModule"))
    }
}
