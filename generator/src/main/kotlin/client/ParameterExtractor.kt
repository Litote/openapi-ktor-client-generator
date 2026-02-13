/*
 * Copyright 2026 litote.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litote.openapi.ktor.client.generator.client

import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Operation
import community.flock.kotlinx.openapi.bindings.OpenAPIV3ParameterLocation
import org.litote.openapi.ktor.client.generator.ApiModel
import org.litote.openapi.ktor.client.generator.constName
import org.litote.openapi.ktor.client.generator.isConstSupported
import org.litote.openapi.ktor.client.generator.parameterDefaultLiteral
import org.litote.openapi.ktor.client.generator.parameterTypeBaseName
import org.litote.openapi.ktor.client.generator.parameterVariableName

/**
 * Extracts parameters from OpenAPI operations.
 *
 * Responsible for extracting header, path, and query parameters from an OpenAPI operation
 * and converting them to strongly-typed parameter objects.
 */
internal class ParameterExtractor(
    private val apiModel: ApiModel,
) {
    /**
     * Extracts header parameters from an operation.
     */
    fun extractHeaderParameters(operation: OpenAPIV3Operation): List<HeaderParameter> =
        extractParameters(operation, OpenAPIV3ParameterLocation.HEADER) { parameter, parameterTypeName, isOptional ->
            val constBaseName = constName(parameter.name)
            val defaultLiteral = parameterDefaultLiteral(parameter.schema, parameterTypeName)
            val defaultConst =
                if (defaultLiteral != null && isConstSupported(parameterTypeName)) {
                    "PARAMETER_${constBaseName}_DEFAULT_VALUE"
                } else {
                    null
                }
            HeaderParameter(
                parameterName = parameterVariableName(parameter.name),
                parameterType = if (isOptional) parameterTypeName.copy(nullable = true) else parameterTypeName,
                nameConst = "PARAMETER_$constBaseName",
                defaultValueConst = defaultConst,
                isOptional = isOptional,
            )
        }

    /**
     * Extracts path parameters from an operation.
     */
    fun extractPathParameters(operation: OpenAPIV3Operation): List<PathParameter> =
        extractParameters(operation, OpenAPIV3ParameterLocation.PATH) { parameter, parameterTypeName, isOptional ->
            PathParameter(
                parameterName = parameterVariableName(parameter.name),
                parameterType = if (isOptional) parameterTypeName.copy(nullable = true) else parameterTypeName,
                pathName = parameter.name,
                isOptional = isOptional,
            )
        }

    /**
     * Extracts query parameters from an operation.
     */
    fun extractQueryParameters(operation: OpenAPIV3Operation): List<QueryParameter> =
        extractParameters(operation, OpenAPIV3ParameterLocation.QUERY) { parameter, parameterTypeName, isOptional ->
            QueryParameter(
                parameterName = parameterVariableName(parameter.name),
                parameterType = if (isOptional) parameterTypeName.copy(nullable = true) else parameterTypeName,
                queryKey = parameter.name,
                isOptional = isOptional,
            )
        }

    private inline fun <T> extractParameters(
        operation: OpenAPIV3Operation,
        location: OpenAPIV3ParameterLocation,
        crossinline mapper: (
            parameter: community.flock.kotlinx.openapi.bindings.OpenAPIV3Parameter,
            typeName: TypeName,
            isOptional: Boolean,
        ) -> T,
    ): List<T> =
        operation
            .parameters
            .orEmpty()
            .asSequence()
            .mapNotNull { apiModel.getComponentParameter(it) }
            .filter { it.`in` == location }
            .distinctBy { it.name }
            .map { parameter ->
                val parameterTypeName =
                    parameter.schema?.let { schema ->
                        apiModel.getClassName(parameterTypeBaseName(parameter.name), schema)
                    } ?: STRING
                val defaultLiteral = parameterDefaultLiteral(parameter.schema, parameterTypeName)
                val isOptional = parameter.required != true && defaultLiteral == null
                mapper(parameter, parameterTypeName, isOptional)
            }.toList()
}

/**
 * Represents an HTTP header parameter.
 */
internal data class HeaderParameter(
    val parameterName: String,
    val parameterType: TypeName,
    val nameConst: String,
    val defaultValueConst: String?,
    val isOptional: Boolean,
)

/**
 * Represents a URL path parameter.
 */
internal data class PathParameter(
    val parameterName: String,
    val parameterType: TypeName,
    val pathName: String,
    val isOptional: Boolean,
)

/**
 * Represents a URL query parameter.
 */
internal data class QueryParameter(
    val parameterName: String,
    val parameterType: TypeName,
    val queryKey: String,
    val isOptional: Boolean,
)
