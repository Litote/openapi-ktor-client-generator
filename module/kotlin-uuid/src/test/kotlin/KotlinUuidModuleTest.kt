package org.litote.openapi.ktor.client.generator.module.kotlinuuid

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.StringFormatType
import org.litote.openapi.ktor.client.generator.stringFormatTypes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class KotlinUuidModuleTest {
    @Test
    fun `GIVEN KotlinUuidModule WHEN processTypeMapping THEN maps uuid format to Uuid`() {
        val mapping = listOf(KotlinUuidModule()).stringFormatTypes()

        assertEquals(mapOf("uuid" to StringFormatType("kotlin.uuid.Uuid")), mapping)
    }

    @Test
    fun `GIVEN module id WHEN getModule THEN KotlinUuidModule is loaded through ServiceLoader`() {
        assertIs<KotlinUuidModule>(ApiGeneratorModule.getModule("KotlinUuidModule"))
    }
}
