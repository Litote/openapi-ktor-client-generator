package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Operation
import community.flock.kotlinx.openapi.bindings.OpenAPIV3ParameterLocation
import community.flock.kotlinx.openapi.bindings.OpenAPIV3RequestBody
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Response
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import kotlinx.serialization.Serializable
import org.litote.openapi.ktor.client.generator.shared.capitalize
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase
import org.litote.openapi.ktor.client.generator.shared.tagToCamelCase
import org.litote.openapi.ktor.client.generator.shared.uncapitalize
import java.io.File

public class ApiClientGenerator internal constructor(public val apiModel: ApiModel) {

    private companion object {
        val bodyMember = MemberName("io.ktor.client.call", "body")
        val setBodyMember = MemberName("io.ktor.client.request", "setBody")
        val contentTypeMember = MemberName("io.ktor.http", "contentType")
        val headerMember = MemberName("io.ktor.client.request", "header")
        const val ALIAS_HEADER = "setHeader"
        val contentTypeClass = ClassName("io.ktor.http", "ContentType")
        val serializableAnnotation = AnnotationSpec.builder(Serializable::class).build()

        private val logger = io.github.oshai.kotlinlogging.KotlinLogging.logger {}
    }

    public val clientConfigurationClass: ClassName =
        ClassName(apiModel.configuration.clientPackage, "ClientConfiguration")
    public val clientConfigurationCompanionClass: ClassName =
        ClassName(apiModel.configuration.clientPackage, "ClientConfiguration", "Companion")

    private fun clientNameForTag(tag: String): String {
        val tagName = tag.tagToCamelCase()
        val clientBaseName = tagName.removeSuffix("Controller")
        return "${clientBaseName}Client"
    }

