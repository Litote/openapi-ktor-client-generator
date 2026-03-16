package org.litote.openapi.ktor.client.generator.domain

/**
 * A reusable parameter defined in `components/parameters` and referenced from operations.
 * Generates `PARAMETER_XXX` constants in the `ClientConfiguration` companion object.
 */
public data class ComponentParameterSpec(
    val originalName: String,
    val constName: String,
    val type: DomainTypeSpec,
    val defaultValue: DefaultValueSpec? = null,
)
