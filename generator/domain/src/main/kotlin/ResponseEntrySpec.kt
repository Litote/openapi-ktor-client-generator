package org.litote.openapi.ktor.client.generator.domain

/**
 * One entry in the response sealed hierarchy of an operation.
 *
 * A group of [statusCodes] sharing the same body type and success/failure category.
 *
 * [contentTypes] lists the declared non-JSON media types (e.g. `application/octet-stream`, `text/plain`)
 * of a binary or text body, without parameters. It is empty for JSON/YAML bodies.
 *
 * [headers] lists the response headers declared for these status codes (union over [statusCodes]).
 */
public data class ResponseEntrySpec(
    val statusCodes: List<Int>,
    val bodyType: DomainTypeSpec?,
    val isSuccess: Boolean,
    val contentTypes: List<String> = emptyList(),
    val headers: List<ResponseHeaderSpec> = emptyList(),
)
