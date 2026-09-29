package org.litote.openapi.ktor.client.generator.port

/** Type mapping hook, exposed to `ApiGeneratorModule` implementors. */
public interface ApiTypeMappingConfig {
    /** OpenAPI `string` format (e.g. `date-time`, `uuid`) → Kotlin type used to render it. */
    public val stringFormatTypes: MutableMap<String, StringFormatType>
}

/**
 * Kotlin type used to render a `string` schema with a given OpenAPI format.
 *
 * @param qualifiedName fully qualified class name, e.g. `kotlin.uuid.Uuid`
 * @param parseFunction companion function used to render string default values, e.g. `Uuid.parse("…")`
 */
public data class StringFormatType(
    val qualifiedName: String,
    val parseFunction: String = "parse",
)
