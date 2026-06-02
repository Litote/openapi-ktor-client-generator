package org.litote.openapi.ktor.client.generator.adapter.parser

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import community.flock.kotlinx.openapi.bindings.BooleanValue
import community.flock.kotlinx.openapi.bindings.OpenAPIV3
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Model
import community.flock.kotlinx.openapi.bindings.Operation
import community.flock.kotlinx.openapi.bindings.Parameter
import community.flock.kotlinx.openapi.bindings.ParameterOrReference
import community.flock.kotlinx.openapi.bindings.Reference
import community.flock.kotlinx.openapi.bindings.Response
import community.flock.kotlinx.openapi.bindings.Schema
import community.flock.kotlinx.openapi.bindings.SchemaOrReference
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.litote.openapi.ktor.client.generator.ApiGeneratorConfiguration
import org.litote.openapi.ktor.client.generator.domain.OperationMetaSpec
import org.litote.openapi.ktor.client.generator.domain.SecuritySchemeSpec
import org.litote.openapi.ktor.client.generator.shared.capitalize
import org.litote.openapi.ktor.client.generator.shared.ensureEndsWith
import org.litote.openapi.ktor.client.generator.shared.sanitizeToIdentifier
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase
import org.litote.openapi.ktor.client.generator.shared.toOrNull
import java.nio.file.Files
import java.nio.file.Path
import org.yaml.snakeyaml.Yaml as SnakeYaml

