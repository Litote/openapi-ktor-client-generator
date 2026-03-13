package org.litote.openapi.ktor.client.generator.domain

/** Where the API key is transmitted. */
public enum class SecuritySchemeLocation { HEADER, QUERY }

/**
 * An API-key security scheme extracted from the OpenAPI spec.
 *
 * @param paramName camelCase Kotlin parameter name derived from [name].
 */
public data class SecuritySchemeSpec(
    val name: String,
    val keyName: String,
    val location: SecuritySchemeLocation,
    val paramName: String,
)
