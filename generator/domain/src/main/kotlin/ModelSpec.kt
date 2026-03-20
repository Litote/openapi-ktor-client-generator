package org.litote.openapi.ktor.client.generator.domain

/**
 * Hints used by a [ModelSpec.SealedClassSpec] to generate a `JsonContentPolymorphicSerializer`
 * that selects the correct subtype based on JSON key presence.
 *
 * @param subtypeName name of the sealed subtype class
 * @param requiredSerialNames JSON property names that are required in the subtype's schema
 */
public data class SubtypeHint(
    val subtypeName: String,
    val requiredSerialNames: List<String>,
)

/**
 * Domain representation of a generated model class.
 *
 * The sealed hierarchy mirrors the different kinds of Kotlin code that can be produced:
 * - [DataClassSpec] → `data class`
 * - [EnumSpec] → `enum class`
 * - [SealedClassSpec] → `sealed class`
 * - [ObjectSpec] → `object`
 * - [AliasSpec] → type alias to `JsonObject`
 * - [InterfaceSpec] → `interface` (for allOf-only composition schemas)
 */
public sealed class ModelSpec {
    public abstract val name: String

    public data class DataClassSpec(
        override val name: String,
        val properties: List<ModelPropertySpec>,
        /** Name of the sealed parent class, if this is a subtype. */
        val sealedParentName: String? = null,
        /** Value for `@SerialName` when this is a sealed subtype. */
        val discriminatorValue: String? = null,
        /** Names of interfaces this class implements (from allOf $ref to interface-only schemas). */
        val interfaceParentNames: List<String> = emptyList(),
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
        /**
         * When non-null, a `JsonContentPolymorphicSerializer` companion is generated that selects
         * the correct subtype by inspecting which required JSON properties are present.
         * Used for response bodies with inline `oneOf` that have no discriminator property.
         */
        val subtypeHints: List<SubtypeHint>? = null,
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

    /**
     * Schema referenced only via `allOf` in other schemas — generated as a Kotlin `interface`.
     * This allows implementing classes to also extend a sealed parent (e.g. from `oneOf`),
     * preserving the composition relationship without conflicting with Kotlin single inheritance.
     */
    public data class InterfaceSpec(
        override val name: String,
        val properties: List<ModelPropertySpec>,
    ) : ModelSpec()
}
