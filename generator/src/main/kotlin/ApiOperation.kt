package org.litote.openapi.ktor.client.generator

import community.flock.kotlinx.openapi.bindings.OpenAPIV3Operation

public data class ApiOperation(
    val path: String,
    val method: String,
    val operation: OpenAPIV3Operation
)