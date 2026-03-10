package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Operation
import org.litote.openapi.ktor.client.generator.client.ParameterExtractor.Parameter

public data class ApiOperation(
    val path: String,
    val method: String,
    val operation: OpenAPIV3Operation,
) {
    internal val parameters: MutableList<Parameter> = mutableListOf()
    internal var requestBody: RequestBodyInfo? = null
}

internal data class RequestBodyInfo(
    val parameterName: String,
    val parameterType: TypeName,
    val contentTypes: Set<String>,
    val isMultipartFormData: Boolean,
    val isUrlEncodedForm: Boolean,
    val formFields: List<FormField> = emptyList(),
    val formTypeSpec: TypeSpec? = null,
    val additionalTypeSpecs: List<TypeSpec> = emptyList(),
)

internal data class FormField(
    val originalName: String,
    val parameterName: String,
    val typeName: TypeName,
    val isBinary: Boolean,
    val isOptional: Boolean,
)
