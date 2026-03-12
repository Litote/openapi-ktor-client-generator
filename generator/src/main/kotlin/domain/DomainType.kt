package org.litote.openapi.ktor.client.generator.domain

/**
 * Pure domain representation of a Kotlin type, independent of KotlinPoet.
 */
internal sealed class DomainType {
    abstract val nullable: Boolean

    /** Kotlin primitive / standard types. */
    data class Primitive(
        val kind: Kind,
        override val nullable: Boolean = false,
    ) : DomainType() {
        enum class Kind { STRING, INT, LONG, DOUBLE, FLOAT, BOOLEAN }
    }

    /** `List<element>` */
    data class ListType(
        val element: DomainType,
        override val nullable: Boolean = false,
    ) : DomainType()

    /** `Set<element>` */
    data class SetType(
        val element: DomainType,
        override val nullable: Boolean = false,
    ) : DomainType()

    /** `Map<String, value>` */
    data class MapType(
        val value: DomainType,
        override val nullable: Boolean = false,
    ) : DomainType()

    /** Reference to a named class in the model package. */
    data class ModelReference(
        val name: String,
        override val nullable: Boolean = false,
    ) : DomainType()

    /**
     * A class generated inline (no package prefix), such as an inline enum or
     * an inline nested object generated within a parent class.
     */
    data class InlineType(
        val name: String,
        val isEnum: Boolean = false,
        override val nullable: Boolean = false,
    ) : DomainType()

    /** Free-form JSON (`JsonElement`). */
    data class JsonType(
        override val nullable: Boolean = false,
    ) : DomainType()

    fun asNullable(): DomainType =
        when (this) {
            is Primitive -> copy(nullable = true)
            is ListType -> copy(nullable = true)
            is SetType -> copy(nullable = true)
            is MapType -> copy(nullable = true)
            is ModelReference -> copy(nullable = true)
            is InlineType -> copy(nullable = true)
            is JsonType -> copy(nullable = true)
        }

    val isString: Boolean get() = this is Primitive && kind == Primitive.Kind.STRING
    val isPrimitive: Boolean get() = this is Primitive

    internal fun asNonNullable(): DomainType =
        when (this) {
            is Primitive -> copy(nullable = false)
            is ListType -> copy(nullable = false)
            is SetType -> copy(nullable = false)
            is MapType -> copy(nullable = false)
            is ModelReference -> copy(nullable = false)
            is InlineType -> copy(nullable = false)
            is JsonType -> copy(nullable = false)
        }
}
