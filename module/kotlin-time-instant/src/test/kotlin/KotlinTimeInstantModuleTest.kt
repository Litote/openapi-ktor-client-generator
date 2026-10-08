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

        assertEquals("kotlin.time.Instant", mapping.getValue("date-time").qualifiedName)
        assertEquals(setOf("date-time"), mapping.keys)
    }

    @Test
    fun `GIVEN default KotlinTimeInstantModule WHEN processTypeMapping THEN instants are truncated to milliseconds`() {
        val suffix = listOf(KotlinTimeInstantModule()).stringFormatTypes().getValue("date-time").formatSuffix

        assertEquals(
            ".let { kotlin.time.Instant.fromEpochSeconds(it.epochSeconds, it.nanosecondsOfSecond / 1_000_000 * 1_000_000) }.toString()",
            suffix,
        )
    }

    @Test
    fun `GIVEN seconds precision WHEN processTypeMapping THEN instants are truncated to seconds`() {
        val suffix =
            listOf(KotlinTimeInstantModule(InstantPrecision.SECONDS)).stringFormatTypes().getValue("date-time").formatSuffix

        assertEquals(".let { kotlin.time.Instant.fromEpochSeconds(it.epochSeconds) }.toString()", suffix)
    }

    @Test
    fun `GIVEN microseconds precision WHEN processTypeMapping THEN instants are truncated to microseconds`() {
        val suffix =
            listOf(KotlinTimeInstantModule(InstantPrecision.MICROSECONDS)).stringFormatTypes().getValue("date-time").formatSuffix

        assertEquals(
            ".let { kotlin.time.Instant.fromEpochSeconds(it.epochSeconds, it.nanosecondsOfSecond / 1_000 * 1_000) }.toString()",
            suffix,
        )
    }

    @Test
    fun `GIVEN nanoseconds precision WHEN processTypeMapping THEN Instant toString is used`() {
        val suffix =
            listOf(KotlinTimeInstantModule(InstantPrecision.NANOSECONDS)).stringFormatTypes().getValue("date-time").formatSuffix

        assertEquals(StringFormatType.DEFAULT_FORMAT_SUFFIX, suffix)
    }

    @Test
    fun `GIVEN module id WHEN getModule THEN KotlinTimeInstantModule is loaded through ServiceLoader`() {
        assertIs<KotlinTimeInstantModule>(ApiGeneratorModule.getModule("KotlinTimeInstantModule"))
    }
}
