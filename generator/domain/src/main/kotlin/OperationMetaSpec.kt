package org.litote.openapi.ktor.client.generator.domain

/** Pure domain representation of an API operation, used for filtering. */
public data class OperationMetaSpec(
    val path: String,
    val method: String,
    val tags: List<String>,
)
