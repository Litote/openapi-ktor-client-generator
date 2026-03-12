package org.litote.openapi.ktor.client.generator.domain

/**
 * A parameter of a generated operation method (header, path, or query).
 */
internal data class OperationParameter(
    val originalName: String,
    val camelCaseName: String,
    val type: DomainType,
    val location: ParameterLocation,
    val required: Boolean,
    /** Companion-object constant name (`PARAMETER_XXX`) for component parameters. */
    val constName: String? = null,
    /** Companion-object constant name for the default value (`PARAMETER_XXX_DEFAULT_VALUE`). */
    val constDefaultName: String? = null,
    val defaultValue: DefaultValue? = null,
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
    val isOptional: Boolean get() = !required
    val isHeader: Boolean get() = location == ParameterLocation.HEADER
    val isPath: Boolean get() = location == ParameterLocation.PATH
    val isQuery: Boolean get() = location == ParameterLocation.QUERY
    val isEnum: Boolean get() = type is DomainType.InlineType && type.isEnum
    val isEnumArray: Boolean
        get() =
            (type is DomainType.ListType && type.element is DomainType.InlineType && type.element.isEnum) ||
                (type is DomainType.SetType && type.element is DomainType.InlineType && type.element.isEnum)
}
