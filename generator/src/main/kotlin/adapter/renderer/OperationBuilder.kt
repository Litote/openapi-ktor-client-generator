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

package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import org.litote.openapi.ktor.client.generator.domain.DomainType
import org.litote.openapi.ktor.client.generator.domain.FormFieldSpec
import org.litote.openapi.ktor.client.generator.domain.OperationParameter
import org.litote.openapi.ktor.client.generator.domain.OperationSpec
import org.litote.openapi.ktor.client.generator.domain.RequestBodySpec
import org.litote.openapi.ktor.client.generator.shared.uncapitalize

/**
 * Builds individual API operations (methods) for a client class.
 */
internal class OperationBuilder(
    private val modelGenerator: ApiModelGenerator,
    private val responseBuilder: ResponseBuilder,
    private val clientConfigurationClass: ClassName,
    private val modelPackage: String,
    private val clientPackage: String,
    private val modelPackageOverrides: Map<String, String> = emptyMap(),
) {
    private companion object {
        val bodyMember = MemberName("io.ktor.client.call", "body")
        val setBodyMember = MemberName("io.ktor.client.request", "setBody")
        val contentTypeMember = MemberName("io.ktor.http", "contentType")
        val contentTypeClass = ClassName("io.ktor.http", "ContentType")
        val formDataMember = MemberName("io.ktor.client.request.forms", "formData")
        val formDataContentClass = ClassName("io.ktor.client.request.forms", "FormDataContent")
        val multiPartFormDataContentClass = ClassName("io.ktor.client.request.forms", "MultiPartFormDataContent")
        val parametersClass = ClassName("io.ktor.http", "Parameters")
        val headersOfMember = MemberName("io.ktor.http", "headersOf")
        val httpHeadersClass = ClassName("io.ktor.http", "HttpHeaders")
        val sseMember = MemberName("io.ktor.client.plugins.sse", "sse")
        val clientSseSessionClass = ClassName("io.ktor.client.plugins.sse", "ClientSSESession")
        const val ALIAS_HEADER = "setHeader"
    }

    /**
     * Builds an operation (method) and adds it to the client class.
     */
    fun buildOperation(
        context: ClientGenerationContext,
        operationInfo: OperationSpec,
        clientBuilder: TypeSpec.Builder,
        clientName: String,
    ) {
        val responseBaseName = operationInfo.name
        val functionName = responseBaseName.uncapitalize()

        // Request body - build form type and add to client
        val requestBody = operationInfo.requestBody
        requestBody?.let {
            if (it.isMultipartFormData || it.isUrlEncodedForm) {
                buildFormBodyDefinition(it, responseBaseName, clientBuilder)
            }
        }

        // Inline models (e.g. inline request body objects)
        operationInfo.inlineModels.forEach { modelSpec ->
            modelGenerator.buildModel(modelSpec)?.let { clientBuilder.addType(it) }
        }

        val parameters = operationInfo.parameters
        val pathParameters = parameters.filter { it.isPath }
        val queryParameters = parameters.filter { it.isQuery }
        val headerParameters = parameters.filter { it.isHeader }

        if (pathParameters.isNotEmpty()) context.hasPathComponents = true
        if (headerParameters.isNotEmpty()) context.hasHeaders = true

        val trimmedPath = buildPathExpression(operationInfo.path, pathParameters)

        if (operationInfo.isSse) {
            context.hasSseOperations = true
            buildSseOperation(
                context = context,
                operationInfo = operationInfo,
                clientBuilder = clientBuilder,
                functionName = functionName,
                requestBody = requestBody,
                pathParameters = pathParameters,
                queryParameters = queryParameters,
                headerParameters = headerParameters,
                trimmedPath = trimmedPath,
            )
            return
        }

        val responseSealedName = "${responseBaseName}Response"
        val responseSealedClass = ClassName(clientPackage, clientName, responseSealedName)
        clientBuilder.addType(responseBuilder.createSealedResponseClass(responseSealedName))
        val responseEntries =
            responseBuilder.buildResponseTypes(
                operationInfo.responses,
                clientBuilder,
                responseBaseName,
                responseSealedClass,
                modelPackage,
                modelPackageOverrides,
            )

        val methodMember = MemberName("io.ktor.client.request", operationInfo.method)
        val funBuilder =
            FunSpec
                .builder(functionName)
                .addModifiers(KModifier.SUSPEND)
                .returns(responseSealedClass)

        operationInfo.summary?.let { funBuilder.addKdoc("%L\n", it) }

        requestBody?.let {
            val requestTypeName = it.type.toTypeName(modelPackage, modelPackageOverrides)
            funBuilder.addParameter(it.parameterName, requestTypeName)
        }
        addParameters(funBuilder, pathParameters)
        addParameters(funBuilder, queryParameters)
        addParameters(funBuilder, headerParameters)

        val requestContentTypes = requestBody?.contentTypes
        val hasJsonContentType =
            requestContentTypes?.any { it.equals("application/json", ignoreCase = true) } == true

        funBuilder.addCode(
            buildFunctionBody(
                methodMember = methodMember,
                trimmedPath = trimmedPath,
                headerParameters = headerParameters,
                queryParameters = queryParameters,
                requestBody = requestBody,
                hasJsonContentType = hasJsonContentType,
                responseEntries = responseEntries,
                responseBaseName = responseBaseName,
            ),
        )

        clientBuilder.addFunction(funBuilder.build())
    }

    private fun buildFormBodyDefinition(
        requestBody: RequestBodySpec,
        responseBaseName: String,
        clientBuilder: TypeSpec.Builder,
    ) {
        val typeName = "${responseBaseName}Form"
        val fileClassName = ClassName("", "${typeName}File")
        val fields = requestBody.formFields

        val fileTypeSpec =
            if (fields.any { it.isBinary }) {
                buildFormFileType(fileClassName)
            } else {
                null
            }

        val typeSpec =
            TypeSpec
                .classBuilder(typeName)
                .addModifiers(KModifier.DATA)
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .apply {
                            fields.forEach { field ->
                                val fieldTypeName = field.type.toTypeName(modelPackage, modelPackageOverrides)
                                addParameter(
                                    ParameterSpec
                                        .builder(field.parameterName, fieldTypeName)
                                        .apply {
                                            if (field.isOptional) defaultValue("null")
                                        }.build(),
                                )
                            }
                        }.build(),
                ).apply {
                    fields.forEach { field ->
                        val fieldTypeName = field.type.toTypeName(modelPackage, modelPackageOverrides)
                        addProperty(
                            PropertySpec
                                .builder(field.parameterName, fieldTypeName)
                                .initializer(field.parameterName)
                                .build(),
                        )
                    }
                }.build()

        clientBuilder.addType(typeSpec)
        fileTypeSpec?.let { clientBuilder.addType(it) }
    }

    private fun buildFormFileType(fileClassName: ClassName): TypeSpec =
        TypeSpec
            .classBuilder(fileClassName.simpleName)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec
                    .constructorBuilder()
                    .addParameter("bytes", BYTE_ARRAY)
                    .addParameter("contentType", contentTypeClass)
                    .build(),
            ).addProperty(
                PropertySpec
                    .builder("bytes", BYTE_ARRAY)
                    .initializer("bytes")
                    .build(),
            ).addProperty(
                PropertySpec
                    .builder("contentType", contentTypeClass)
                    .initializer("contentType")
                    .build(),
            ).build()

    private fun buildSseOperation(
        context: ClientGenerationContext,
        operationInfo: OperationSpec,
        clientBuilder: TypeSpec.Builder,
        functionName: String,
        requestBody: RequestBodySpec?,
        pathParameters: List<OperationParameter>,
        queryParameters: List<OperationParameter>,
        headerParameters: List<OperationParameter>,
        trimmedPath: String,
    ) {
        val blockType =
            LambdaTypeName
                .get(
                    receiver = clientSseSessionClass,
                    returnType = UNIT,
                ).copy(suspending = true)

        val funBuilder =
            FunSpec
                .builder(functionName)
                .addModifiers(KModifier.SUSPEND)

        operationInfo.summary?.let { funBuilder.addKdoc("%L\n", it) }

        requestBody?.let {
            val requestTypeName = it.type.toTypeName(modelPackage, modelPackageOverrides)
            funBuilder.addParameter(it.parameterName, requestTypeName)
        }
        addParameters(funBuilder, pathParameters)
        addParameters(funBuilder, queryParameters)
        addParameters(funBuilder, headerParameters)
        funBuilder.addParameter(ParameterSpec.builder("block", blockType).build())

        funBuilder.addCode(buildSseFunctionBody(trimmedPath, headerParameters, queryParameters))

        clientBuilder.addFunction(funBuilder.build())
    }

    private fun buildSseFunctionBody(
        trimmedPath: String,
        headerParameters: List<OperationParameter>,
        queryParameters: List<OperationParameter>,
    ): CodeBlock {
        val hasRequestConfig = headerParameters.isNotEmpty() || queryParameters.isNotEmpty()
        val builder = CodeBlock.builder()
        builder.beginControlFlow("try")

        if (hasRequestConfig) {
            builder.beginControlFlow(
                "configuration.client.%M(urlString = %L, request = {",
                sseMember,
                trimmedPath,
            )
            headerParameters.forEach { param ->
                if (param.constName != null) {
                    if (param.isOptional) {
                        builder.beginControlFlow("if (%N != null)", param.camelCaseName)
                        builder.addStatement(
                            "$ALIAS_HEADER(%T.%L, %N)",
                            clientConfigurationClass,
                            param.constName,
                            param.camelCaseName,
                        )
                        builder.endControlFlow()
                    } else {
                        builder.addStatement(
                            "$ALIAS_HEADER(%T.%L, %N)",
                            clientConfigurationClass,
                            param.constName,
                            param.camelCaseName,
                        )
                    }
                } else {
                    if (param.isOptional) {
                        builder.beginControlFlow("if (%N != null)", param.camelCaseName)
                        builder.addStatement(
                            "$ALIAS_HEADER(%S, %N)",
                            param.originalName,
                            param.camelCaseName,
                        )
                        builder.endControlFlow()
                    } else {
                        builder.addStatement(
                            "$ALIAS_HEADER(%S, %N)",
                            param.originalName,
                            param.camelCaseName,
                        )
                    }
                }
            }
            if (queryParameters.isNotEmpty()) {
                builder.beginControlFlow("url")
                queryParameters.forEach { param ->
                    val suffix = param.toStringSuffix()
                    if (param.isOptional) {
                        builder.beginControlFlow("if (%N != null)", param.camelCaseName)
                        builder.addStatement(
                            "parameters.append(%S, %N$suffix)",
                            param.originalName,
                            param.camelCaseName,
                        )
                        builder.endControlFlow()
                    } else {
                        builder.addStatement(
                            "parameters.append(%S, %N$suffix)",
                            param.originalName,
                            param.camelCaseName,
                        )
                    }
                }
                builder.endControlFlow()
            }
            builder.endControlFlow()
            builder.beginControlFlow(")")
        } else {
            builder.beginControlFlow(
                "configuration.client.%M(urlString = %L)",
                sseMember,
                trimmedPath,
            )
        }

        builder.addStatement("block()")
        builder.endControlFlow()
        builder.endControlFlow()
        builder.beginControlFlow("catch(e: Exception)")
        builder.addStatement("%L(%L)", "configuration.exceptionLogger", "e")
        builder.endControlFlow()
        return builder.build()
    }

    private fun addParameters(
        funBuilder: FunSpec.Builder,
        parameters: List<OperationParameter>,
    ) {
        parameters.forEach { param ->
            val paramTypeName = param.type.toTypeName(modelPackage, modelPackageOverrides)
            val builder = ParameterSpec.builder(param.camelCaseName, paramTypeName)
            when {
                param.constDefaultName != null -> {
                    builder.defaultValue(
                        "%T.%L",
                        clientConfigurationClass,
                        param.constDefaultName,
                    )
                }

                param.defaultValue != null -> {
                    builder.defaultValue(param.defaultValue.toCodeBlock())
                }

                param.isOptional -> {
                    builder.defaultValue("null")
                }
            }
            funBuilder.addParameter(builder.build())
        }
    }

    private fun buildPathExpression(
        path: String,
        pathParameters: List<OperationParameter>,
    ): String {
        var result = "\"${path.trimStart('/')}\""
        pathParameters.forEach { param ->
            result +=
                if (param.isOptional) {
                    ".replace(\"/{${param.originalName}}\", if(${param.camelCaseName} == null) \"\" else \"/\${${param.camelCaseName}${param.toStringSuffix()}.encodeURLPathPart()}\")"
                } else {
                    ".replace(\"/{${param.originalName}}\", \"/\${${param.camelCaseName}${param.toStringSuffix()}.encodeURLPathPart()}\")"
                }
        }
        return result
    }

    private fun buildFunctionBody(
        methodMember: MemberName,
        trimmedPath: String,
        headerParameters: List<OperationParameter>,
        queryParameters: List<OperationParameter>,
        requestBody: RequestBodySpec?,
        hasJsonContentType: Boolean,
        responseEntries: List<RenderedResponseEntry>,
        responseBaseName: String,
    ): CodeBlock =
        CodeBlock
            .builder()
            .beginControlFlow("try")
            .beginControlFlow("val response = configuration.client.%M(%L)", methodMember, trimmedPath)
            .apply {
                headerParameters.forEach { param ->
                    if (param.constName != null) {
                        if (param.isOptional) {
                            beginControlFlow("if (%N != null)", param.camelCaseName)
                            addStatement(
                                "$ALIAS_HEADER(%T.%L, %N)",
                                clientConfigurationClass,
                                param.constName,
                                param.camelCaseName,
                            )
                            endControlFlow()
                        } else {
                            addStatement(
                                "$ALIAS_HEADER(%T.%L, %N)",
                                clientConfigurationClass,
                                param.constName,
                                param.camelCaseName,
                            )
                        }
                    } else {
                        if (param.isOptional) {
                            beginControlFlow("if (%N != null)", param.camelCaseName)
                            addStatement(
                                "$ALIAS_HEADER(%S, %N)",
                                param.originalName,
                                param.camelCaseName,
                            )
                            endControlFlow()
                        } else {
                            addStatement(
                                "$ALIAS_HEADER(%S, %N)",
                                param.originalName,
                                param.camelCaseName,
                            )
                        }
                    }
                }
                if (queryParameters.isNotEmpty()) {
                    beginControlFlow("url")
                    queryParameters.forEach { param ->
                        val suffix = param.toStringSuffix()
                        if (param.isOptional) {
                            beginControlFlow("if (%N != null)", param.camelCaseName)
                            addStatement("parameters.append(%S, %N$suffix)", param.originalName, param.camelCaseName)
                            endControlFlow()
                        } else {
                            addStatement("parameters.append(%S, %N$suffix)", param.originalName, param.camelCaseName)
                        }
                    }
                    endControlFlow()
                }
                when {
                    requestBody == null -> { // skip
                    }

                    requestBody.isMultipartFormData -> {
                        add(
                            CodeBlock
                                .builder()
                                .add("%M(%T(%M {\n", setBodyMember, multiPartFormDataContentClass, formDataMember)
                                .add(buildMultipartFormData(requestBody))
                                .add("}))\n")
                                .build(),
                        )
                    }

                    requestBody.isUrlEncodedForm -> {
                        add(
                            CodeBlock
                                .builder()
                                .add("%M(%T(%T.build {\n", setBodyMember, formDataContentClass, parametersClass)
                                .add(buildUrlEncodedFormData(requestBody))
                                .add("}))\n")
                                .build(),
                        )
                    }

                    else -> {
                        addStatement("%M(%N)", setBodyMember, requestBody.parameterName)
                        if (hasJsonContentType) {
                            addStatement("%M(%T.Application.Json)", contentTypeMember, contentTypeClass)
                        }
                    }
                }
            }.endControlFlow()
            .beginControlFlow("return when (response.status.value)")
            .apply {
                responseEntries.forEach { entry ->
                    val codesLiteral = entry.statusCodes.joinToString()
                    if (entry.bodyTypeName == null) {
                        addStatement("%L -> %N", codesLiteral, entry.type)
                    } else {
                        addStatement(
                            "%L -> %N(response.%M<%T>())",
                            codesLiteral,
                            entry.type,
                            bodyMember,
                            entry.bodyTypeName,
                        )
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

    private fun buildMultipartFormData(requestBody: RequestBodySpec): CodeBlock {
        val builder = CodeBlock.builder()
        requestBody.formFields.forEach { field ->
            val fieldAccess = "${requestBody.parameterName}.${field.parameterName}"
            if (field.isOptional) {
                builder.beginControlFlow("%L?.let { value ->", fieldAccess)
                appendFormPart(builder, field, "value")
                builder.endControlFlow()
            } else {
                appendFormPart(builder, field, fieldAccess)
            }
        }
        return builder.build()
    }

    private fun appendFormPart(
        builder: CodeBlock.Builder,
        field: FormFieldSpec,
        valueReference: String,
    ) {
        if (field.isBinary) {
            builder.addStatement(
                "append(%S, %L.bytes, %M(%T.ContentType, %L.contentType.toString()))",
                field.originalName,
                valueReference,
                headersOfMember,
                httpHeadersClass,
                valueReference,
            )
        } else {
            val typeName = field.type.toTypeName(modelPackage, modelPackageOverrides)
            if (typeName.isString()) {
                builder.addStatement("append(%S, %L)", field.originalName, valueReference)
            } else {
                builder.addStatement("append(%S, %L.toString())", field.originalName, valueReference)
            }
        }
    }

    private fun buildUrlEncodedFormData(requestBody: RequestBodySpec): CodeBlock {
        val builder = CodeBlock.builder()
        requestBody.formFields.forEach { field ->
            val fieldAccess = "${requestBody.parameterName}.${field.parameterName}"
            if (field.isOptional) {
                builder.beginControlFlow("%L?.let { value ->", fieldAccess)
                builder.addStatement("append(%S, value.toString())", field.originalName)
                builder.endControlFlow()
            } else {
                builder.addStatement("append(%S, %L.toString())", field.originalName, fieldAccess)
            }
        }
        return builder.build()
    }
}

private fun OperationParameter.toStringSuffix(): String =
    when {
        isEnum -> {
            ".serialName()"
        }

        type.isString -> {
            ""
        }

        type is DomainType.ListType || type is DomainType.SetType -> {
            ".joinToString(\",\")${if (isEnumArray) " { it.serialName() }" else ""}"
        }

        else -> {
            ".toString()"
        }
    }