    private fun buildResponseEntries(
        operation: OpenAPIV3Operation,
        clientBuilder: TypeSpec.Builder,
        responseBaseName: String,
        responseSealedClass: ClassName
    ): List<ResponseEntry> =
        (
                operation
                    .responses
                    ?.asSequence()
                    ?.map { (statusCode, responseOrReference) ->
                        val code = statusCode.value.toIntOrNull() ?: error("Invalid status code: ${statusCode.value}")
                        val response = responseOrReference as? OpenAPIV3Response
                            ?: error("Unsupported response reference: $responseOrReference")
                        val schema =
                            response.content?.values?.firstOrNull()?.schema
                        if ((response.content?.size ?: 0) > 1) {
                            logger.warn { "More than one response content specified - unsupported - take first" }
                        }
                        val responseType = if (schema == null) null else apiModel.getClassName(
                            "${responseBaseName}ResponseBody",
                            schema
                        )
                        code to responseType
                    }
                    ?.sortedBy { it.first }
                    ?.groupBy({ it.second to it.first.isSuccess }) { it.first }
                    ?.map { (type: Pair<TypeName?, Boolean>, statusCodes) ->
                        Triple(
                            type.first,
                            type.second,
                            statusCodes
                        )
                    }
                    ?.run {
                        mapIndexed { index, (typeName, success, statusCodes) ->
                            val classNameSuffix = if (success) {
                                if (getOrNull(index + 1)?.second == true) "Success${statusCodes.first()}" else "Success"
                            } else if (getOrNull(index + 1) != null) {
                                "Failure${statusCodes.first()}"
                            } else {
                                "Failure"
                            }
                            val responseClassName = "${responseBaseName}Response$classNameSuffix"
                            val responseType = if (typeName == null) {
                                TypeSpec
                                    .objectBuilder(responseClassName)
                                    .addAnnotation(serializableAnnotation)
                                    .superclass(responseSealedClass)
                                    .build()
                            } else {
                                TypeSpec.classBuilder(responseClassName)
                                    .addModifiers(KModifier.DATA)
                                    .addAnnotation(serializableAnnotation)
                                    .primaryConstructor(
                                        FunSpec.constructorBuilder()
                                            .addParameter("body", typeName)
                                            .build()
                                    )
                                    .addProperty(
                                        PropertySpec.builder("body", typeName)
                                            .initializer("body")
                                            .build()
                                    )
                                    .superclass(responseSealedClass)
                                    .build()
                            }

                            clientBuilder.addType(responseType)
                            ResponseEntry(statusCodes, typeName, responseType)
                        }
                    }
                    ?.takeUnless { it.isEmpty() }
                    ?: error("no response specified"))
            .apply {

                clientBuilder.addType(
                    TypeSpec
                        .classBuilder("${responseBaseName}ResponseUnknownFailure")
                        .addModifiers(KModifier.DATA)
                        .addAnnotation(serializableAnnotation)
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter("statusCode", INT)
                                .build()
                        )
                        .addProperty(
                            PropertySpec.builder("statusCode", INT)
                                .initializer("statusCode")
                                .build()
                        )
                        .superclass(responseSealedClass)
                        .build()
                )
            }

    private fun getHeaderParameters(operation: OpenAPIV3Operation): List<HeaderParameter> =
        operation
            .parameters
            .orEmpty()
            .asSequence()
            .mapNotNull { apiModel.getComponentParameter(it) }
            .filter { it.`in` == OpenAPIV3ParameterLocation.HEADER }
            .distinctBy { it.name }
            .map { parameter ->
                val parameterTypeName = parameter.schema?.let { schema ->
                    apiModel.getClassName(parameterTypeBaseName(parameter.name), schema)
                } ?: STRING
                val defaultLiteral = parameterDefaultLiteral(parameter.schema, parameterTypeName)
                val isOptional = parameter.required != true && defaultLiteral == null
                val parameterType =
                    if (isOptional) parameterTypeName.copy(nullable = true) else parameterTypeName
                val constBaseName = constName(parameter.name)
                val parameterName = parameterVariableName(parameter.name)
                val defaultConst = if (defaultLiteral != null && isConstSupported(parameterTypeName)) {
                    "PARAMETER_${constBaseName}_DEFAULT_VALUE"
                } else {
                    null
                }
                HeaderParameter(
                    parameterName,
                    parameterType,
                    "PARAMETER_$constBaseName",
                    defaultConst,
                    isOptional
                )
            }
            .toList()

    private fun getPathParameters(operation: OpenAPIV3Operation): List<PathParameter> =
        operation
            .parameters
            .orEmpty()
            .asSequence()
            .mapNotNull { apiModel.getComponentParameter(it) }
            .filter { it.`in` == OpenAPIV3ParameterLocation.PATH }
            .distinctBy { it.name }
            .map { parameter ->
                val parameterTypeName = parameter.schema?.let { schema ->
                    apiModel.getClassName(parameterTypeBaseName(parameter.name), schema)
                } ?: STRING
                val defaultLiteral = parameterDefaultLiteral(parameter.schema, parameterTypeName)
                val isOptional = parameter.required != true && defaultLiteral == null
                val parameterType =
                    if (isOptional) parameterTypeName.copy(nullable = true) else parameterTypeName
                val parameterName = parameterVariableName(parameter.name)
                PathParameter(
                    parameterName,
                    parameterType,
                    parameter.name,
                    isOptional
                )
            }
            .toList()

    private fun getQueryParameters(operation: OpenAPIV3Operation): List<QueryParameter> =
        operation
            .parameters
            .orEmpty()
            .asSequence()
            .mapNotNull { apiModel.getComponentParameter(it) }
            .filter { it.`in` == OpenAPIV3ParameterLocation.QUERY }
            .distinctBy { it.name }
            .map { parameter ->
                val parameterTypeName = parameter.schema?.let { schema ->
                    apiModel.getClassName(parameterTypeBaseName(parameter.name), schema)
                } ?: STRING
                val defaultLiteral = parameterDefaultLiteral(parameter.schema, parameterTypeName)
                val isOptional = parameter.required != true && defaultLiteral == null
                val parameterType =
                    if (isOptional) parameterTypeName.copy(nullable = true) else parameterTypeName
                val parameterName = parameterVariableName(parameter.name)
                QueryParameter(
                    parameterName,
                    parameterType,
                    parameter.name,
                    isOptional
                )
            }
            .toList()

    private fun buildOperation(
        context: ClientGenerationContext,
        operationInfo: ApiOperation,
        clientBuilder: TypeSpec.Builder,
        clientName: String
    ) {
        val operation = operationInfo.operation
        val operationId = operation.operationId
            ?: "${operationInfo.method}_${operationInfo.path.replace("/", "_").replace("{", "With_").replace("}", "")}"
        val responseBaseName = operationId.replace("-", "_").snakeToCamelCase().capitalize()
        val functionName = responseBaseName.uncapitalize()

        val requestBody = operation.requestBody as? OpenAPIV3RequestBody

        val requestSchema = requestBody
            ?.content
            ?.values
            ?.firstOrNull()
            ?.schema
        val requestType =
            requestSchema?.let { apiModel.getClassName("${responseBaseName}Request", it) }

        val responseSealedName = "${responseBaseName}Response"
        val packageName = apiModel.configuration.clientPackage
        val responseSealedClass = ClassName(packageName, clientName, responseSealedName)
        val responseSealedType = TypeSpec.classBuilder(responseSealedName)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(serializableAnnotation)
            .build()

        clientBuilder.addType(responseSealedType)

        val responseEntries: List<ResponseEntry> =
            buildResponseEntries(operation, clientBuilder, responseBaseName, responseSealedClass)

        val methodMember = MemberName("io.ktor.client.request", operationInfo.method)
        val funBuilder = FunSpec.builder(functionName)
            .addModifiers(KModifier.SUSPEND)
            .returns(responseSealedClass)

        operation.summary?.let { summary ->
            funBuilder.addKdoc("%L\n", summary)
        }

        val requestContentTypes = requestBody
            ?.content
            ?.keys
            ?.map { it.value }
            ?.toSet()


        requestType?.let { type ->
            funBuilder.addParameter("request", type)
        }

        val pathParameters = getPathParameters(operation)
        if (!context.hasPathComponents && pathParameters.isNotEmpty()) {
            context.hasPathComponents = true
        }
        pathParameters.forEach { pathParameter ->
            val pathBuilder = ParameterSpec.builder(
                pathParameter.parameterName,
                pathParameter.parameterType
            )
            if (pathParameter.isOptional) {
                pathBuilder.defaultValue("null")
            }
            funBuilder.addParameter(pathBuilder.build())
        }

        val queryParameters = getQueryParameters(operation)
        queryParameters.forEach { queryParameter ->
            val queryBuilder = ParameterSpec.builder(
                queryParameter.parameterName,
                queryParameter.parameterType
            )
            if (queryParameter.isOptional) {
                queryBuilder.defaultValue("null")
            }
            funBuilder.addParameter(queryBuilder.build())
        }

        val headerParameters = getHeaderParameters(operation)
        if (!context.hasHeaders && headerParameters.isNotEmpty()) {
            context.hasHeaders = true
        }

        headerParameters.forEach { headerParameter ->
            val parameterBuilder = ParameterSpec.builder(
                headerParameter.parameterName,
                headerParameter.parameterType
            )
            when {
                headerParameter.defaultValueConst != null -> {
                    parameterBuilder.defaultValue(
                        "%T.%L",
                        clientConfigurationClass,
                        headerParameter.defaultValueConst
                    )
                }

                headerParameter.isOptional -> {
                    parameterBuilder.defaultValue("null")
                }
            }
            funBuilder.addParameter(parameterBuilder.build())
        }

        val hasJsonContentType =
            requestContentTypes?.any { it.equals("application/json", ignoreCase = true) } == true
        val trimmedPath = operationInfo.path.trimStart('/').run {
            var s = "\"$this\""
            pathParameters.forEach { pathParameter ->
                if (pathParameter.isOptional) {
                    s += ".replace(\"/{${pathParameter.pathName}}\", if(${pathParameter.parameterName} == null) \"\" else \"/\${${pathParameter.parameterName}.encodeURLPathPart()}\")"
                } else {
                    s += ".replace(\"/{${pathParameter.pathName}}\", \"/\${${pathParameter.parameterName}.encodeURLPathPart()}\")"
                }
            }
            s
        }
        funBuilder.addCode(
            CodeBlock.builder()
                .beginControlFlow("try")
                .beginControlFlow("val response = configuration.client.%M(%L)", methodMember, trimmedPath)
                .apply {
                    headerParameters.forEach { headerParameter ->
                        if (headerParameter.isOptional) {
                            beginControlFlow("if (%N != null)", headerParameter.parameterName)
                            addStatement(
                                "$ALIAS_HEADER(%T.%L, %N)",
                                clientConfigurationClass,
                                headerParameter.nameConst,
                                headerParameter.parameterName
                            )
                            endControlFlow()
                        } else {
                            addStatement(
                                "$ALIAS_HEADER(%T.%L, %N)",
                                clientConfigurationClass,
                                headerParameter.nameConst,
                                headerParameter.parameterName
                            )
                        }
                    }
                    if (queryParameters.isNotEmpty()) {
                        beginControlFlow("url")
                        queryParameters.forEach { queryParameter ->
                            if (queryParameter.isOptional) {
                                beginControlFlow("if (%N != null)", queryParameter.parameterName)
                                addStatement(
                                    "parameters.append(%S, %N.toString())",
                                    queryParameter.queryKey,
                                    queryParameter.parameterName
                                )
                                endControlFlow()
                            } else {
                                addStatement(
                                    "parameters.append(%S, %N.toString())",
                                    queryParameter.queryKey,
                                    queryParameter.parameterName
                                )
                            }
                        }

                        endControlFlow()
                    }

                    if (requestType != null) {
                        addStatement("%M(%N)", setBodyMember, "request")
                        if (hasJsonContentType) {
                            addStatement("%M(%T.Application.Json)", contentTypeMember, contentTypeClass)
                        }
                    }
                }
                .endControlFlow()
                .beginControlFlow("return when (response.status.value)")
                .apply {
                    responseEntries.forEach { (statusCodes, bodyType, type) ->
                        val codesLiteral = statusCodes.joinToString()

                        if (bodyType == null) {
                            addStatement(
                                "%L -> %N",
                                codesLiteral,
                                type
                            )
                        } else {
                            addStatement(
                                "%L -> %N(response.%M<%T>())",
                                codesLiteral,
                                type,
                                bodyMember,
                                bodyType
                            )
                        }
                    }
                    addStatement(
                        "else -> %L(%L)",
                        "${responseBaseName}ResponseUnknownFailure",
                        "response.status.value"
                    )
                }
                .endControlFlow()
                .endControlFlow()
                .beginControlFlow("catch(e: Exception)")
                .addStatement("%L(%L)", "configuration.exceptionLogger", "e")
                .addStatement(
                    "return %L(%L)",
                    "${responseBaseName}ResponseUnknownFailure",
                    InternalServerError.value
                )
                .endControlFlow()
                .build()
        )

        clientBuilder.addFunction(funBuilder.build())
    }

    internal fun buildClient(context: ClientGenerationContext): ClientFileContext {
        val clientName = clientNameForTag(context.name)

        val clientBuilder = TypeSpec.classBuilder(clientName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        ParameterSpec.builder("configuration", clientConfigurationClass)
                            .defaultValue(
                                "%M",
                                MemberName(clientConfigurationCompanionClass, "defaultClientConfiguration")
                            )
                            .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("configuration", clientConfigurationClass)
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("configuration")
                    .build()
            )

        context.operations.forEach { operationInfo ->
            buildOperation(context, operationInfo, clientBuilder, clientName)
        }

        return ClientFileContext(context, clientBuilder.build())
    }

    internal fun writeFile(context: ClientFileContext) {
        val clientName = clientNameForTag(context.name)
        val fileSpec = FileSpec.builder(apiModel.configuration.clientPackage, clientName)
            .apply {
                if (context.hasHeaders) {
                    addAliasedImport(headerMember, ALIAS_HEADER)
                }
                if (context.hasPathComponents) {
                    addImport("io.ktor.http", "encodeURLPathPart")
                }
            }
            .addType(context.clientClass)
            .build()
        val basePath = File(apiModel.outputDirectory).resolve("src/main/kotlin")
        println("Writing $clientName to $basePath")
        fileSpec.writeTo(basePath)
    }

}

