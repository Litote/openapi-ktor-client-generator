package org.litote.openapi.ktor.client.generator.domain

/**
 * Pure domain representation of a Kotlin type, independent of KotlinPoet.
 */
public sealed class DomainTypeSpec {
    public abstract val nullable: Boolean

    /** Kotlin primitive / standard types. */
    public data class PrimitiveSpec(
        val kind: KindSpec,
        override val nullable: Boolean = false,
    ) : DomainTypeSpec() {
        public enum class KindSpec { STRING, INT, LONG, DOUBLE, FLOAT, BOOLEAN }
    }

    /** `List<element>` */
    public data class ListTypeSpec(
        val element: DomainTypeSpec,
        override val nullable: Boolean = false,
    ) : DomainTypeSpec()

    /** `Set<element>` */
    public data class SetTypeSpec(
        val element: DomainTypeSpec,
        override val nullable: Boolean = false,
    ) : DomainTypeSpec()

    /** `Map<String, value>` */
    public data class MapTypeSpec(
        val value: DomainTypeSpec,
        override val nullable: Boolean = false,
    ) : DomainTypeSpec()

    /** Reference to a named class in the model package. */
    public data class ModelReferenceSpec(
        val name: String,
        override val nullable: Boolean = false,
    ) : DomainTypeSpec()

    /**
     * A class generated inline (no package prefix), such as an inline enum or
     * an inline nested object generated within a parent class.
     */
    public data class InlineTypeSpec(
        val name: String,
        val isEnum: Boolean = false,
        override val nullable: Boolean = false,
    ) : DomainTypeSpec()

    /** Free-form JSON (`JsonElement`). */
    public data class JsonTypeSpec(
        override val nullable: Boolean = false,
    ) : DomainTypeSpec()

    public fun asNullable(): DomainTypeSpec =
        when (this) {
            is PrimitiveSpec -> copy(nullable = true)
            is ListTypeSpec -> copy(nullable = true)
            is SetTypeSpec -> copy(nullable = true)
            is MapTypeSpec -> copy(nullable = true)
            is ModelReferenceSpec -> copy(nullable = true)
            is InlineTypeSpec -> copy(nullable = true)
            is JsonTypeSpec -> copy(nullable = true)
        }

    public val isString: Boolean get() = this is PrimitiveSpec && kind == PrimitiveSpec.KindSpec.STRING
    public val isPrimitive: Boolean get() = this is PrimitiveSpec

    public fun asNonNullable(): DomainTypeSpec =
        when (this) {
            is PrimitiveSpec -> copy(nullable = false)
            is ListTypeSpec -> copy(nullable = false)
            is SetTypeSpec -> copy(nullable = false)
            is MapTypeSpec -> copy(nullable = false)
            is ModelReferenceSpec -> copy(nullable = false)
            is InlineTypeSpec -> copy(nullable = false)
            is JsonTypeSpec -> copy(nullable = false)
        }
}
