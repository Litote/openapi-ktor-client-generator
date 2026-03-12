package org.litote.openapi.ktor.client.generator.domain

/**
 * Typed default value from an OpenAPI schema, used to generate parameter/property default values.
 */
internal sealed class DefaultValue {
    data class StringDefault(
        val value: String,
    ) : DefaultValue()

    data class BooleanDefault(
        val value: Boolean,
    ) : DefaultValue()

    data class IntDefault(
        val value: Int,
    ) : DefaultValue()

    data class LongDefault(
        val value: Long,
    ) : DefaultValue()

    data class DoubleDefault(
        val value: Double,
    ) : DefaultValue()

    data class FloatDefault(
        val value: Float,
    ) : DefaultValue()

    /** An enum constant default, e.g. `Status.ACTIVE`. */
    data class EnumDefault(
        val typeName: String,
        val enumValue: String,
    ) : DefaultValue()
}
