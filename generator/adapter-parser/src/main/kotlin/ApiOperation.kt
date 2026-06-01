package org.litote.openapi.ktor.client.generator.adapter.parser

import community.flock.kotlinx.openapi.bindings.OpenAPIV30Operation

internal data class ApiOperation(
    val path: String,
    val method: String,
    val operation: OpenAPIV30Operation,
)
