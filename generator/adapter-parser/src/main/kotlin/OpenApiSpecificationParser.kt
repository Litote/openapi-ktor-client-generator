package org.litote.openapi.ktor.client.generator.adapter.parser

import com.squareup.kotlinpoet.STRING
import community.flock.kotlinx.openapi.bindings.MediaType
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Operation
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Parameter
import community.flock.kotlinx.openapi.bindings.OpenAPIV3ParameterLocation
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Reference
import community.flock.kotlinx.openapi.bindings.OpenAPIV3RequestBody
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Response
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Type
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.litote.openapi.ktor.client.generator.ApiGeneratorConfiguration
import org.litote.openapi.ktor.client.generator.SplitGranularity
import org.litote.openapi.ktor.client.generator.domain.ClientConfigurationSpec
import org.litote.openapi.ktor.client.generator.domain.ClientSpec
import org.litote.openapi.ktor.client.generator.domain.ComponentParameterSpec
import org.litote.openapi.ktor.client.generator.domain.DefaultValueSpec
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.FormFieldSpec
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.ModelPropertySpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.domain.OperationMetaSpec
import org.litote.openapi.ktor.client.generator.domain.OperationParameterSpec
import org.litote.openapi.ktor.client.generator.domain.OperationSpec
import org.litote.openapi.ktor.client.generator.domain.ParameterLocationSpec
import org.litote.openapi.ktor.client.generator.domain.RequestBodySpec
import org.litote.openapi.ktor.client.generator.domain.ResponseEntrySpec
import org.litote.openapi.ktor.client.generator.port.ApiSpecificationParser
import org.litote.openapi.ktor.client.generator.shared.capitalize
import org.litote.openapi.ktor.client.generator.shared.sanitizeToIdentifier
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase
import org.litote.openapi.ktor.client.generator.shared.tagToCamelCase

