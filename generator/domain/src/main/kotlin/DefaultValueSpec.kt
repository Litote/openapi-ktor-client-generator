package org.litote.openapi.ktor.client.generator.domain

/**
 * Typed default value from an OpenAPI schema, used to generate parameter/property default values.
 */
public sealed class DefaultValueSpec {
    public data class StringDefaultSpec(
        val value: String,
    ) : DefaultValueSpec()

    public data class BooleanDefaultSpec(
        val value: Boolean,
    ) : DefaultValueSpec()

    public data class IntDefaultSpec(
        val value: Int,
    ) : DefaultValueSpec()

    public data class LongDefaultSpec(
        val value: Long,
    ) : DefaultValueSpec()

    public data class DoubleDefaultSpec(
        val value: Double,
    ) : DefaultValueSpec()

    public data class FloatDefaultSpec(
        val value: Float,
    ) : DefaultValueSpec()

    /** An enum constant default, e.g. `Status.ACTIVE`. */
    public data class EnumDefaultSpec(
        val typeName: String,
        val enumValue: String,
    ) : DefaultValueSpec()
}
