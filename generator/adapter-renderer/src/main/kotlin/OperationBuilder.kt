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
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.FormFieldSpec
import org.litote.openapi.ktor.client.generator.domain.OperationParameterSpec
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
    private data class OperationParameters(
        val pathParameters: List<OperationParameterSpec>,
        val queryParameters: List<OperationParameterSpec>,
        val headerParameters: List<OperationParameterSpec>,
        val trimmedPath: String,
    )

    private data class RequestBodyContext(
        val requestBody: RequestBodySpec?,
        val hasJsonContentType: Boolean,
        val hasYamlContentType: Boolean,
    )

    private data class ResponseBuildContext(
        val entries: List<RenderedResponseEntry>,
        val baseName: String,
    )

    private companion object {
        private const val KTOR_HTTP = "io.ktor.http"
        private const val KTOR_FORMS = "io.ktor.client.request.forms"
        private const val IF_NOT_NULL = "if (%N != null)"

        val bodyMember = MemberName("io.ktor.client.call", "body")
        val setBodyMember = MemberName("io.ktor.client.request", "setBody")
        val contentTypeMember = MemberName(KTOR_HTTP, "contentType")
        val contentTypeClass = ClassName(KTOR_HTTP, "ContentType")
        val formDataMember = MemberName(KTOR_FORMS, "formData")
        val formDataContentClass = ClassName(KTOR_FORMS, "FormDataContent")
        val multiPartFormDataContentClass = ClassName(KTOR_FORMS, "MultiPartFormDataContent")
        val parametersClass = ClassName(KTOR_HTTP, "Parameters")
        val headersClass = ClassName(KTOR_HTTP, "Headers")
        val httpHeadersClass = ClassName(KTOR_HTTP, "HttpHeaders")
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
        val operationParams =
            OperationParameters(
                pathParameters = pathParameters,
                queryParameters = queryParameters,
                headerParameters = headerParameters,
                trimmedPath = trimmedPath,
            )

        if (operationInfo.isSse) {
            context.hasSseOperations = true
            buildSseOperation(
                operationInfo = operationInfo,
                clientBuilder = clientBuilder,
                functionName = functionName,
                requestBody = requestBody,
                params = operationParams,
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
        val hasYamlContentType =
            requestContentTypes?.any {
                it.equals("application/yaml", ignoreCase = true) || it.equals("application/x-yaml", ignoreCase = true)
            } == true

        funBuilder.addCode(
            buildFunctionBody(
                methodMember = methodMember,
                operationParams = operationParams,
                requestBodyCtx =
                    RequestBodyContext(
                        requestBody = requestBody,
                        hasJsonContentType = hasJsonContentType,
                        hasYamlContentType = hasYamlContentType,
                    ),
                responseCtx =
                    ResponseBuildContext(
                        entries = responseEntries,
                        baseName = responseBaseName,
                    ),
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
                    .addParameter(
                        ParameterSpec
                            .builder("filename", STRING)
                            .defaultValue("%S", "upload")
                            .build(),
                    ).build(),
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
            ).addProperty(
                PropertySpec
                    .builder("filename", STRING)
                    .initializer("filename")
                    .build(),
            ).build()

    private fun buildSseOperation(
        operationInfo: OperationSpec,
        clientBuilder: TypeSpec.Builder,
        functionName: String,
        requestBody: RequestBodySpec?,
        params: OperationParameters,
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
        addParameters(funBuilder, params.pathParameters)
        addParameters(funBuilder, params.queryParameters)
        addParameters(funBuilder, params.headerParameters)
        funBuilder.addParameter(ParameterSpec.builder("block", blockType).build())

        funBuilder.addCode(buildSseFunctionBody(params.trimmedPath, params.headerParameters, params.queryParameters))

        clientBuilder.addFunction(funBuilder.build())
    }

    private fun buildSseFunctionBody(
        trimmedPath: String,
        headerParameters: List<OperationParameterSpec>,
        queryParameters: List<OperationParameterSpec>,
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
            addRegularHeaderParams(builder, headerParameters)
            addQueryParams(builder, queryParameters)
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

    private fun addQueryParams(
        builder: CodeBlock.Builder,
        queryParameters: List<OperationParameterSpec>,
    ) {
        if (queryParameters.isEmpty()) return
        builder.beginControlFlow("url")
        queryParameters.forEach { param ->
            val suffix = param.toStringSuffix()
            if (param.isOptional) {
                builder.beginControlFlow(IF_NOT_NULL, param.camelCaseName)
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

    private fun addParameters(
        funBuilder: FunSpec.Builder,
        parameters: List<OperationParameterSpec>,
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
                    param.defaultValue?.let { builder.defaultValue(it.toCodeBlock()) }
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
        pathParameters: List<OperationParameterSpec>,
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
        operationParams: OperationParameters,
        requestBodyCtx: RequestBodyContext,
        responseCtx: ResponseBuildContext,
    ): CodeBlock {
        val responseBaseName = responseCtx.baseName
        val builder = CodeBlock.builder()
        builder.beginControlFlow("try")
        builder.beginControlFlow("val response = configuration.client.%M(%L)", methodMember, operationParams.trimmedPath)
        addRegularHeaderParams(builder, operationParams.headerParameters)
        addQueryParams(builder, operationParams.queryParameters)
        addRequestBodyCode(builder, requestBodyCtx)
        builder.endControlFlow()
        builder.beginControlFlow("return when (response.status.value)")
        addResponseCases(builder, responseCtx, responseBaseName)
        builder.endControlFlow()
        builder.endControlFlow()
        builder.beginControlFlow("catch(e: Exception)")
        builder.addStatement("%L(%L)", "configuration.exceptionLogger", "e")
        builder.addStatement("return %L(%L)", "${responseBaseName}ResponseUnknownFailure", InternalServerError.value)
        builder.endControlFlow()
        return builder.build()
    }

    private fun addRegularHeaderParams(
        builder: CodeBlock.Builder,
        headerParameters: List<OperationParameterSpec>,
    ) {
        headerParameters.forEach { param ->
            if (param.constName != null) {
                if (param.isOptional) {
                    builder.beginControlFlow(IF_NOT_NULL, param.camelCaseName)
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
                    builder.beginControlFlow(IF_NOT_NULL, param.camelCaseName)
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
    }

    private fun addRequestBodyCode(
        builder: CodeBlock.Builder,
        requestBodyCtx: RequestBodyContext,
    ) {
        val requestBody = requestBodyCtx.requestBody ?: return
        when {
            requestBody.isMultipartFormData -> {
                builder.add(
                    CodeBlock
                        .builder()
                        .add("%M(%T(%M {\n", setBodyMember, multiPartFormDataContentClass, formDataMember)
                        .add(buildMultipartFormData(requestBody))
                        .add("}))\n")
                        .build(),
                )
            }

            requestBody.isUrlEncodedForm -> {
                builder.add(
                    CodeBlock
                        .builder()
                        .add("%M(%T(%T.build {\n", setBodyMember, formDataContentClass, parametersClass)
                        .add(buildUrlEncodedFormData(requestBody))
                        .add("}))\n")
                        .build(),
                )
            }

            else -> {
                builder.addStatement("%M(%N)", setBodyMember, requestBody.parameterName)
                when {
                    requestBodyCtx.hasJsonContentType -> {
                        builder.addStatement("%M(%T.Application.Json)", contentTypeMember, contentTypeClass)
                    }

                    requestBodyCtx.hasYamlContentType -> {
                        builder.addStatement(
                            "%M(%T(%S, %S))",
                            contentTypeMember,
                            contentTypeClass,
                            "application",
                            "yaml",
                        )
                    }
                }
            }
        }
    }

    private fun addResponseCases(
        builder: CodeBlock.Builder,
        responseCtx: ResponseBuildContext,
        responseBaseName: String,
    ) {
        responseCtx.entries.forEach { entry ->
            val codesLiteral = entry.statusCodes.joinToString()
            if (entry.bodyTypeName == null) {
                builder.addStatement("%L -> %N", codesLiteral, entry.type)
            } else {
                builder.addStatement(
                    "%L -> %N(response.%M<%T>())",
                    codesLiteral,
                    entry.type,
                    bodyMember,
                    entry.bodyTypeName,
                )
            }
        }
        builder.addStatement("else -> %L(%L)", "${responseBaseName}ResponseUnknownFailure", "response.status.value")
    }

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
            builder
                .add("append(%S, %L.bytes, %T.build {\n", field.originalName, valueReference, headersClass)
                .indent()
                .addStatement("append(%T.ContentType, %L.contentType.toString())", httpHeadersClass, valueReference)
                .addStatement(
                    "append(%T.ContentDisposition, %S + %L.filename + %S)",
                    httpHeadersClass,
                    "form-data; name=\"${field.originalName}\"; filename=\"",
                    valueReference,
                    "\"",
                ).unindent()
                .add("})\n")
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

private fun OperationParameterSpec.toStringSuffix(): String =
    when {
        isEnum -> {
            ".serialName()"
        }

        type.isString -> {
            ""
        }

        type is DomainTypeSpec.ListTypeSpec || type is DomainTypeSpec.SetTypeSpec -> {
            ".joinToString(\",\")${if (isEnumArray) " { it.serialName() }" else ""}"
        }

        else -> {
            ".toString()"
        }
    }