private data class ResponseEntry(
    val statusCodes: List<Int>,
    val bodyType: TypeName?,
    val type: TypeSpec

) {
    val isSuccess: Boolean get() = statusCodes.any { it.isSuccess }
}

private val Int.isSuccess: Boolean get() = this in 200 until 300

private data class HeaderParameter(
    val parameterName: String,
    val parameterType: TypeName,
    val nameConst: String,
    val defaultValueConst: String?,
    val isOptional: Boolean
)

private data class PathParameter(
    val parameterName: String,
    val parameterType: TypeName,
    val pathName: String,
    val isOptional: Boolean
)

private data class QueryParameter(
    val parameterName: String,
    val parameterType: TypeName,
    val queryKey: String,
    val isOptional: Boolean
)

internal data class ClientGenerationContext private constructor(
    val name: String,
    val operations: List<ApiOperation>,
    var hasHeaders: Boolean = false,
    var hasPathComponents: Boolean = false,
) {
    constructor(name: String, operations: List<ApiOperation>) : this(
        name,
        operations,
        false
    )
}

internal data class ClientFileContext private constructor(
    val name: String,
    val operations: List<ApiOperation>,
    val hasHeaders: Boolean,
    val hasPathComponents: Boolean,
    val clientClass: TypeSpec,
) {
    constructor(generationContext: ClientGenerationContext, clientClass: TypeSpec) :
            this(
                generationContext.name,
                generationContext.operations,
                generationContext.hasHeaders,
                generationContext.hasPathComponents,
                clientClass
            )
}
