package org.litote.openapi.ktor.client.generator.domain

/**
 * A header declared in the `headers` map of an OpenAPI response, exposed as a typed property
 * of the generated response class.
 *
 * [type] is always a [DomainTypeSpec.PrimitiveSpec]: non-primitive header schemas fall back to `String`.
 * Response headers therefore never reference a model and are ignored by the model usage analysis.
 *
 * @param originalName the header name as declared in the specification (e.g. `X-RateLimit-Limit`)
 * @param propertyName the camelCase Kotlin property name (e.g. `xRateLimitLimit`)
 */
public data class ResponseHeaderSpec(
    val originalName: String,
    val propertyName: String,
    val type: DomainTypeSpec.PrimitiveSpec,
    val description: String? = null,
)
