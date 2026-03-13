package org.litote.openapi.ktor.client.generator.domain

/**
 * The request body of an operation.
 *
 * @param inlineModels Models that must be generated inline within the client class
 *   (form data class, optional file helper class, inline request object).
 */
public data class RequestBodySpec(
    val parameterName: String,
    val type: DomainType,
    val contentTypes: Set<String>,
    val isMultipartFormData: Boolean,
    val isUrlEncodedForm: Boolean,
    val formFields: List<FormFieldSpec> = emptyList(),
    val inlineModels: List<ModelSpec> = emptyList(),
)
