package org.litote.openapi.ktor.client.generator.domain

/**
 * Domain representation of a generated model class.
 *
 * The sealed hierarchy mirrors the different kinds of Kotlin code that can be produced:
 * - [DataClassSpec] → `data class`
 * - [EnumSpec] → `enum class`
 * - [SealedClassSpec] → `sealed class`
 * - [ObjectSpec] → `object`
 * - [AliasSpec] → type alias to `JsonObject`
 */
public sealed class ModelSpec {
    public abstract val name: String

    public data class DataClassSpec(
        override val name: String,
        val properties: List<ModelProperty>,
        /** Name of the sealed parent class, if this is a subtype. */
        val sealedParentName: String? = null,
        /** Value for `@SerialName` when this is a sealed subtype. */
        val discriminatorValue: String? = null,
    ) : ModelSpec()

    public data class EnumSpec(
        override val name: String,
        val values: List<String>,
        val defaultValue: String? = null,
    ) : ModelSpec()

    public data class SealedClassSpec(
        override val name: String,
        /** Property name used as the JSON discriminator key. */
        val discriminatorPropertyName: String? = null,
    ) : ModelSpec()

    public data class ObjectSpec(
        override val name: String,
        val sealedParentName: String? = null,
        val discriminatorValue: String? = null,
    ) : ModelSpec()

    /** Schema with no properties and no enum values → alias to `JsonObject`. */
    public data class AliasSpec(
        override val name: String,
    ) : ModelSpec()
}
