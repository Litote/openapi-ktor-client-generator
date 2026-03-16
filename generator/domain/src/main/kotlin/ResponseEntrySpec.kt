package org.litote.openapi.ktor.client.generator.domain

/**
 * One entry in the response sealed hierarchy of an operation.
 *
 * A group of [statusCodes] sharing the same body type and success/failure category.
 */
public data class ResponseEntrySpec(
    val statusCodes: List<Int>,
    val bodyType: DomainTypeSpec?,
    val isSuccess: Boolean,
)
