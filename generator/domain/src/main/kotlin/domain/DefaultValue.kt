package org.litote.openapi.ktor.client.generator.domain

/**
 * Typed default value from an OpenAPI schema, used to generate parameter/property default values.
 */
public sealed class DefaultValue {
    public data class StringDefault(
        val value: String,
    ) : DefaultValue()

    public data class BooleanDefault(
        val value: Boolean,
    ) : DefaultValue()

    public data class IntDefault(
        val value: Int,
    ) : DefaultValue()

    public data class LongDefault(
        val value: Long,
    ) : DefaultValue()

    public data class DoubleDefault(
        val value: Double,
    ) : DefaultValue()

    public data class FloatDefault(
        val value: Float,
    ) : DefaultValue()

    /** An enum constant default, e.g. `Status.ACTIVE`. */
    public data class EnumDefault(
        val typeName: String,
        val enumValue: String,
    ) : DefaultValue()
}