internal class ApiModel private constructor(
    val model: OpenAPIV3Model,
    val configuration: ApiGeneratorConfiguration,
) {
    internal companion object {
        private val logger = KotlinLogging.logger {}
        private val openApiParser = OpenAPIV3(Json { ignoreUnknownKeys = true })

        internal fun parseOpenApiFile(configuration: ApiGeneratorConfiguration): ApiModel {
            val openApiFile = configuration.openApiFile
            logger.debug { "Parsing $openApiFile" }
            val rawContent = Files.readString(Path.of(openApiFile))
            val jsonContent =
                if (openApiFile.endsWith(".yaml", ignoreCase = true) ||
                    openApiFile.endsWith(
                        ".yml",
                        ignoreCase = true,
                    )
                ) {
                    yamlToJson(rawContent)
                } else {
                    rawContent
                }

            return ApiModel(openApiParser.decodeFromString(jsonContent), configuration)
        }

        private fun yamlToJson(yamlContent: String): String {
            val snakeYaml = SnakeYaml()
            val parsed = snakeYaml.load<Any>(yamlContent)
            return Json.encodeToString(JsonElement.serializer(), anyToJsonElement(parsed))
        }

        private fun anyToJsonElement(obj: Any?): JsonElement =
            when (obj) {
                null -> {
                    JsonNull
                }

                is Map<*, *> -> {
                    kotlinx.serialization.json.JsonObject(
                        obj.entries.associate { (k, v) -> k.toString() to anyToJsonElement(v) },
                    )
                }

                is List<*> -> {
                    JsonArray(obj.map { anyToJsonElement(it) })
                }

                is Boolean -> {
                    JsonPrimitive(obj)
                }

                is Number -> {
                    JsonPrimitive(obj)
                }

                is String -> {
                    JsonPrimitive(obj)
                }

                else -> {
                    JsonPrimitive(obj.toString())
                }
            }
    }

    val outputDirectory: String get() = configuration.outputDirectory
    val serverUrl: String =
        model.apiServers
            ?.firstOrNull()
            ?.url
            ?.ensureEndsWith("/") ?: "http://localhost:8080/"

    val pathsByTags: Map<String, List<ApiOperation>> =
        model.paths
            .orEmpty()
            .entries
            .asSequence()
            .flatMap { (path, item) ->
                listOfNotNull(
                    "get" toOrNull item.get,
                    "put" toOrNull item.put,
                    "post" toOrNull item.post,
                    "delete" toOrNull item.delete,
                    "options" toOrNull item.options,
                    "head" toOrNull item.head,
                    "patch" toOrNull item.patch,
                    "trace" toOrNull item.trace,
                ).asSequence()
                    .flatMap { (method, operation) ->
                        (
                            operation
                                .tags
                                .takeUnless { it.isNullOrEmpty() }
                                ?: listOf("")
                        ).asSequence()
                            .filterNotNull()
                            .distinct()
                            .map { it to ApiOperation(path.value, method, operation) }
                    }
            }.filter { (_, operation) ->
                val meta =
                    OperationMetaSpec(
                        path = operation.path,
                        method = operation.method,
                        tags =
                            operation.operation.tags
                                .orEmpty()
                                .filterNotNull(),
                    )
                configuration.operationFilter(meta)
            }.groupBy(keySelector = { it.first }, valueTransform = { it.second })

    val schemaParentMap: Map<String, Set<String>> =
        model.componentSchemas
            ?.mapValues { (_, v) -> v.allReferences().map { it.refClassName }.toSet() }
            ?: emptyMap()

    internal val requestBodySealedParents: Map<String, List<String>> =
        model.paths
            .orEmpty()
            .values
            .flatMap { pathItem ->
                listOfNotNull(
                    pathItem.get,
                    pathItem.post,
                    pathItem.put,
                    pathItem.delete,
                    pathItem.patch,
                    pathItem.options,
                    pathItem.head,
                    pathItem.trace,
                ).mapNotNull { op ->
                    val opId = op.operationId ?: return@mapNotNull null
                    val schema =
                        op.requestBody
                            ?.asRequestBody
                            ?.content
                            ?.values
                            ?.firstOrNull()
                            ?.schema as? Schema
                            ?: return@mapNotNull null
                    val refs = schema.oneOfSchemas?.filterIsInstance<Reference>() ?: return@mapNotNull null
                    if (refs.size < 2) return@mapNotNull null
                    val sealedName = "${opId.snakeToCamelCase().capitalize()}Request"
                    sealedName to refs.map { it.refClassName }
                }
            }.toMap()

    internal val responseSealedParents: Map<String, List<String>> =
        model.paths
            .orEmpty()
            .values
            .flatMap { pathItem ->
                listOfNotNull(
                    pathItem.get,
                    pathItem.post,
                    pathItem.put,
                    pathItem.delete,
                    pathItem.patch,
                    pathItem.options,
                    pathItem.head,
                    pathItem.trace,
                ).mapNotNull { op ->
                    val opId = op.operationId ?: return@mapNotNull null
                    val refs =
                        op.responses?.values?.firstNotNullOfOrNull { responseOrRef ->
                            if (responseOrRef !is Response) return@firstNotNullOfOrNull null
                            responseOrRef.responseContent?.values?.firstNotNullOfOrNull { mediaType ->
                                val schema = mediaType.schema as? Schema ?: return@firstNotNullOfOrNull null
                                val oneOfRefs =
                                    schema.oneOfSchemas?.filterIsInstance<Reference>()
                                        ?: return@firstNotNullOfOrNull null
                                if (oneOfRefs.size >= 2) oneOfRefs else null
                            }
                        } ?: return@mapNotNull null
                    val sealedName = "${opId.snakeToCamelCase().capitalize()}Response"
                    sealedName to refs.map { it.refClassName }
                }
            }.toMap()

    val sealedParents: Map<String, List<String>> =
        (
            model.componentSchemas
                ?.entries
                ?.mapNotNull { (name, schemaOrRef) ->
                    val schema = schemaOrRef as? Schema ?: return@mapNotNull null
                    val refs = schema.oneOfSchemas?.filterIsInstance<Reference>() ?: return@mapNotNull null
                    if (refs.size < 2) return@mapNotNull null
                    name to refs.map { it.refClassName }
                }?.toMap()
                ?: emptyMap()
        ) + requestBodySealedParents + responseSealedParents

    val sealedSubTypes: Map<String, String> =
        sealedParents
            .flatMap { (parent, children) -> children.map { it to parent } }
            .toMap()

    val schemas: Map<String, Schema> =
        (
            pathsByTags
                .values
                .flatten()
                .distinct()
                .flatMap { o ->
                    o.operation.allReferences().map { it.refClassName }
                }.toSet()
                .run {
                    val set = mutableSetOf<String>()
                    forEach { addChildren(set, it) }
                    set
                }
        ).let { set ->
            (
                model.componentSchemas
                    ?.mapNotNull { (k, v) -> (v as? Schema)?.let { k to it } }
                    ?.toMap()
                    ?: emptyMap()
            ).filterKeys { set.contains(it) }
        }

    internal val allOfOnlySchemas: Set<String> =
        run {
            val allOfRefs = mutableSetOf<String>()
            val directRefs = mutableSetOf<String>()

            model.componentSchemas?.values?.forEach { schemaOrRef ->
                val schema = schemaOrRef as? Schema ?: return@forEach
                schema.allOf?.forEach { part ->
                    when (part) {
                        is Reference -> {
                            allOfRefs.add(part.refClassName)
                        }

                        is Schema -> {
                            part.properties
                                ?.values
                                ?.filterIsInstance<Reference>()
                                ?.forEach { directRefs.add(it.refClassName) }
                            (part.items as? Reference)?.let { directRefs.add(it.refClassName) }
                        }
                    }
                }
                schema.oneOfSchemas
                    ?.filterIsInstance<Reference>()
                    ?.forEach { directRefs.add(it.refClassName) }
                schema.anyOfSchemas
                    ?.filterIsInstance<Reference>()
                    ?.forEach { directRefs.add(it.refClassName) }
                schema.properties
                    ?.values
                    ?.filterIsInstance<Reference>()
                    ?.forEach { directRefs.add(it.refClassName) }
                (schema.items as? Reference)?.let { directRefs.add(it.refClassName) }
                (schema.notSchema as? Reference)?.let { directRefs.add(it.refClassName) }
                (schema.additionalProperties as? Reference)?.let { directRefs.add(it.refClassName) }
            }

            model.paths.orEmpty().values.forEach { pathItem ->
                listOfNotNull(
                    pathItem.get,
                    pathItem.post,
                    pathItem.put,
                    pathItem.delete,
                    pathItem.patch,
                    pathItem.options,
                    pathItem.head,
                    pathItem.trace,
                ).forEach { op -> op.allReferences().forEach { directRefs.add(it.refClassName) } }
            }

            allOfRefs - directRefs
        }

    val componentParameters: List<Parameter> =
        model.componentParameters
            ?.values
            ?.mapNotNull { resolveParameter(it) }
            .orEmpty()

    val apiKeySecuritySchemes: List<SecuritySchemeSpec> =
        model.componentSecuritySchemes
            ?.entries
            ?.mapNotNull { (schemeName, scheme) ->
                scheme.apiKeySchemeData?.toSpec(schemeName)
            }.orEmpty()

    internal fun isEnum(property: ApiClassProperty): Boolean =
        (!property.asSchema?.enum.isNullOrEmpty()) ||
            property.asReference?.let { !schemas[it]?.enum.isNullOrEmpty() } == true

    private fun addChildren(
        set: MutableSet<String>,
        name: String,
    ) {
        val existingSet = schemaParentMap[name] ?: return
        set.add(name)
        existingSet.forEach {
            if (!set.contains(it)) {
                addChildren(set, it)
            }
        }
        set.addAll(existingSet)
    }

    private fun Operation.allReferences(): Set<Reference> =
        setOfNotNull(requestBody as? Reference) +
            (
                requestBody
                    ?.asRequestBody
                    ?.content
                    ?.values
                    ?.flatMap { it.schema.allReferences() }
                    ?: emptyList()
            ) +
            (parameters?.mapNotNull { it as? Reference } ?: emptyList()) +
            (
                parameters?.flatMap {
                    (it as? Parameter)?.parameterContent?.values?.flatMap { v -> v.schema.allReferences() }
                        ?: emptyList()
                }
                    ?: emptyList()
            ) +
            (responses?.values?.mapNotNull { it as? Reference } ?: emptyList()) +
            (
                responses
                    ?.values
                    ?.flatMap {
                        it.responseContent?.values?.flatMap { v -> v.schema.allReferences() }
                            ?: emptyList()
                    } ?: emptyList()
            )

    private fun SchemaOrReference?.allReferences(): Set<Reference> =
        when (this) {
            is Schema -> schemaAllReferences()
            is Reference -> setOf(this)
            null -> emptySet()
        }

    private fun Schema.schemaAllReferences(): Set<Reference> =
        setOfNotNull(
            notSchema as? Reference,
            items as? Reference,
            additionalProperties as? Reference,
        ) +
            (oneOfSchemas?.mapNotNull { it as? Reference } ?: emptyList()) +
            (anyOfSchemas?.mapNotNull { it as? Reference } ?: emptyList()) +
            (allOf?.mapNotNull { it as? Reference } ?: emptyList()) +
            (properties?.values?.mapNotNull { it as? Reference } ?: emptyList()) +
            (
                properties?.values?.flatMap { (it as? Schema)?.schemaAllReferences() ?: emptyList() }
                    ?: emptyList()
            ) +
            ((items as? Schema)?.schemaAllReferences() ?: emptyList())

    private fun resolveParameter(parameterOrReference: ParameterOrReference): Parameter? =
        when (parameterOrReference) {
            is Parameter -> {
                parameterOrReference
            }

            is Reference -> {
                val refName = parameterOrReference.refClassName
                val resolved = model.componentParameters?.get(refName)
                if (resolved == null || resolved === parameterOrReference) null else resolveParameter(resolved)
            }
        }

    internal fun getComponentParameter(parameterOrReference: ParameterOrReference): Parameter? =
        when (parameterOrReference) {
            is Reference -> {
                val refName = parameterOrReference.refClassName
                val resolved = model.componentParameters?.get(refName) ?: return null
                resolveParameter(resolved)
            }

            is Parameter -> {
                parameterOrReference
            }
        }

    internal fun resolveSchema(schemaOrReference: SchemaOrReference?): Schema? =
        when (schemaOrReference) {
            is Schema -> {
                schemaOrReference
            }

            is Reference -> {
                val refName = schemaOrReference.refClassName
                model.componentSchemas?.get(refName) as? Schema
            }

            null -> {
                null
            }
        }

    private fun getClassName(
        name: String,
        schema: Schema,
        type: ApiSchemaType?,
    ): TypeName =
        when (type) {
            ApiSchemaType.STRING -> {
                if (schema.enum?.isNotEmpty() == true) {
                    ClassName("", name.sanitizeToIdentifier().snakeToCamelCase().capitalize())
                } else {
                    STRING
                }
            }

            ApiSchemaType.NUMBER -> {
                when (schema.format) {
                    "float" -> FLOAT
                    else -> DOUBLE
                }
            }

            ApiSchemaType.INTEGER -> {
                when (schema.format) {
                    "int32" -> INT
                    else -> LONG
                }
            }

            ApiSchemaType.BOOLEAN -> {
                BOOLEAN
            }

            ApiSchemaType.ARRAY -> {
                (if (schema.uniqueItems == true) SET else LIST)
                    .parameterizedBy(
                        listOf(
                            getClassName(
                                name,
                                schema.items ?: error("null items for $schema"),
                            ),
                        ),
                    )
            }

            ApiSchemaType.OBJECT -> {
                resolveObjectType(name, schema)
            }

            ApiSchemaType.NULL, null -> {
                resolveNullableOrUnionType(name, schema)
            }
        }

    private fun resolveObjectType(
        name: String,
        schema: Schema,
    ): TypeName =
        schema.additionalProperties?.let { additionalProp ->
            when (additionalProp) {
                is BooleanValue -> {
                    error("boolean not allowed for $schema")
                }

                is Schema,
                is Reference,
                -> {
                    MAP.parameterizedBy(
                        listOf(
                            String::class.asClassName(),
                            getClassName(name, additionalProp as SchemaOrReference),
                        ),
                    )
                }
            }
        } ?: JsonElement::class.asClassName()

    private fun resolveNullableOrUnionType(
        name: String,
        schema: Schema,
    ): TypeName {
        val oneOf = schema.oneOfSchemas
        if (oneOf.isNullOrEmpty()) return JsonElement::class.asClassName()
        val refs = oneOf.filterIsInstance<Reference>()
        val hasNullSchema = oneOf.any { it.isNullSchema() }
        return when {
            refs.size == 1 && hasNullSchema -> {
                getClassName(
                    name,
                    refs.first() as SchemaOrReference,
                ).copy(nullable = true)
            }

            refs.size == 1 -> {
                getClassName(name, refs.first() as SchemaOrReference)
            }

            else -> {
                resolveMultiRefUnionType(refs)
            }
        }
    }

    private fun resolveMultiRefUnionType(refs: List<Reference>): TypeName {
        val refNames = refs.map { it.refClassName }
        val parentName =
            sealedParents.entries
                .firstOrNull { it.value.toSet() == refNames.toSet() }
                ?.key
        return if (parentName != null) {
            ClassName(configuration.resolvedModelPackage, parentName)
        } else {
            JsonElement::class.asClassName()
        }
    }

    fun getClassName(
        name: String,
        schemaOrReference: SchemaOrReference,
    ): TypeName =
        when (schemaOrReference) {
            is Reference -> {
                ClassName(
                    configuration.resolvedModelPackage,
                    schemaOrReference.refClassName.let {
                        if (it == "Companion") {
                            "${configuration.resolvedModelPackage}.$it"
                        } else {
                            it
                        }
                    },
                )
            }

            is Schema -> {
                val type = schemaOrReference.firstApiType
                if (type != null && schemaOrReference.typeIncludesNull) {
                    getClassName(name, schemaOrReference, type).copy(nullable = true)
                } else {
                    getClassName(name, schemaOrReference, type)
                }
            }
        }

    fun getClassProperty(
        name: String,
        schemaOrReference: SchemaOrReference,
        parentSchema: Schema,
    ): ApiClassProperty =
        ApiClassProperty(
            name,
            getClassName(name, schemaOrReference).let { c ->
                val inRequired = parentSchema.required?.contains(name) == true
                val typeIsNullable = (schemaOrReference as? Schema)?.typeIncludesNull == true
                if (!inRequired || typeIsNullable) c.copy(nullable = true) else c
            },
            schemaOrReference,
        )

    private fun SchemaOrReference.isNullSchema(): Boolean = this is Schema && typeIncludesNull && firstApiType == ApiSchemaType.NULL
}
