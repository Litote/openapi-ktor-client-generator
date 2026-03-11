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

import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import community.flock.kotlinx.openapi.bindings.OpenAPIV3RequestBody
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Type
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import org.litote.openapi.ktor.client.generator.ApiModel
import org.litote.openapi.ktor.client.generator.ApiOperation
import org.litote.openapi.ktor.client.generator.FormField
import org.litote.openapi.ktor.client.generator.ModelGenerator
import org.litote.openapi.ktor.client.generator.RequestBodyInfo
import org.litote.openapi.ktor.client.generator.client.ParameterExtractor.Parameter
import org.litote.openapi.ktor.client.generator.firstType
import org.litote.openapi.ktor.client.generator.isString
import org.litote.openapi.ktor.client.generator.methodName
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
        val formDataMember = MemberName("io.ktor.client.request.forms", "formData")
        val formDataContentClass = ClassName("io.ktor.client.request.forms", "FormDataContent")
        val multiPartFormDataContentClass = ClassName("io.ktor.client.request.forms", "MultiPartFormDataContent")
        val parametersClass = ClassName("io.ktor.http", "Parameters")
        val headersOfMember = MemberName("io.ktor.http", "headersOf")
        val httpHeadersClass = ClassName("io.ktor.http", "HttpHeaders")
        const val ALIAS_HEADER = "setHeader"
    }

    fun analyseOperation(
        context: ClientGenerationContext,
        operationInfo: ApiOperation,
    ) {
        // Extract parameters
        val parameters = parameterExtractor.extractParameters(operationInfo)
        operationInfo.parameters.addAll(parameters)
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
        val responseBaseName = operationInfo.methodName(context)
        val functionName = responseBaseName.uncapitalize()

        // Request body
        val requestBody = operation.requestBody as? OpenAPIV3RequestBody
        val requestBodyInfo = requestBodyInfo(requestBody, responseBaseName, context.modelGenerator)
        operationInfo.requestBody = requestBodyInfo
        requestBodyInfo?.formTypeSpec?.let { clientBuilder.addType(it) }
        requestBodyInfo?.additionalTypeSpecs?.forEach { clientBuilder.addType(it) }

        // Response types
        val responseSealedName = "${responseBaseName}Response"
        val packageName = apiModel.configuration.clientPackage
        val responseSealedClass = ClassName(packageName, clientName, responseSealedName)
        clientBuilder.addType(responseBuilder.createSealedResponseClass(responseSealedName))
        val responseEntries =
            responseBuilder.buildResponseTypes(operation, clientBuilder, responseBaseName, responseSealedClass)

        // group parameters
        val parameters = operationInfo.parameters
        val pathParameters = parameters.filter { it.isPath }
        val queryParameters = parameters.filter { it.isQuery }
        val headerParameters = parameters.filter { it.isHeader }

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
        requestBodyInfo?.let { funBuilder.addParameter(it.parameterName, it.parameterType) }
        addParameters(funBuilder, pathParameters)
        addParameters(funBuilder, queryParameters)
        addParameters(funBuilder, headerParameters)

        // Build function body
        val requestContentTypes = requestBodyInfo?.contentTypes
        val hasJsonContentType = requestContentTypes?.any { it.equals("application/json", ignoreCase = true) } == true
        val trimmedPath = buildPathExpression(operationInfo.path, pathParameters)

        funBuilder.addCode(
            buildFunctionBody(
                methodMember = methodMember,
                trimmedPath = trimmedPath,
                headerParameters = headerParameters,
                queryParameters = queryParameters,
                requestBody = requestBodyInfo,
                hasJsonContentType = hasJsonContentType,
                responseEntries = responseEntries,
                responseBaseName = responseBaseName,
            ),
        )

        clientBuilder.addFunction(funBuilder.build())
    }

    private fun addParameters(
        funBuilder: FunSpec.Builder,
        parameters: List<Parameter>,
    ) {
        parameters.forEach { param ->
            val builder = ParameterSpec.builder(param.parameterName, param.parameterType)
            when {
                param.constDefaultValue != null -> {
                    builder.defaultValue(
                        "%T.%L",
                        clientConfigurationClass,
                        param.constDefaultValue,
                    )
                }

                param.defaultValue != null -> {
                    builder.defaultValue(param.defaultValue)
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
        pathParameters: List<Parameter>,
    ): String {
        var result = "\"${path.trimStart('/')}\""
        pathParameters.forEach { param ->
            result +=
                if (param.isOptional) {
                    ".replace(\"/{${param.originalName}}\", if(${param.parameterName} == null) \"\" else \"/\${${param.parameterName}${param.toStringSuffix}.encodeURLPathPart()}\")"
                } else {
                    ".replace(\"/{${param.originalName}}\", \"/\${${param.parameterName}${param.toStringSuffix}.encodeURLPathPart()}\")"
                }
        }
        return result
    }

    private fun buildFunctionBody(
        methodMember: MemberName,
        trimmedPath: String,
        headerParameters: List<Parameter>,
        queryParameters: List<Parameter>,
        requestBody: RequestBodyInfo?,
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
                    if (param.constName != null) {
                        if (param.isOptional) {
                            beginControlFlow("if (%N != null)", param.parameterName)
                            addStatement(
                                "$ALIAS_HEADER(%T.%L, %N)",
                                clientConfigurationClass,
                                param.constName,
                                param.parameterName,
                            )
                            endControlFlow()
                        } else {
                            addStatement(
                                "$ALIAS_HEADER(%T.%L, %N)",
                                clientConfigurationClass,
                                param.constName,
                                param.parameterName,
                            )
                        }
                    } else {
                        if (param.isOptional) {
                            beginControlFlow("if (%N != null)", param.parameterName)
                            addStatement(
                                "$ALIAS_HEADER(%S, %N)",
                                param.parameter.name,
                                param.parameterName,
                            )
                            endControlFlow()
                        } else {
                            addStatement(
                                "$ALIAS_HEADER(%S, %N)",
                                param.parameter.name,
                                param.parameterName,
                            )
                        }
                    }
                }
                // Query parameters
                if (queryParameters.isNotEmpty()) {
                    beginControlFlow("url")
                    queryParameters.forEach { param ->
                        val suffix = param.toStringSuffix
                        if (param.isOptional) {
                            beginControlFlow("if (%N != null)", param.parameterName)
                            addStatement("parameters.append(%S, %N$suffix)", param.originalName, param.parameterName)
                            endControlFlow()
                        } else {
                            addStatement("parameters.append(%S, %N$suffix)", param.originalName, param.parameterName)
                        }
                    }
                    endControlFlow()
                }
                // Request body
                when {
                    requestBody == null -> {
                        //skip
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

    private fun requestBodyInfo(
        requestBody: OpenAPIV3RequestBody?,
        responseBaseName: String,
        modelGenerator: ModelGenerator,
    ): RequestBodyInfo? {
        val content = requestBody?.content ?: return null
        val requestSchema =
            content
                .values
                .firstOrNull()
                ?.schema
        val contentTypes = content.keys.map { it.value }.toSet()
        val isMultipartFormData = contentTypes.any { it.equals("multipart/form-data", ignoreCase = true) }
        val isUrlEncodedForm = contentTypes.any { it.equals("application/x-www-form-urlencoded", ignoreCase = true) }
        val parameterName = if (isMultipartFormData || isUrlEncodedForm) "form" else "request"
        val formSchema =
            if (isMultipartFormData || isUrlEncodedForm) {
                apiModel.resolveSchema(requestSchema) ?: requestSchema as? OpenAPIV3Schema
            } else {
                null
            }
        val formBodyDefinition =
            if (formSchema != null) {
                buildFormBodyDefinition(responseBaseName, formSchema)
            } else {
                null
            }
        val inlineObjectSchema =
            if (!isMultipartFormData && !isUrlEncodedForm) {
                (requestSchema as? OpenAPIV3Schema)?.takeIf {
                    it.oneOf.isNullOrEmpty() && !it.properties.isNullOrEmpty()
                }
            } else {
                null
            }
        val inlineObjectDefinition =
            inlineObjectSchema?.let {
                buildInlineObjectDefinition("${responseBaseName}Request", it, modelGenerator)
            }
        val requestType =
            when {
                formBodyDefinition != null -> formBodyDefinition.className
                inlineObjectDefinition != null -> inlineObjectDefinition.className
                else -> requestSchema?.let { apiModel.getClassName("${responseBaseName}Request", it) }
            }
        return requestType?.let {
            RequestBodyInfo(
                parameterName = parameterName,
                parameterType = it,
                contentTypes = contentTypes,
                isMultipartFormData = isMultipartFormData,
                isUrlEncodedForm = isUrlEncodedForm,
                formFields = formBodyDefinition?.fields.orEmpty(),
                formTypeSpec = formBodyDefinition?.typeSpec,
                additionalTypeSpecs =
                    formBodyDefinition?.additionalTypeSpecs.orEmpty() +
                        listOfNotNull(inlineObjectDefinition?.typeSpec),
            )
        }
    }

    private data class InlineObjectDefinition(
        val className: ClassName,
        val typeSpec: TypeSpec,
    )

    private fun buildInlineObjectDefinition(
        requestName: String,
        schema: OpenAPIV3Schema,
        modelGenerator: ModelGenerator,
    ): InlineObjectDefinition {
        val className = ClassName("", requestName)
        val typeSpec =
            (modelGenerator.buildModel(requestName, schema) ?: TypeSpec.classBuilder(requestName).build())
                .toBuilder()
                .build()
        return InlineObjectDefinition(className, typeSpec)
    }

    private fun buildFormBodyDefinition(
        responseBaseName: String,
        schema: OpenAPIV3Schema,
    ): FormBodyDefinition {
        val typeName = "${responseBaseName}Form"
        val formClassName = ClassName("", typeName)
        val fileClassName = ClassName("", "${typeName}File")
        val properties =
            schema.properties
                ?.map { (name, propertySchema) ->
                    val property = apiModel.getClassProperty(name, propertySchema, schema)
                    val resolvedSchema = apiModel.resolveSchema(propertySchema) ?: propertySchema as? OpenAPIV3Schema
                    val isBinary =
                        resolvedSchema?.firstType == OpenAPIV3Type.STRING && resolvedSchema.format == "binary"
                    val typeName =
                        if (isBinary) {
                            if (property.type.isNullable) {
                                fileClassName.copy(nullable = true)
                            } else {
                                fileClassName
                            }
                        } else {
                            property.type
                        }
                    FormField(
                        originalName = name,
                        parameterName = property.camelCaseName,
                        typeName = typeName,
                        isBinary = isBinary,
                        isOptional = property.type.isNullable,
                    )
                }.orEmpty()
        val fileTypeSpec =
            if (properties.any { it.isBinary }) {
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
                            properties.forEach { property ->
                                addParameter(
                                    ParameterSpec
                                        .builder(property.parameterName, property.typeName)
                                        .apply {
                                            if (property.isOptional) {
                                                defaultValue("null")
                                            }
                                        }.build(),
                                )
                            }
                        }.build(),
                ).apply {
                    properties.forEach { property ->
                        addProperty(
                            com.squareup.kotlinpoet.PropertySpec
                                .builder(property.parameterName, property.typeName)
                                .initializer(property.parameterName)
                                .build(),
                        )
                    }
                }.build()

        return FormBodyDefinition(
            className = formClassName,
            typeSpec = typeSpec,
            fields = properties,
            additionalTypeSpecs = listOfNotNull(fileTypeSpec),
        )
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
                com.squareup.kotlinpoet.PropertySpec
                    .builder("bytes", BYTE_ARRAY)
                    .initializer("bytes")
                    .build(),
            ).addProperty(
                com.squareup.kotlinpoet.PropertySpec
                    .builder("contentType", contentTypeClass)
                    .initializer("contentType")
                    .build(),
            ).build()

    private fun buildMultipartFormData(requestBody: RequestBodyInfo): CodeBlock {
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
        field: FormField,
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
        } else if (field.typeName.isString()) {
            builder.addStatement("append(%S, %L)", field.originalName, valueReference)
        } else {
            builder.addStatement("append(%S, %L.toString())", field.originalName, valueReference)
        }
    }

    private fun buildUrlEncodedFormData(requestBody: RequestBodyInfo): CodeBlock {
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

    private data class FormBodyDefinition(
        val className: ClassName,
        val typeSpec: TypeSpec,
        val fields: List<FormField>,
        val additionalTypeSpecs: List<TypeSpec>,
    )
}