public class OpenApiSpecificationParser(
    private val configuration: ApiGeneratorConfiguration,
) : ApiSpecificationParser {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun parse(operationFilter: (OperationMetaSpec) -> Boolean): GenerationSpec {
        val apiModel = ApiModel.parseOpenApiFile(configuration)
        val modelPackage = configuration.resolvedModelPackage

        val clientConfigurationSpec = buildClientConfigurationSpec(apiModel, configuration)
        val modelSpecs = buildModelSpecs(apiModel, configuration)
        val clientSpecs = buildClientSpecs(apiModel, configuration, modelPackage)

        return GenerationSpec(
            clientConfiguration = clientConfigurationSpec,
            clients = clientSpecs,
            models = modelSpecs,
        )
    }

    private fun buildClientConfigurationSpec(
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
    ): ClientConfigurationSpec {
        val apiKeySchemes = apiModel.apiKeySecuritySchemes

        val componentParams =
            apiModel.componentParameters.map { param ->
                val typeName =
                    param.schema?.let { schema ->
                        apiModel.getClassName(constName(param.name), schema)
                    } ?: STRING
                val defaultLiteral = parameterDefaultLiteral(param.schema, typeName)
                val defaultValue =
                    if (defaultLiteral != null) buildDefaultValueFromCodeBlock(defaultLiteral, typeName) else null

                ComponentParameterSpec(
                    originalName = param.name,
                    constName = "PARAMETER_${constName(param.name)}",
                    type = typeName.toDomainType(configuration.resolvedModelPackage),
                    defaultValue = defaultValue,
                )
            }

        return ClientConfigurationSpec(
            serverUrl = apiModel.serverUrl,
            apiKeySchemes = apiKeySchemes,
            componentParameters = componentParams,
            hasYamlContentType = detectYamlContentType(apiModel),
        )
    }

    private fun detectYamlContentType(apiModel: ApiModel): Boolean {
        val yamlMimeTypes = setOf("application/yaml", "application/x-yaml")
        return apiModel.pathsByTags.values.flatten().any { apiOperation ->
            val requestYaml =
                (apiOperation.operation.requestBody as? OpenAPIV3RequestBody)
                    ?.content
                    ?.keys
                    ?.map { it.value }
                    ?.any { it.lowercase() in yamlMimeTypes } == true
            val responseYaml =
                apiOperation.operation.responses?.values?.any { response ->
                    (response as? OpenAPIV3Response)
                        ?.content
                        ?.keys
                        ?.map { it.value }
                        ?.any { it.lowercase() in yamlMimeTypes } == true
                } == true
            requestYaml || responseYaml
        }
    }

    private fun buildClientSpecs(
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
        modelPackage: String,
    ): List<ClientSpec> {
        val pathsByTags = apiModel.pathsByTags
        return when (configuration.splitGranularity) {
            SplitGranularity.BY_TAG -> {
                pathsByTags.map { (tag, operations) ->
                    ClientSpec(
                        name = tagToClientName(tag),
                        operations = buildOperationSpecs(operations, apiModel, configuration, modelPackage),
                    )
                }
            }

            SplitGranularity.BY_TAG_AND_PATH -> {
                pathsByTags.flatMap { (tag, operations) ->
                    operations
                        .groupBy { it.path }
                        .map { (path, pathOps) ->
                            ClientSpec(
                                name = "${tagBaseName(tag)}${pathToCamelCase(path)}Client",
                                operations = buildOperationSpecs(pathOps, apiModel, configuration, modelPackage),
                            )
                        }
                }
            }

            SplitGranularity.BY_TAG_AND_OPERATION -> {
                pathsByTags.flatMap { (tag, operations) ->
                    operations.map { op ->
                        ClientSpec(
                            name = "${tagBaseName(tag)}${pathToCamelCase(op.path)}${op.method.capitalize()}Client",
                            operations = buildOperationSpecs(listOf(op), apiModel, configuration, modelPackage),
                        )
                    }
                }
            }
        }
    }

    private fun tagBaseName(tag: String): String = tag.sanitizeToIdentifier().tagToCamelCase().removeSuffix("Controller")

    private fun tagToClientName(tag: String): String = "${tagBaseName(tag)}Client"

    /** Converts an OpenAPI path like `/v1/users/{id}` to a camelCase identifier `V1UsersId`. */
    private fun pathToCamelCase(path: String): String = path.removePrefix("/").sanitizeToIdentifier().capitalize()

    private fun buildOperationSpecs(
        operations: List<ApiOperation>,
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
        modelPackage: String,
    ): List<OperationSpec> {
        val operationIds = operations.map { it.operation.operationId }
        val duplicateIds =
            operationIds
                .groupBy { it }
                .filter { it.value.size > 1 }
                .keys
                .toSet()

        val rawSpecs =
            operations.map { apiOperation ->
                buildOperationSpec(apiOperation, duplicateIds, apiModel, configuration, modelPackage)
            }

        val conflictingModelNames = findConflictingAdditionalModelNames(rawSpecs)
        if (conflictingModelNames.isEmpty()) return rawSpecs

        return rawSpecs.map { spec ->
            spec.copy(parameters = resolveAdditionalModelConflicts(spec, conflictingModelNames))
        }
    }

    private fun findConflictingAdditionalModelNames(specs: List<OperationSpec>): Set<String> {
        val allAdditionalModelEntries =
            specs.flatMap { spec ->
                spec.parameters.mapNotNull { p ->
                    p.additionalModel?.let { it.name to spec }
                }
            }
        return allAdditionalModelEntries
            .groupBy { it.first }
            .filter { it.value.size > 1 }
            .keys
            .toSet()
    }

    private fun resolveAdditionalModelConflicts(
        spec: OperationSpec,
        conflictingModelNames: Set<String>,
    ): List<OperationParameterSpec> =
        spec.parameters.map { param ->
            val additionalModelLocal = param.additionalModel
            if (additionalModelLocal != null && conflictingModelNames.contains(additionalModelLocal.name)) {
                resolveParamConflict(param, additionalModelLocal, spec.name)
            } else {
                param
            }
        }

    private fun resolveParamConflict(
        param: OperationParameterSpec,
        additionalModel: ModelSpec,
        specName: String,
    ): OperationParameterSpec {
        val modelName = additionalModel.name
        val newName = "$specName$modelName"
        val newModel = renameModelSpec(additionalModel, newName)
        val isEnum = additionalModel is ModelSpec.EnumSpec
        val inlineType = DomainTypeSpec.InlineTypeSpec(newName, isEnum = isEnum)
        val baseType = if (param.type.nullable) param.type.asNonNullable() else param.type
        val newBaseType: DomainTypeSpec =
            when (baseType) {
                is DomainTypeSpec.InlineTypeSpec -> inlineType
                is DomainTypeSpec.ListTypeSpec -> baseType.copy(element = inlineType)
                is DomainTypeSpec.SetTypeSpec -> baseType.copy(element = inlineType)
                else -> inlineType
            }
        val finalType = if (param.type.nullable) newBaseType.asNullable() else newBaseType
        val newDefaultValue =
            if (param.isEnum && param.defaultValue != null) {
                when (val dv = param.defaultValue) {
                    is DefaultValueSpec.EnumDefaultSpec -> dv.copy(typeName = newName)
                    else -> dv
                }
            } else {
                param.defaultValue
            }
        return param.copy(type = finalType, additionalModel = newModel, defaultValue = newDefaultValue)
    }

    private fun buildOperationSpec(
        apiOperation: ApiOperation,
        duplicateIds: Set<String?>,
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
        modelPackage: String,
    ): OperationSpec {
        val operation = apiOperation.operation
        val operationId = operation.operationId
        val methodName =
            (
                operationId?.takeUnless { duplicateIds.contains(it) }
                    ?: "${apiOperation.method}_${
                        apiOperation.path.replace("/", "_").replace("{", "With_").replace("}", "")
                    }"
            ).replace("-", "_").snakeToCamelCase().capitalize()

        val parameters = buildParameters(operation, apiModel, configuration, modelPackage)
        val requestBodySpec =
            (operation.requestBody as? OpenAPIV3RequestBody)?.let {
                buildRequestBodySpec(it, methodName, apiModel, configuration, modelPackage)
            }
        val responseEntries = buildResponseEntries(operation, methodName, apiModel, modelPackage)
        val isSse = isSseOperation(operation)
        val inlineModels = requestBodySpec?.inlineModels ?: emptyList()

        return OperationSpec(
            name = methodName,
            path = apiOperation.path,
            method = apiOperation.method,
            parameters = parameters,
            requestBody = requestBodySpec,
            responses = responseEntries,
            isSse = isSse,
            summary = operation.summary,
            inlineModels = inlineModels,
        )
    }

    private fun buildParameters(
        operation: OpenAPIV3Operation,
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
        modelPackage: String,
    ): List<OperationParameterSpec> =
        operation
            .parameters
            .orEmpty()
            .asSequence()
            .mapNotNull { apiModel.getComponentParameter(it) }
            .distinctBy { it.name }
            .map { parameter ->
                buildOperationParameter(parameter, apiModel, configuration, modelPackage)
            }.toList()

    private fun buildOperationParameter(
        parameter: OpenAPIV3Parameter,
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
        modelPackage: String,
    ): OperationParameterSpec {
        val paramBaseName = parameterTypeBaseName(parameter.name)
        val parameterTypeName =
            parameter.schema?.let { schema ->
                apiModel.getClassName(paramBaseName, schema)
            } ?: STRING
        val defaultLiteral = parameterDefaultLiteral(parameter.schema, parameterTypeName)
        val isOptional = parameter.required != true
        val constBaseName = constName(parameter.name)
        val constName =
            if (apiModel.componentParameters.none { it.name == parameter.name }) {
                null
            } else {
                "PARAMETER_$constBaseName"
            }
        val constDefaultValue =
            if (constName != null && defaultLiteral != null && parameterTypeName.isPrimitive()) {
                "PARAMETER_${constBaseName}_DEFAULT_VALUE"
            } else {
                null
            }
        val paramName = parameterVariableName(parameter.name)
        val rawDomainType = parameterTypeName.toDomainType(modelPackage)
        val additionalTypeName = computeAdditionalTypeName(parameter, parameterTypeName, paramName)
        val additionalModel =
            if (additionalTypeName != null) {
                buildAdditionalModel(additionalTypeName, parameter, apiModel, configuration)
            } else {
                null
            }

        val domainType =
            if (additionalModel is ModelSpec.EnumSpec && additionalTypeName != null) {
                adjustTypeForAdditionalModel(rawDomainType, additionalModel, additionalTypeName)
            } else {
                rawDomainType
            }
        val domainTypeWithNullability = if (isOptional) domainType.asNullable() else domainType
        val defaultValue = defaultLiteral?.let { buildDefaultValueFromCodeBlock(it, parameterTypeName) }

        return OperationParameterSpec(
            originalName = parameter.name,
            camelCaseName = paramName,
            type = domainTypeWithNullability,
            location =
                when (parameter.`in`) {
                    OpenAPIV3ParameterLocation.HEADER -> ParameterLocationSpec.HEADER
                    OpenAPIV3ParameterLocation.PATH -> ParameterLocationSpec.PATH
                    OpenAPIV3ParameterLocation.QUERY -> ParameterLocationSpec.QUERY
                    else -> ParameterLocationSpec.QUERY
                },
            required = !isOptional,
            constName = constName,
            constDefaultName = constDefaultValue,
            defaultValue = defaultValue,
            additionalModel = additionalModel,
            additionalModelBaseName = additionalTypeName,
        )
    }

    private fun computeAdditionalTypeName(
        parameter: OpenAPIV3Parameter,
        parameterTypeName: com.squareup.kotlinpoet.TypeName,
        paramName: String,
    ): String? {
        val schema = parameter.schema as? OpenAPIV3Schema ?: return null
        val items = schema.items as? OpenAPIV3Schema
        return when {
            schema.firstType == OpenAPIV3Type.ARRAY && items == null -> null
            parameterTypeName.isPrimitive() -> null
            else -> paramName.snakeToCamelCase().capitalize()
        }
    }

    private fun buildAdditionalModel(
        typeName: String,
        parameter: OpenAPIV3Parameter,
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
    ): ModelSpec? {
        val schema = parameter.schema as? OpenAPIV3Schema ?: return null
        val items = schema.items as? OpenAPIV3Schema
        val targetSchema =
            if (schema.firstType == OpenAPIV3Type.ARRAY && items != null) items else schema
        return buildModelSpecFromSchema(typeName, targetSchema, apiModel, configuration)
    }

    /**
     * When a parameter has an additional inline model, adjust the domain type to reference it.
     * For example, `List<String>` with an additional `ObjectSpec("Id")` becomes `List<InlineType("Id")>`.
     * For enum models, marks the `InlineType.isEnum = true`.
     */
    private fun adjustTypeForAdditionalModel(
        domainType: DomainTypeSpec,
        additionalModel: ModelSpec,
        additionalTypeName: String,
    ): DomainTypeSpec {
        val isEnum = additionalModel is ModelSpec.EnumSpec
        val inlineType = DomainTypeSpec.InlineTypeSpec(additionalTypeName, isEnum = isEnum)
        return when (domainType) {
            is DomainTypeSpec.ListTypeSpec -> domainType.copy(element = inlineType)
            is DomainTypeSpec.SetTypeSpec -> domainType.copy(element = inlineType)
            is DomainTypeSpec.InlineTypeSpec -> inlineType
            else -> domainType
        }
    }

    private fun buildRequestBodySpec(
        requestBody: OpenAPIV3RequestBody,
        operationName: String,
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
        modelPackage: String,
    ): RequestBodySpec? {
        val content = requestBody.content ?: return null
        val requestSchema = content.values.firstOrNull()?.schema
        val contentTypes = content.keys.map { it.value }.toSet()
        val isMultipartFormData = contentTypes.any { it.equals("multipart/form-data", ignoreCase = true) }
        val isUrlEncodedForm = contentTypes.any { it.equals("application/x-www-form-urlencoded", ignoreCase = true) }
        val parameterName = if (isMultipartFormData || isUrlEncodedForm) "form" else "request"

        if (isMultipartFormData || isUrlEncodedForm) {
            val formSchema =
                apiModel.resolveSchema(requestSchema) ?: requestSchema as? OpenAPIV3Schema ?: return null
            val formFields = buildFormFields(formSchema, operationName, apiModel, modelPackage)
            val domainType = DomainTypeSpec.InlineTypeSpec("${operationName}Form")
            return RequestBodySpec(
                parameterName = parameterName,
                type = domainType,
                contentTypes = contentTypes,
                isMultipartFormData = isMultipartFormData,
                isUrlEncodedForm = isUrlEncodedForm,
                formFields = formFields,
                inlineModels = emptyList(),
            )
        }

        val inlineObjectSchema =
            (requestSchema as? OpenAPIV3Schema)?.takeIf {
                it.oneOf.isNullOrEmpty() && !it.properties.isNullOrEmpty()
            }

        val inlineModels = mutableListOf<ModelSpec>()
        val requestType =
            when {
                inlineObjectSchema != null -> {
                    val inlineModelSpec =
                        buildModelSpecFromSchema("${operationName}Request", inlineObjectSchema, apiModel, configuration)
                    if (inlineModelSpec != null) inlineModels.add(inlineModelSpec)
                    DomainTypeSpec.InlineTypeSpec("${operationName}Request")
                }

                requestSchema != null -> {
                    val typeName = apiModel.getClassName("${operationName}Request", requestSchema)
                    typeName.toDomainType(modelPackage)
                }

                else -> {
                    return null
                }
            }

        return RequestBodySpec(
            parameterName = parameterName,
            type = requestType,
            contentTypes = contentTypes,
            isMultipartFormData = false,
            isUrlEncodedForm = false,
            formFields = emptyList(),
            inlineModels = inlineModels.toList(),
        )
    }

    private fun buildFormFields(
        formSchema: OpenAPIV3Schema,
        operationName: String,
        apiModel: ApiModel,
        modelPackage: String,
    ): List<FormFieldSpec> {
        val fileClassName = "${operationName}FormFile"
        return formSchema.properties
            ?.map { (name, propertySchema) ->
                val property = apiModel.getClassProperty(name, propertySchema, formSchema)
                val resolvedSchema = apiModel.resolveSchema(propertySchema) ?: propertySchema as? OpenAPIV3Schema
                val isBinary =
                    resolvedSchema?.firstType == OpenAPIV3Type.STRING && resolvedSchema.format == "binary"
                val fieldType =
                    if (isBinary) {
                        DomainTypeSpec.InlineTypeSpec(fileClassName)
                    } else {
                        property.type.toDomainType(modelPackage)
                    }
                val isOptional = property.type.isNullable
                FormFieldSpec(
                    originalName = name,
                    parameterName = property.camelCaseName,
                    type = if (isOptional) fieldType.asNullable() else fieldType,
                    isBinary = isBinary,
                    isOptional = isOptional,
                )
            }.orEmpty()
    }

    private fun buildResponseEntries(
        operation: OpenAPIV3Operation,
        operationName: String,
        apiModel: ApiModel,
        modelPackage: String,
    ): List<ResponseEntrySpec> {
        val responses = operation.responses ?: return emptyList()

        val parsedResponses =
            responses.entries
                .mapNotNull { (key, responseOrRef) ->
                    val code = key.value.toIntOrNull() ?: return@mapNotNull null
                    val response = responseOrRef as? OpenAPIV3Response ?: return@mapNotNull null
                    val schema =
                        response.content?.get(MediaType("application/json"))?.schema
                            ?: response.content?.get(MediaType("application/yaml"))?.schema
                            ?: response.content?.get(MediaType("application/x-yaml"))?.schema
                            ?: response.content?.get(MediaType("*/*"))?.schema
                    if (response.content != null && schema == null) {
                        val isSseContent =
                            response.content?.containsKey(MediaType("text/event-stream")) == true
                        if (!isSseContent) {
                            logger.warn { "Unknown media type for: $responseOrRef - do not parse response" }
                        }
                    }
                    val bodyTypeName = schema?.let { apiModel.getClassName("${operationName}ResponseBody", it) }
                    val bodyType = bodyTypeName?.toDomainType(modelPackage)
                    code to bodyType
                }.sortedBy { it.first }

        val grouped =
            parsedResponses
                .groupBy { (code, bodyType) -> bodyType to (code in 200 until 300) }
                .map { (key, values) -> Triple(key.first, key.second, values.map { it.first }) }

        return grouped.map { (bodyType, isSuccess, statusCodes) ->
            ResponseEntrySpec(
                statusCodes = statusCodes,
                bodyType = bodyType,
                isSuccess = isSuccess,
            )
        }
    }

    private fun buildModelSpecs(
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
    ): List<ModelSpec> {
        val specs = mutableListOf<ModelSpec>()
        apiModel.schemas.forEach { (name, schema) ->
            buildModelSpecFromSchema(name, schema, apiModel, configuration)?.let { specs.add(it) }
        }
        apiModel.requestBodySealedParents.keys.forEach { name ->
            specs.add(ModelSpec.SealedClassSpec(name = name, discriminatorPropertyName = null))
        }
        return specs.toList()
    }

    internal fun buildModelSpecFromSchema(
        name: String,
        schema: OpenAPIV3Schema,
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
    ): ModelSpec? {
        val oneOfRefs = schema.oneOf?.filterIsInstance<OpenAPIV3Reference>()
        if (oneOfRefs != null && oneOfRefs.size >= 2) {
            return ModelSpec.SealedClassSpec(
                name = name,
                discriminatorPropertyName = schema.discriminator?.propertyName,
            )
        }

        val allOfParts: List<OpenAPIV3Schema> = schema.allOf?.mapNotNull { it as? OpenAPIV3Schema } ?: emptyList()
        val mergedProperties =
            (schema.properties ?: emptyMap()) +
                allOfParts.flatMap { it.properties?.entries ?: emptyList() }.associate { it.key to it.value }
        val mergedRequired =
            ((schema.required ?: emptyList()) + allOfParts.flatMap { it.required ?: emptyList() }).distinct()
        val effectiveSchema =
            if (allOfParts.isNotEmpty()) {
                schema.copy(
                    properties = mergedProperties,
                    required = mergedRequired,
                )
            } else {
                schema
            }

        val properties = buildModelProperties(effectiveSchema, apiModel, configuration)

        return when {
            properties.isEmpty() && effectiveSchema.enum.isNullOrEmpty() -> {
                val sealedParentName = apiModel.sealedSubTypes[name]
                val discriminatorValue =
                    sealedParentName?.let { resolveDiscriminatorValue(name, it, apiModel) }
                ModelSpec.ObjectSpec(
                    name = name,
                    sealedParentName = sealedParentName,
                    discriminatorValue = discriminatorValue,
                )
            }

            properties.isEmpty() && !effectiveSchema.enum.isNullOrEmpty() -> {
                val enumValues =
                    effectiveSchema.enum.orEmpty().mapNotNull { e ->
                        e.contentOrNull
                    }
                ModelSpec.EnumSpec(name = name, values = enumValues)
            }

            effectiveSchema.discriminator != null -> {
                ModelSpec.AliasSpec(name = name)
            }

            else -> {
                val sealedParentName = apiModel.sealedSubTypes[name]
                val discriminatorValue =
                    sealedParentName?.let { resolveDiscriminatorValue(name, it, apiModel) }
                ModelSpec.DataClassSpec(
                    name = name,
                    properties = properties,
                    sealedParentName = sealedParentName,
                    discriminatorValue = discriminatorValue,
                )
            }
        }
    }

    private fun buildModelProperties(
        schema: OpenAPIV3Schema,
        apiModel: ApiModel,
        configuration: ApiGeneratorConfiguration,
    ): List<ModelPropertySpec> {
        val modelPackage = configuration.resolvedModelPackage
        val rawProperties =
            schema.properties
                ?.asSequence()
                ?.mapNotNull { (propName, schemaOrReference) ->
                    if (schemaOrReference is OpenAPIV3Schema && schemaOrReference.deprecated == true) {
                        null
                    } else {
                        apiModel.getClassProperty(propName, schemaOrReference, schema)
                    }
                }?.sortedBy { it.camelCaseName }
                ?.toList() ?: emptyList()

        return rawProperties.map { property ->
            val nestedModels = mutableListOf<ModelSpec>()
            val propSchema = property.asSchema
            val propRef = property.asReference
            val enum = propSchema?.enum ?: (propSchema?.items as? OpenAPIV3Schema)?.enum
            val isEnum =
                !enum.isNullOrEmpty() || (propRef != null && apiModel.isEnum(property))

            val finalType: DomainTypeSpec
            if (propSchema != null && propSchema.firstType == OpenAPIV3Type.OBJECT &&
                !propSchema.properties.isNullOrEmpty()
            ) {
                val nestedName = property.camelCaseName.capitalize()
                buildModelSpecFromSchema(nestedName, propSchema, apiModel, configuration)?.let {
                    nestedModels.add(it)
                }
                val inlineType = DomainTypeSpec.InlineTypeSpec(nestedName, isEnum = false)
                finalType = if (property.type.isNullable) inlineType.asNullable() else inlineType
            } else if (!enum.isNullOrEmpty()) {
                val enumName = property.camelCaseName.capitalize()
                val enumValues =
                    enum.mapNotNull { it.contentOrNull }
                nestedModels.add(ModelSpec.EnumSpec(name = enumName, values = enumValues))
                finalType = property.type.toDomainType(modelPackage)
            } else {
                finalType = property.type.toDomainType(modelPackage)
            }

            val schemaDefaultValue =
                propSchema?.default?.let { default ->
                    (default as? JsonPrimitive)?.content
                }

            ModelPropertySpec(
                originalName = property.initialName,
                camelCaseName = property.camelCaseName,
                type = finalType,
                needsSerialName = property.needsSerialName,
                isEnum = isEnum,
                schemaDefaultValue = schemaDefaultValue,
                nestedModels = nestedModels.toList(),
            )
        }
    }

    private fun resolveDiscriminatorValue(
        subName: String,
        parentName: String,
        apiModel: ApiModel,
    ): String? {
        val parentSchema =
            apiModel.components?.schemas?.get(parentName) as? OpenAPIV3Schema ?: return null
        val discriminator = parentSchema.discriminator ?: return null

        discriminator.mapping
            ?.entries
            ?.firstOrNull { it.value.substringAfterLast("/") == subName }
            ?.let { return it.key }

        val subSchema =
            apiModel.components?.schemas?.get(subName) as? OpenAPIV3Schema ?: return null
        val discriminatorProp =
            subSchema.properties?.get(discriminator.propertyName) as? OpenAPIV3Schema
        return discriminatorProp?.enum?.firstOrNull()?.contentOrNull
    }

    private fun isSseOperation(operation: OpenAPIV3Operation): Boolean {
        val responses = operation.responses ?: return false
        return responses.entries.any { (key, value) ->
            val code = key.value.toIntOrNull() ?: return@any false
            if (code !in 200..299) return@any false
            val response = value as? OpenAPIV3Response ?: return@any false
            response.content?.containsKey(MediaType("text/event-stream")) == true
        }
    }

    private fun buildDefaultValueFromCodeBlock(
        codeBlock: com.squareup.kotlinpoet.CodeBlock,
        typeName: com.squareup.kotlinpoet.TypeName,
    ): DefaultValueSpec? {
        val str = codeBlock.toString()
        return when {
            typeName.isString() -> {
                DefaultValueSpec.StringDefaultSpec(str.removeSurrounding("\""))
            }

            typeName.isBoolean() -> {
                str.toBooleanStrictOrNull()?.let { DefaultValueSpec.BooleanDefaultSpec(it) }
            }

            typeName.isLong() -> {
                str.toLongOrNull()?.let { DefaultValueSpec.LongDefaultSpec(it) }
            }

            typeName.isDouble() -> {
                str.toDoubleOrNull()?.let { DefaultValueSpec.DoubleDefaultSpec(it) }
            }

            typeName.isFloat() -> {
                str.removeSuffix("F").toFloatOrNull()?.let { DefaultValueSpec.FloatDefaultSpec(it) }
            }

            typeName.isInt() -> {
                str.toIntOrNull()?.let { DefaultValueSpec.IntDefaultSpec(it) }
            }

            else -> {
                // Enum default: format is "EnumName.ENUM_VALUE"
                val parts = str.split(".")
                if (parts.size == 2) {
                    DefaultValueSpec.EnumDefaultSpec(typeName = parts[0], enumValue = parts[1])
                } else {
                    null
                }
            }
        }
    }
}

private fun renameModelSpec(
    model: ModelSpec,
    newName: String,
): ModelSpec =
    when (model) {
        is ModelSpec.EnumSpec -> model.copy(name = newName)
        is ModelSpec.DataClassSpec -> model.copy(name = newName)
        is ModelSpec.ObjectSpec -> model.copy(name = newName)
        is ModelSpec.SealedClassSpec -> model.copy(name = newName)
        is ModelSpec.AliasSpec -> model.copy(name = newName)
    }
