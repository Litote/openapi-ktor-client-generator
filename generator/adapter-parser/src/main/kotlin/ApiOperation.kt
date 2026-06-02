package org.litote.openapi.ktor.client.generator.adapter.parser

import community.flock.kotlinx.openapi.bindings.Operation

internal data class ApiOperation(
    val path: String,
    val method: String,
    val operation: Operation,
)
