package org.litote.openapi.ktor.client.generator.domain

/** A generated Ktor client class grouping operations by OpenAPI tag. */
internal data class ClientSpec(
    /** Class name, e.g. `UserClient`. */
    val name: String,
    val operations: List<OperationSpec>,
)
