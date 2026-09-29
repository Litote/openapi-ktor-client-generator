package org.litote.openapi.ktor.client.generator.domain

/** How an operation parameter value is serialized (OpenAPI `style`). */
public enum class ParameterStyleSpec {
    FORM,
    SIMPLE,
    SPACE_DELIMITED,
    PIPE_DELIMITED,
    DEEP_OBJECT,
    ;

    public companion object {
        /** The OpenAPI default style for [location]. */
        public fun defaultFor(location: ParameterLocationSpec): ParameterStyleSpec =
            when (location) {
                ParameterLocationSpec.QUERY, ParameterLocationSpec.COOKIE -> FORM
                ParameterLocationSpec.PATH, ParameterLocationSpec.HEADER -> SIMPLE
            }
    }
}
