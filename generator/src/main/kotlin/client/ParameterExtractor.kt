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

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Operation
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Parameter
import community.flock.kotlinx.openapi.bindings.OpenAPIV3ParameterLocation.HEADER
import community.flock.kotlinx.openapi.bindings.OpenAPIV3ParameterLocation.PATH
import community.flock.kotlinx.openapi.bindings.OpenAPIV3ParameterLocation.QUERY
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
 * and converting them to parameter objects.
 */
internal class ParameterExtractor(
    private val apiModel: ApiModel,
) {
    /**
     * Extracts header parameters from an operation.
     */
    fun extractParameters(operation: OpenAPIV3Operation): List<Parameter> =
        operation
            .parameters
            .orEmpty()
            .asSequence()
            .mapNotNull { apiModel.getComponentParameter(it) }
            .distinctBy { it.name }
            .map { parameter ->
                val parameterTypeName =
                    parameter.schema?.let { schema ->
                        apiModel.getClassName(parameterTypeBaseName(parameter.name), schema)
                    } ?: STRING
                val defaultLiteral = parameterDefaultLiteral(parameter.schema, parameterTypeName)
                val isOptional = parameter.required != true
                val constBaseName = constName(parameter.name)
                val defaultConst =
                    if (defaultLiteral != null && isConstSupported(parameterTypeName)) {
                        "PARAMETER_${constBaseName}_DEFAULT_VALUE"
                    } else {
                        null
                    }
                Parameter(
                    parameter = parameter,
                    parameterName = parameterVariableName(parameter.name),
                    parameterType = if (isOptional) parameterTypeName.copy(nullable = true) else parameterTypeName,
                    constName = if (apiModel.componentParameters.none { it.name == parameter.name }) null else "PARAMETER_$constBaseName",
                    constDefaultValue = defaultConst,
                    isOptional = isOptional,
                    defaultValue = defaultLiteral,
                )
            }.toList()

    internal data class Parameter(
        val parameter: OpenAPIV3Parameter,
        val parameterName: String,
        val parameterType: TypeName,
        val isOptional: Boolean,
        val defaultValue: CodeBlock?,
        val constName: String?,
        val constDefaultValue: String?,
    ) {
        val originalName: String get() = parameter.name

        val isHeader: Boolean get() = parameter.`in` == HEADER
        val isPath: Boolean get() = parameter.`in` == PATH
        val isQuery: Boolean get() = parameter.`in` == QUERY
    }
}
