package org.litote.openapi.ktor.client.generator.domain

/**
 * A parameter of a generated operation method (header, path, or query).
 */
public data class OperationParameterSpec(
    val originalName: String,
    val camelCaseName: String,
    val type: DomainTypeSpec,
    val location: ParameterLocationSpec,
    val required: Boolean,
    /** Companion-object constant name (`PARAMETER_XXX`) for component parameters. */
    val constName: String? = null,
    /** Companion-object constant name for the default value (`PARAMETER_XXX_DEFAULT_VALUE`). */
    val constDefaultName: String? = null,
    val defaultValue: DefaultValueSpec? = null,
    /**
     * When non-null, this parameter's type needs an inline nested model generated inside the
     * client class. [additionalModel] holds that model spec.
     */
    val additionalModel: ModelSpec? = null,
    /**
     * The base type name before conflict-rename (e.g. "ExcludeTypes"). Used to group inline
     * models when adding them to the client class, preserving the original ordering.
     */
    val additionalModelBaseName: String? = null,
) {
    public val isOptional: Boolean get() = !required
    public val isHeader: Boolean get() = location == ParameterLocationSpec.HEADER
    public val isPath: Boolean get() = location == ParameterLocationSpec.PATH
    public val isQuery: Boolean get() = location == ParameterLocationSpec.QUERY
    public val isEnum: Boolean get() = type is DomainTypeSpec.InlineTypeSpec && type.isEnum
    public val isEnumArray: Boolean
        get() =
            (type is DomainTypeSpec.ListTypeSpec && type.element is DomainTypeSpec.InlineTypeSpec && type.element.isEnum) ||
                (type is DomainTypeSpec.SetTypeSpec && type.element is DomainTypeSpec.InlineTypeSpec && type.element.isEnum)
}
