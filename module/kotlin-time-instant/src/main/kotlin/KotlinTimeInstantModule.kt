package org.litote.openapi.ktor.client.generator.module.kotlintimeinstant

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.ApiTypeMappingConfig
import org.litote.openapi.ktor.client.generator.port.StringFormatType

/**
 * Maps OpenAPI `string`/`date-time` to `kotlin.time.Instant`.
 *
 * Instants are sent (parameters and JSON bodies) truncated to [precision], because some servers misread
 * the nanosecond precision of `Instant.toString()`. Use it with `customModules` to choose another precision:
 * `customModules.add(KotlinTimeInstantModule(InstantPrecision.SECONDS))`.
 */
public class KotlinTimeInstantModule(
    private val precision: InstantPrecision = InstantPrecision.MILLISECONDS,
) : ApiGeneratorModule {
    override fun processTypeMapping(config: ApiTypeMappingConfig) {
        config.stringFormatTypes["date-time"] =
            StringFormatType(
                qualifiedName = "kotlin.time.Instant",
                formatSuffix = precision.formatSuffix,
            )
    }
}

/** Precision of the `kotlin.time.Instant` values sent to the server. Received values are parsed whatever their precision. */
public enum class InstantPrecision(
    internal val formatSuffix: String,
) {
    /** e.g. `2026-10-09T15:23:39Z` */
    SECONDS(".let { kotlin.time.Instant.fromEpochSeconds(it.epochSeconds) }.toString()"),

    /** e.g. `2026-10-09T15:23:39.886Z` */
    MILLISECONDS(truncatedFormatSuffix(NANOS_PER_MILLISECOND)),

    /** e.g. `2026-10-09T15:23:39.886124Z` */
    MICROSECONDS(truncatedFormatSuffix(NANOS_PER_MICROSECOND)),

    /** e.g. `2026-10-09T15:23:39.886124921Z`: `Instant.toString()`, without truncation. */
    NANOSECONDS(StringFormatType.DEFAULT_FORMAT_SUFFIX),
}

private const val NANOS_PER_MILLISECOND = "1_000_000"
private const val NANOS_PER_MICROSECOND = "1_000"

private fun truncatedFormatSuffix(nanosPerUnit: String): String =
    ".let { kotlin.time.Instant.fromEpochSeconds(it.epochSeconds, it.nanosecondsOfSecond / $nanosPerUnit * $nanosPerUnit) }.toString()"
