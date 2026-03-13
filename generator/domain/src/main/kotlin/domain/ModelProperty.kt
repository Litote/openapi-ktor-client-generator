package org.litote.openapi.ktor.client.generator.domain

/**
 * A property of a generated data class or form type.
 */
public data class ModelProperty(
    /** Original property name from OpenAPI (may be snake_case or contain illegal chars). */
    val originalName: String,
    /** camelCase sanitised name used as Kotlin identifier. */
    val camelCaseName: String,
    val type: DomainType,
    /** True when a `@SerialName` annotation is required (snake_case or illegal chars). */
    val needsSerialName: Boolean,
    /** Whether the property type is an enum. */
    val isEnum: Boolean = false,
    /** Explicit default value from the OpenAPI schema, if any. */
    val schemaDefaultValue: String? = null,
    /**
     * Nested models generated inline within this property's parent class:
     * e.g. an inline enum class or an inline nested data class.
     */
    val nestedModels: List<ModelSpec> = emptyList(),
)
