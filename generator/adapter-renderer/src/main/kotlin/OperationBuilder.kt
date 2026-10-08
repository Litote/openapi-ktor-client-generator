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
import org.litote.openapi.ktor.client.generator.domain.ParameterStyleSpec
import org.litote.openapi.ktor.client.generator.domain.RequestBodySpec
import org.litote.openapi.ktor.client.generator.port.StringFormatType
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
    private val stringFormatTypes: Map<String, StringFormatType> = emptyMap(),
) {
    private data class OperationParameters(
        val pathParameters: List<OperationParameterSpec>,
        val queryParameters: List<OperationParameterSpec>,
        val headerParameters: List<OperationParameterSpec>,
        val cookieParameters: List<OperationParameterSpec>,
        val trimmedPath: String,
    )

    private data class RequestBodyContext(
        val requestBody: RequestBodySpec?,
        val hasJsonContentType: Boolean,
        val hasYamlContentType: Boolean,
    )

    private data class ResponseBuildContext(
        val sealedClass: ClassName,
        val entries: List<RenderedResponseEntry>,
        val baseName: String,
        val acceptContentTypes: List<String>,
    ) {
        val unknownFailureClass: ClassName get() = sealedClass.peerClass("${baseName}ResponseUnknownFailure")
    }

    private companion object {
        private const val KTOR_HTTP = "io.ktor.http"
        private const val KTOR_REQUEST = "io.ktor.client.request"
        private const val KTOR_FORMS = "io.ktor.client.request.forms"
        private const val IF_NOT_NULL = "if (%N != null)"

        val bodyMember = MemberName("io.ktor.client.call", "body")
        val setBodyMember = MemberName(KTOR_REQUEST, "setBody")
        val contentTypeMember = MemberName(KTOR_HTTP, "contentType")
        val acceptMember = MemberName(KTOR_REQUEST, "accept")
        val contentTypeClass = ClassName(KTOR_HTTP, "ContentType")
        val formDataMember = MemberName(KTOR_FORMS, "formData")
        val formDataContentClass = ClassName(KTOR_FORMS, "FormDataContent")
        val multiPartFormDataContentClass = ClassName(KTOR_FORMS, "MultiPartFormDataContent")
        val parametersClass = ClassName(KTOR_HTTP, "Parameters")
        val headersClass = ClassName(KTOR_HTTP, "Headers")
        val httpHeadersClass = ClassName(KTOR_HTTP, "HttpHeaders")
        val sseMember = MemberName("io.ktor.client.plugins.sse", "sse")
        val clientSseSessionClass = ClassName("io.ktor.client.plugins.sse", "ClientSSESession")
        val cancellationExceptionClass = ClassName("kotlin.coroutines.cancellation", "CancellationException")
        const val ALIAS_HEADER = "setHeader"
    }

    /**
     * Builds an operation: its types and abstract declaration are added to the client interface,
     * its implementation to the client class.
     */
    fun buildOperation(
        context: ClientGenerationContext,
        operationInfo: OperationSpec,
        interfaceBuilder: TypeSpec.Builder,
        clientBuilder: TypeSpec.Builder,
        clientName: String,
    ) {
        val responseBaseName = operationInfo.name
        val functionName = responseBaseName.uncapitalize()

        // Request body - build form type and add to client
        val requestBody = operationInfo.requestBody
        requestBody?.let {
            if (it.isMultipartFormData || it.isUrlEncodedForm) {
                buildFormBodyDefinition(it, responseBaseName, interfaceBuilder)
            }
        }

        // Inline models (e.g. inline request body objects)
        operationInfo.inlineModels.forEach { modelSpec ->
            modelGenerator.buildModel(modelSpec)?.let { interfaceBuilder.addType(it) }
        }

        val parameters = operationInfo.parameters
        val pathParameters = parameters.filter { it.isPath }
        val queryParameters = parameters.filter { it.isQuery }
        val headerParameters = parameters.filter { it.isHeader }
        val cookieParameters = parameters.filter { it.isCookie }

        if (pathParameters.isNotEmpty()) context.hasPathComponents = true
        if (headerParameters.isNotEmpty()) context.hasHeaders = true
        parameters.forEach { param -> param.serializationHelper()?.let { context.parameterHelpers.add(it) } }

        val trimmedPath = buildPathExpression(operationInfo.path, pathParameters)
        val operationParams =
            OperationParameters(
                pathParameters = pathParameters,
                queryParameters = queryParameters,
                headerParameters = headerParameters,
                cookieParameters = cookieParameters,
                trimmedPath = trimmedPath,
            )

        val interfaceClass = ClassName(clientPackage, clientName)
        if (operationInfo.isSse) {
            context.hasSseOperations = true
            buildSseOperation(
                operationInfo = operationInfo,
                interfaceBuilder = interfaceBuilder,
                interfaceClass = interfaceClass,
                clientBuilder = clientBuilder,
                functionName = functionName,
                params = operationParams,
            )
            return
        }

        val responseSealedName = "${responseBaseName}Response"
        val responseSealedClass = interfaceClass.nestedClass(responseSealedName)
        interfaceBuilder.addType(responseBuilder.createSealedResponseClass(responseSealedName))
        val responseEntries =
            responseBuilder.buildResponseTypes(
                operationInfo.responses,
                interfaceBuilder,
                responseBaseName,
                responseSealedClass,
                modelPackage,
                modelPackageOverrides,
            )

        val methodMember = MemberName(KTOR_REQUEST, operationInfo.method)
        val funBuilder =
            declareOperation(interfaceBuilder, interfaceClass, operationInfo, functionName, operationParams) {
                returns(responseSealedClass)
            }

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
                        sealedClass = responseSealedClass,
                        entries = responseEntries,
                        baseName = responseBaseName,
                        acceptContentTypes =
                            operationInfo.responses
                                .filter { it.isSuccess }
                                .flatMap { it.contentTypes }
                                .distinct(),
                    ),
            ),
        )

        clientBuilder.addFunction(funBuilder.build())
    }

    private fun buildFormBodyDefinition(
        requestBody: RequestBodySpec,
        responseBaseName: String,
        interfaceBuilder: TypeSpec.Builder,
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
                                val fieldTypeName = field.type.toTypeName(modelPackage, modelPackageOverrides, stringFormatTypes)
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
                        val fieldTypeName = field.type.toTypeName(modelPackage, modelPackageOverrides, stringFormatTypes)
                        addProperty(
                            PropertySpec
                                .builder(field.parameterName, fieldTypeName)
                                .initializer(field.parameterName)
                                .build(),
                        )
                    }
                }.build()

        interfaceBuilder.addType(typeSpec)
        fileTypeSpec?.let { interfaceBuilder.addType(it) }
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

    /**
     * Adds the abstract declaration of the operation to the client interface, with the KDoc and the default values,
     * and returns the builder of its override, whose parameters have no default values.
     */
    private fun declareOperation(
        interfaceBuilder: TypeSpec.Builder,
        interfaceClass: ClassName,
        operationInfo: OperationSpec,
        functionName: String,
        params: OperationParameters,
        configure: FunSpec.Builder.() -> Unit,
    ): FunSpec.Builder {
        fun signature(withDefaults: Boolean): FunSpec.Builder =
            FunSpec
                .builder(functionName)
                .addModifiers(KModifier.SUSPEND)
                .apply {
                    operationInfo.requestBody?.let {
                        val requestTypeName = it.type.toTypeName(modelPackage, modelPackageOverrides, stringFormatTypes, interfaceClass)
                        addParameter(it.parameterName, requestTypeName)
                    }
                    addParameters(this, params.pathParameters, withDefaults, interfaceClass)
                    addParameters(this, params.queryParameters, withDefaults, interfaceClass)
                    addParameters(this, params.headerParameters, withDefaults, interfaceClass)
                    addParameters(this, params.cookieParameters, withDefaults, interfaceClass)
                    configure()
                }

        interfaceBuilder.addFunction(
            signature(withDefaults = true)
                .addModifiers(KModifier.ABSTRACT)
                .apply { operationInfo.summary?.let { addKdoc("%L\n", it) } }
                .build(),
        )
        return signature(withDefaults = false).addModifiers(KModifier.OVERRIDE)
    }

    private fun buildSseOperation(
        operationInfo: OperationSpec,
        interfaceBuilder: TypeSpec.Builder,
        interfaceClass: ClassName,
        clientBuilder: TypeSpec.Builder,
        functionName: String,
        params: OperationParameters,
    ) {
        val blockType =
            LambdaTypeName
                .get(
                    receiver = clientSseSessionClass,
                    returnType = UNIT,
                ).copy(suspending = true)

        val funBuilder =
            declareOperation(interfaceBuilder, interfaceClass, operationInfo, functionName, params) {
                addParameter(ParameterSpec.builder("block", blockType).build())
            }

        funBuilder.addCode(buildSseFunctionBody(params))

        clientBuilder.addFunction(funBuilder.build())
    }

    private fun buildSseFunctionBody(params: OperationParameters): CodeBlock {
        val trimmedPath = params.trimmedPath
        val hasRequestConfig =
            params.headerParameters.isNotEmpty() || params.queryParameters.isNotEmpty() || params.cookieParameters.isNotEmpty()
        val builder = CodeBlock.builder()
        builder.beginControlFlow("try")

        if (hasRequestConfig) {
            builder.beginControlFlow(
                "configuration.client.%M(urlString = %L, request = {",
                sseMember,
                trimmedPath,
            )
            addRegularHeaderParams(builder, params.headerParameters)
            addCookieParams(builder, params.cookieParameters)
            addQueryParams(builder, params.queryParameters)
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
        addCancellationRethrow(builder)
        builder.beginControlFlow("catch(e: Exception)")
        builder.addStatement("%L(%L)", "configuration.exceptionLogger", "e")
        builder.endControlFlow()
        return builder.build()
    }

    /**
     * Rethrows [kotlin.coroutines.cancellation.CancellationException] so that coroutine cancellation
     * is not swallowed by the generic exception handler.
     */
    private fun addCancellationRethrow(builder: CodeBlock.Builder) {
        builder.beginControlFlow("catch(e: %T)", cancellationExceptionClass)
        builder.addStatement("throw e")
        builder.endControlFlow()
    }

    private fun addQueryParams(
        builder: CodeBlock.Builder,
        queryParameters: List<OperationParameterSpec>,
    ) {
        if (queryParameters.isEmpty()) return
        builder.beginControlFlow("url")
        queryParameters.forEach { param ->
            addIfNotNull(builder, param) { builder.add(queryStatement(param)) }
        }
        builder.endControlFlow()
    }

    private fun queryStatement(param: OperationParameterSpec): CodeBlock {
        val name = param.originalName
        return when {
            param.isObject -> {
                when {
                    param.style == ParameterStyleSpec.DEEP_OBJECT -> {
                        CodeBlock.of("parameters.$APPEND_DEEP_OBJECT(%S, %L)\n", name, param.jsonElementCode())
                    }

                    param.style == ParameterStyleSpec.FORM && param.explode -> {
                        CodeBlock.of("parameters.$APPEND_EXPLODED_OBJECT(%L)\n", param.jsonObjectCode())
                    }

                    else -> {
                        CodeBlock.of("parameters.append(%S, %L)\n", name, param.delimitedObjectCode(param.style.delimiter()))
                    }
                }
            }

            param.isArray && param.style == ParameterStyleSpec.FORM && param.explode -> {
                CodeBlock.of("parameters.appendAll(%S, %N%L)\n", name, param.camelCaseName, param.elementMapping())
            }

            param.isArray -> {
                CodeBlock.of("parameters.append(%S, %L)\n", name, param.joinedArrayCode(param.style.delimiter()))
            }

            else -> {
                CodeBlock.of("parameters.append(%S, %N${param.toStringSuffix(stringFormatTypes)})\n", name, param.camelCaseName)
            }
        }
    }

    private fun addCookieParams(
        builder: CodeBlock.Builder,
        cookieParameters: List<OperationParameterSpec>,
    ) {
        cookieParameters.forEach { param ->
            val value =
                when {
                    param.isObject -> param.delimitedObjectCode(",")
                    param.isArray -> param.joinedArrayCode(",")
                    else -> CodeBlock.of("%N${param.toStringSuffix(stringFormatTypes)}", param.camelCaseName)
                }
            addIfNotNull(builder, param) {
                builder.addStatement("%M(%L, %L)", cookieMember, parameterKey(param), value)
            }
        }
    }

    private fun addIfNotNull(
        builder: CodeBlock.Builder,
        param: OperationParameterSpec,
        addCode: () -> Unit,
    ) {
        if (param.isOptional) {
            builder.beginControlFlow(IF_NOT_NULL, param.camelCaseName)
            addCode()
            builder.endControlFlow()
        } else {
            addCode()
        }
    }

    private fun parameterKey(param: OperationParameterSpec): CodeBlock =
        if (param.constName != null) {
            CodeBlock.of("%T.%L", clientConfigurationClass, param.constName)
        } else {
            CodeBlock.of("%S", param.originalName)
        }

    /** Mapping appended to an array parameter to get strings, e.g. `.map { it.serialName() }`. */
    private fun OperationParameterSpec.elementMapping(): String {
        val element =
            when (val arrayType = type) {
                is DomainTypeSpec.ListTypeSpec -> arrayType.element
                is DomainTypeSpec.SetTypeSpec -> arrayType.element
                else -> return ""
            }
        val formatType = element.stringFormatType(stringFormatTypes)
        return when {
            isEnumArray -> ".map { it.serialName() }"
            formatType != null -> ".map { value -> value${formatType.formatSuffix} }"
            element.isString -> ""
            else -> ".map { it.toString() }"
        }
    }

    private fun OperationParameterSpec.joinedArrayCode(delimiter: String): CodeBlock =
        CodeBlock.of("%N.joinToString(%S)${joinTransform(stringFormatTypes)}", camelCaseName, delimiter)

    private fun OperationParameterSpec.jsonElementCode(): CodeBlock =
        CodeBlock.of("configuration.json.%M(%N)", encodeToJsonElementMember, camelCaseName)

    private fun OperationParameterSpec.jsonObjectCode(): CodeBlock = CodeBlock.of("%L.%M", jsonElementCode(), jsonObjectMember)

    private fun OperationParameterSpec.delimitedObjectCode(
        separator: String,
        keyValueSeparator: String = separator,
    ): CodeBlock =
        if (keyValueSeparator == separator) {
            CodeBlock.of("%L.$TO_DELIMITED_STRING(%S)", jsonObjectCode(), separator)
        } else {
            CodeBlock.of("%L.$TO_DELIMITED_STRING(%S, %S)", jsonObjectCode(), separator, keyValueSeparator)
        }

    private fun addParameters(
        funBuilder: FunSpec.Builder,
        parameters: List<OperationParameterSpec>,
        withDefaults: Boolean,
        interfaceClass: ClassName,
    ) {
        parameters.forEach { param ->
            val paramTypeName = param.type.toTypeName(modelPackage, modelPackageOverrides, stringFormatTypes, interfaceClass)
            val builder = ParameterSpec.builder(param.camelCaseName, paramTypeName)
            when {
                !withDefaults -> {
                    // Overrides cannot redeclare default values: they are inherited from the interface.
                }

                param.constDefaultName != null -> {
                    builder.defaultValue(
                        "%T.%L",
                        clientConfigurationClass,
                        param.constDefaultName,
                    )
                }

                param.defaultValue != null -> {
                    param.defaultValue?.let { builder.defaultValue(it.toCodeBlock(param.type.stringFormatType(stringFormatTypes))) }
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
            val suffix = param.toStringSuffix(stringFormatTypes)
            result +=
                if (param.isOptional) {
                    ".replace(\"/{${param.originalName}}\", if(${param.camelCaseName} == null) \"\" else \"/\${${param.camelCaseName}$suffix.encodeURLPathPart()}\")"
                } else {
                    ".replace(\"/{${param.originalName}}\", \"/\${${param.camelCaseName}$suffix.encodeURLPathPart()}\")"
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
        val builder = CodeBlock.builder()
        builder.beginControlFlow("try")
        builder.beginControlFlow("val response = configuration.client.%M(%L)", methodMember, operationParams.trimmedPath)
        responseCtx.acceptContentTypes.forEach { contentType ->
            builder.addStatement("%M(%T.parse(%S))", acceptMember, contentTypeClass, contentType)
        }
        addRegularHeaderParams(builder, operationParams.headerParameters)
        addCookieParams(builder, operationParams.cookieParameters)
        addQueryParams(builder, operationParams.queryParameters)
        addRequestBodyCode(builder, requestBodyCtx)
        builder.endControlFlow()
        builder.beginControlFlow("return when (response.status.value)")
        addResponseCases(builder, responseCtx)
        builder.endControlFlow()
        builder.endControlFlow()
        addCancellationRethrow(builder)
        builder.beginControlFlow("catch(e: Exception)")
        builder.addStatement("%L(%L)", "configuration.exceptionLogger", "e")
        builder.addStatement("return %T(%L)", responseCtx.unknownFailureClass, InternalServerError.value)
        builder.endControlFlow()
        return builder.build()
    }

    private fun addRegularHeaderParams(
        builder: CodeBlock.Builder,
        headerParameters: List<OperationParameterSpec>,
    ) {
        headerParameters.forEach { param ->
            val value =
                when {
                    param.isObject -> param.delimitedObjectCode(",", if (param.explode) "=" else ",")

                    param.isArray -> param.joinedArrayCode(",")

                    param.isEnum -> CodeBlock.of("%N.serialName()", param.camelCaseName)

                    param.type.stringFormatType(
                        stringFormatTypes,
                    ) != null -> CodeBlock.of("%N${param.toStringSuffix(stringFormatTypes)}", param.camelCaseName)

                    else -> CodeBlock.of("%N", param.camelCaseName)
                }
            addIfNotNull(builder, param) {
                builder.addStatement("$ALIAS_HEADER(%L, %L)", parameterKey(param), value)
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
    ) {
        responseCtx.entries.forEach { entry ->
            val codesLiteral = entry.statusCodes.joinToString()
            if (entry.bodyTypeName == null) {
                builder.addStatement("%L -> %T(response.headers)", codesLiteral, entry.className)
            } else {
                builder.addStatement(
                    "%L -> %T(response.%M<%T>(), response.headers)",
                    codesLiteral,
                    entry.className,
                    bodyMember,
                    entry.bodyTypeName,
                )
            }
        }
        builder.addStatement("else -> %T(response.status.value, response.headers)", responseCtx.unknownFailureClass)
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
            val typeName = field.type.toTypeName(modelPackage, modelPackageOverrides, stringFormatTypes)
            if (typeName.isString()) {
                builder.addStatement("append(%S, %L)", field.originalName, valueReference)
            } else {
                builder.addStatement("append(%S, %L%L)", field.originalName, valueReference, field.type.formatSuffix())
            }
        }
    }

    /** Suffix formatting a non-`String` value of this type, e.g. `.toString()`. */
    private fun DomainTypeSpec.formatSuffix(): String =
        stringFormatType(stringFormatTypes)?.formatSuffix ?: StringFormatType.DEFAULT_FORMAT_SUFFIX

    private fun buildUrlEncodedFormData(requestBody: RequestBodySpec): CodeBlock {
        val builder = CodeBlock.builder()
        requestBody.formFields.forEach { field ->
            val fieldAccess = "${requestBody.parameterName}.${field.parameterName}"
            if (field.isOptional) {
                builder.beginControlFlow("%L?.let { value ->", fieldAccess)
                builder.addStatement("append(%S, value%L)", field.originalName, field.type.formatSuffix())
                builder.endControlFlow()
            } else {
                builder.addStatement("append(%S, %L%L)", field.originalName, fieldAccess, field.type.formatSuffix())
            }
        }
        return builder.build()
    }
}

private val OperationParameterSpec.isArray: Boolean
    get() = type is DomainTypeSpec.ListTypeSpec || type is DomainTypeSpec.SetTypeSpec

/** Delimiter of unexploded query arrays and objects for this style. */
private fun ParameterStyleSpec.delimiter(): String =
    when (this) {
        ParameterStyleSpec.SPACE_DELIMITED -> " "
        ParameterStyleSpec.PIPE_DELIMITED -> "|"
        else -> ","
    }

/** The runtime helper needed to serialize this object parameter, if any. */
private fun OperationParameterSpec.serializationHelper(): ParameterSerializationHelper? =
    when {
        !isObject || isPath -> null
        isQuery && style == ParameterStyleSpec.DEEP_OBJECT -> ParameterSerializationHelper.DEEP_OBJECT
        isQuery && style == ParameterStyleSpec.FORM && explode -> ParameterSerializationHelper.EXPLODED_OBJECT
        else -> ParameterSerializationHelper.DELIMITED_OBJECT
    }

private fun OperationParameterSpec.toStringSuffix(stringFormatTypes: Map<String, StringFormatType>): String =
    when {
        isEnum -> {
            ".serialName()"
        }

        type.isString && type.stringFormatType(stringFormatTypes) == null -> {
            ""
        }

        type is DomainTypeSpec.ListTypeSpec || type is DomainTypeSpec.SetTypeSpec -> {
            ".joinToString(\",\")${joinTransform(stringFormatTypes)}"
        }

        else -> {
            type.stringFormatType(stringFormatTypes)?.formatSuffix ?: StringFormatType.DEFAULT_FORMAT_SUFFIX
        }
    }

/** Trailing lambda of `joinToString` for an array parameter, e.g. ` { it.serialName() }`, or empty when `toString()` is enough. */
private fun OperationParameterSpec.joinTransform(stringFormatTypes: Map<String, StringFormatType>): String {
    val element =
        when (val arrayType = type) {
            is DomainTypeSpec.ListTypeSpec -> arrayType.element
            is DomainTypeSpec.SetTypeSpec -> arrayType.element
            else -> null
        }
    return when {
        isEnumArray -> " { it.serialName() }"
        else -> element?.stringFormatType(stringFormatTypes)?.let { " { value -> value${it.formatSuffix} }" }.orEmpty()
    }
}
