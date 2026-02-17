package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.TypeSpec
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Operation
import org.litote.openapi.ktor.client.generator.client.ParameterExtractor.Parameter

public data class ApiOperation(
    val path: String,
    val method: String,
    val operation: OpenAPIV3Operation,
) {
    internal val parameters: MutableList<Parameter> = mutableListOf()
}
