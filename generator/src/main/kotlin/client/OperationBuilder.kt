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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import community.flock.kotlinx.openapi.bindings.OpenAPIV3RequestBody
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import org.litote.openapi.ktor.client.generator.ApiModel
import org.litote.openapi.ktor.client.generator.ApiOperation
import org.litote.openapi.ktor.client.generator.shared.capitalize
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase
import org.litote.openapi.ktor.client.generator.shared.uncapitalize

/**
 * Builds individual API operations (methods) for a client class.
 */
internal class OperationBuilder(
    private val apiModel: ApiModel,
    private val parameterExtractor: ParameterExtractor,
    private val responseBuilder: ResponseBuilder,
    private val clientConfigurationClass: ClassName,
) {
    private companion object {
        val bodyMember = MemberName("io.ktor.client.call", "body")
        val setBodyMember = MemberName("io.ktor.client.request", "setBody")
        val contentTypeMember = MemberName("io.ktor.http", "contentType")
        val contentTypeClass = ClassName("io.ktor.http", "ContentType")
        const val ALIAS_HEADER = "setHeader"
    }

    /**
     * Builds an operation (method) and adds it to the client class.
     */
    fun buildOperation(
        context: ClientGenerationContext,
        operationInfo: ApiOperation,
        clientBuilder: TypeSpec.Builder,
        clientName: String,
    ) {
        val operation = operationInfo.operation
        val operationId =
            operation.operationId
                ?: "${operationInfo.method}_${operationInfo.path.replace("/", "_").replace("{", "With_").replace("}", "")}"
        val responseBaseName = operationId.replace("-", "_").snakeToCamelCase().capitalize()
        val functionName = responseBaseName.uncapitalize()

        // Request body
        val requestBody = operation.requestBody as? OpenAPIV3RequestBody
        val requestSchema =
            requestBody
                ?.content
                ?.values
                ?.firstOrNull()
                ?.schema
        val requestType = requestSchema?.let { apiModel.getClassName("${responseBaseName}Request", it) }

        // Response types
        val responseSealedName = "${responseBaseName}Response"
        val packageName = apiModel.configuration.clientPackage
        val responseSealedClass = ClassName(packageName, clientName, responseSealedName)
        clientBuilder.addType(responseBuilder.createSealedResponseClass(responseSealedName))
        val responseEntries = responseBuilder.buildResponseTypes(operation, clientBuilder, responseBaseName, responseSealedClass)

        // Extract parameters
        val pathParameters = parameterExtractor.extractPathParameters(operation)
        val queryParameters = parameterExtractor.extractQueryParameters(operation)
        val headerParameters = parameterExtractor.extractHeaderParameters(operation)

        // Update context flags
        if (pathParameters.isNotEmpty()) context.hasPathComponents = true
        if (headerParameters.isNotEmpty()) context.hasHeaders = true

        // Build function
        val methodMember = MemberName("io.ktor.client.request", operationInfo.method)
        val funBuilder =
            FunSpec
                .builder(functionName)
                .addModifiers(KModifier.SUSPEND)
                .returns(responseSealedClass)

        operation.summary?.let { funBuilder.addKdoc("%L\n", it) }

        // Add parameters
        requestType?.let { funBuilder.addParameter("request", it) }
        addPathParameters(funBuilder, pathParameters)
        addQueryParameters(funBuilder, queryParameters)
        addHeaderParameters(funBuilder, headerParameters)

        // Build function body
        val requestContentTypes =
            requestBody
                ?.content
                ?.keys
                ?.map { it.value }
                ?.toSet()
        val hasJsonContentType = requestContentTypes?.any { it.equals("application/json", ignoreCase = true) } == true
        val trimmedPath = buildPathExpression(operationInfo.path, pathParameters)

        funBuilder.addCode(
            buildFunctionBody(
                methodMember = methodMember,
                trimmedPath = trimmedPath,
                headerParameters = headerParameters,
                queryParameters = queryParameters,
                requestType = requestType,
                hasJsonContentType = hasJsonContentType,
                responseEntries = responseEntries,
                responseBaseName = responseBaseName,
            ),
        )

        clientBuilder.addFunction(funBuilder.build())
    }

    private fun addPathParameters(
        funBuilder: FunSpec.Builder,
        parameters: List<PathParameter>,
    ) {
        parameters.forEach { param ->
            val builder = ParameterSpec.builder(param.parameterName, param.parameterType)
            if (param.isOptional) builder.defaultValue("null")
            funBuilder.addParameter(builder.build())
        }
    }

    private fun addQueryParameters(
        funBuilder: FunSpec.Builder,
        parameters: List<QueryParameter>,
    ) {
        parameters.forEach { param ->
            val builder = ParameterSpec.builder(param.parameterName, param.parameterType)
            if (param.isOptional) builder.defaultValue("null")
            funBuilder.addParameter(builder.build())
        }
    }

    private fun addHeaderParameters(
        funBuilder: FunSpec.Builder,
        parameters: List<HeaderParameter>,
    ) {
        parameters.forEach { param ->
            val builder = ParameterSpec.builder(param.parameterName, param.parameterType)
            when {
                param.defaultValueConst != null -> builder.defaultValue("%T.%L", clientConfigurationClass, param.defaultValueConst)
                param.isOptional -> builder.defaultValue("null")
            }
            funBuilder.addParameter(builder.build())
        }
    }

    private fun buildPathExpression(
        path: String,
        pathParameters: List<PathParameter>,
    ): String {
        var result = "\"${path.trimStart('/')}\""
        pathParameters.forEach { param ->
            result +=
                if (param.isOptional) {
                    ".replace(\"/{${param.pathName}}\", if(${param.parameterName} == null) \"\" else \"/\${${param.parameterName}.encodeURLPathPart()}\")"
                } else {
                    ".replace(\"/{${param.pathName}}\", \"/\${${param.parameterName}.encodeURLPathPart()}\")"
                }
        }
        return result
    }

    private fun buildFunctionBody(
        methodMember: MemberName,
        trimmedPath: String,
        headerParameters: List<HeaderParameter>,
        queryParameters: List<QueryParameter>,
        requestType: com.squareup.kotlinpoet.TypeName?,
        hasJsonContentType: Boolean,
        responseEntries: List<ResponseEntry>,
        responseBaseName: String,
    ): CodeBlock =
        CodeBlock
            .builder()
            .beginControlFlow("try")
            .beginControlFlow("val response = configuration.client.%M(%L)", methodMember, trimmedPath)
            .apply {
                // Headers
                headerParameters.forEach { param ->
                    if (param.isOptional) {
                        beginControlFlow("if (%N != null)", param.parameterName)
                        addStatement("$ALIAS_HEADER(%T.%L, %N)", clientConfigurationClass, param.nameConst, param.parameterName)
                        endControlFlow()
                    } else {
                        addStatement("$ALIAS_HEADER(%T.%L, %N)", clientConfigurationClass, param.nameConst, param.parameterName)
                    }
                }
                // Query parameters
                if (queryParameters.isNotEmpty()) {
                    beginControlFlow("url")
                    queryParameters.forEach { param ->
                        if (param.isOptional) {
                            beginControlFlow("if (%N != null)", param.parameterName)
                            addStatement("parameters.append(%S, %N.toString())", param.queryKey, param.parameterName)
                            endControlFlow()
                        } else {
                            addStatement("parameters.append(%S, %N.toString())", param.queryKey, param.parameterName)
                        }
                    }
                    endControlFlow()
                }
                // Request body
                if (requestType != null) {
                    addStatement("%M(%N)", setBodyMember, "request")
                    if (hasJsonContentType) {
                        addStatement("%M(%T.Application.Json)", contentTypeMember, contentTypeClass)
                    }
                }
            }.endControlFlow()
            .beginControlFlow("return when (response.status.value)")
            .apply {
                responseEntries.forEach { (statusCodes, bodyType, type) ->
                    val codesLiteral = statusCodes.joinToString()
                    if (bodyType == null) {
                        addStatement("%L -> %N", codesLiteral, type)
                    } else {
                        addStatement("%L -> %N(response.%M<%T>())", codesLiteral, type, bodyMember, bodyType)
                    }
                }
                addStatement("else -> %L(%L)", "${responseBaseName}ResponseUnknownFailure", "response.status.value")
            }.endControlFlow()
            .endControlFlow()
            .beginControlFlow("catch(e: Exception)")
            .addStatement("%L(%L)", "configuration.exceptionLogger", "e")
            .addStatement("return %L(%L)", "${responseBaseName}ResponseUnknownFailure", InternalServerError.value)
            .endControlFlow()
            .build()
}
