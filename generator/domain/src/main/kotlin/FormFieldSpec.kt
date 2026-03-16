package org.litote.openapi.ktor.client.generator.domain

/** A field in a generated multipart or URL-encoded form data class. */
public data class FormFieldSpec(
    val originalName: String,
    val parameterName: String,
    val type: DomainTypeSpec,
    val isBinary: Boolean,
    val isOptional: Boolean,
)
