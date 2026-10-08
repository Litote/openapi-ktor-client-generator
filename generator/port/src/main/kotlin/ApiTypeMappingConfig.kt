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
 * @param formatSuffix Kotlin expression suffix appended to a value to format it as a string, e.g. `.toString()`.
 * It is used for path, query, header, cookie and form parameters. When it differs from [DEFAULT_FORMAT_SUFFIX], a
 * `KSerializer` using it is also generated next to `ClientConfiguration` and applied to the model properties,
 * so JSON bodies use the same format.
 */
public data class StringFormatType(
    val qualifiedName: String,
    val parseFunction: String = "parse",
    val formatSuffix: String = DEFAULT_FORMAT_SUFFIX,
) {
    public companion object {
        /** Formats the value with its own `toString()`. */
        public const val DEFAULT_FORMAT_SUFFIX: String = ".toString()"
    }
}
