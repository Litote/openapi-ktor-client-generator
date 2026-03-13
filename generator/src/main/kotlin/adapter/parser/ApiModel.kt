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
import community.flock.kotlinx.openapi.bindings.OpenAPIV3
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Boolean
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Components
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Model
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Operation
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Parameter
import community.flock.kotlinx.openapi.bindings.OpenAPIV3ParameterOrReference
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Reference
import community.flock.kotlinx.openapi.bindings.OpenAPIV3RequestBody
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Response
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SchemaOrReference
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SecurityScheme
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SecuritySchemeType
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SingleType
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Type
import community.flock.kotlinx.openapi.bindings.OpenAPIV3TypeArray
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.litote.openapi.ktor.client.generator.ApiGeneratorConfiguration
import org.litote.openapi.ktor.client.generator.domain.OperationMeta
import org.litote.openapi.ktor.client.generator.domain.SecuritySchemeLocation
import org.litote.openapi.ktor.client.generator.domain.SecuritySchemeSpec
import org.litote.openapi.ktor.client.generator.shared.capitalize
import org.litote.openapi.ktor.client.generator.shared.ensureEndsWith
import org.litote.openapi.ktor.client.generator.shared.sanitizeToIdentifier
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase
import org.litote.openapi.ktor.client.generator.shared.toOrNull
import java.nio.file.Files
import java.nio.file.Path

internal class ApiModel private constructor(
    val model: OpenAPIV3Model,
    val configuration: ApiGeneratorConfiguration,
) {
    internal companion object {
        private val logger = KotlinLogging.logger {}

        internal fun parseOpenApiFile(configuration: ApiGeneratorConfiguration): ApiModel {
            val openApi =
                OpenAPIV3(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            val openApiFile = configuration.openApiFile
            logger.debug { "Parsing $openApiFile" }
            val json = Files.readString(Path.of(openApiFile))

            return ApiModel(openApi.decodeFromString(json), configuration)
        }
    }

    val outputDirectory: String get() = configuration.outputDirectory
    val serverUrl: String =
        model.servers
            ?.firstOrNull()
            ?.url
            ?.ensureEndsWith("/") ?: "http://localhost:8080/"

    val pathsByTags: Map<String, List<ApiOperation>> =
        model.paths.entries
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
                    OperationMeta(
                        path = operation.path,
                        method = operation.method,
                        tags =
                            operation.operation.tags
                                .orEmpty()
                                .filterNotNull(),
                    )
                configuration.operationFilter(meta)
            }.groupBy(keySelector = { it.first }, valueTransform = { it.second })

    val components: OpenAPIV3Components? get() = model.components

    val schemaParentMap: Map<String, Set<String>> =
        components
            ?.schemas
            ?.mapValues { (_, v) -> v.allReferences().map { getRefClassName(it) }.toSet() }
            ?: emptyMap()

    /**
     * Maps sealed class parent names (derived from operation request bodies with inline `oneOf`)
     * to their ordered list of sub-type ref names.
     *
     * These are "virtual" sealed classes that do not exist in `components/schemas` but are
     * synthesised from operations whose request body contains an inline `oneOf` with 2+ `$ref` entries.
     *
     * Naming convention: `{operationId.capitalize()}Request` (e.g. `createStatus` → `CreateStatusRequest`).
     */
    internal val requestBodySealedParents: Map<String, List<String>> =
        model.paths
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
                        (op.requestBody as? OpenAPIV3RequestBody)
                            ?.content
                            ?.values
                            ?.firstOrNull()
                            ?.schema as? OpenAPIV3Schema
                            ?: return@mapNotNull null
                    val refs = schema.oneOf?.filterIsInstance<OpenAPIV3Reference>() ?: return@mapNotNull null
                    if (refs.size < 2) return@mapNotNull null
                    val sealedName = "${opId.snakeToCamelCase().capitalize()}Request"
                    sealedName to refs.map { getRefClassName(it) }
                }
            }.toMap()

    /**
     * Maps sealed class parent names to their ordered list of sub-type names.
     * A schema qualifies as a sealed parent when its `oneOf` contains at least 2 `$ref` entries.
     * Also includes virtual sealed parents synthesised from inline request body `oneOf` schemas.
     */
    val sealedParents: Map<String, List<String>> =
        (
            components
                ?.schemas
                ?.entries
                ?.mapNotNull { (name, schemaOrRef) ->
                    val schema = schemaOrRef as? OpenAPIV3Schema ?: return@mapNotNull null
                    val refs = schema.oneOf?.filterIsInstance<OpenAPIV3Reference>() ?: return@mapNotNull null
                    if (refs.size < 2) return@mapNotNull null
                    name to refs.map { getRefClassName(it) }
                }?.toMap()
                ?: emptyMap()
        ) + requestBodySealedParents

    /** Reverse of [sealedParents]: maps each sub-type name to its sealed parent name. */
    val sealedSubTypes: Map<String, String> =
        sealedParents
            .flatMap { (parent, children) -> children.map { it to parent } }
            .toMap()

    val schemas: Map<String, OpenAPIV3Schema> =
        (
            pathsByTags
                .values
                .flatten()
                .distinct()
                .flatMap { o ->
                    o.operation.allReferences().map { getRefClassName(it) }
                }.toSet()
                .run {
                    val set = mutableSetOf<String>()
                    forEach { addChildren(set, it) }
                    set
                }
        ).let { set ->
            (
                components
                    ?.schemas
                    ?.filterValues { it is OpenAPIV3Schema }
                    ?.mapValues { it.value as OpenAPIV3Schema }
                    ?: emptyMap()
            ).filterKeys { set.contains(it) }
        }

    val componentParameters: List<OpenAPIV3Parameter> =
        components
            ?.parameters
            ?.values
            ?.mapNotNull { resolveParameter(it) }
            .orEmpty()

    /**
     * Extracts API Key security schemes from the OpenAPI specification.
     * These are used to generate authentication configuration in the client.
     */
    val apiKeySecuritySchemes: List<SecuritySchemeSpec> =
        components
            ?.securitySchemes
            ?.entries
            ?.mapNotNull { (schemeName, scheme) ->
                val securityScheme = scheme as? OpenAPIV3SecurityScheme ?: return@mapNotNull null
                if (securityScheme.type != OpenAPIV3SecuritySchemeType.API_KEY) return@mapNotNull null
                val keyName = securityScheme.name ?: return@mapNotNull null
                val inValue = securityScheme.`in` ?: return@mapNotNull null
                val location =
                    when (inValue) {
                        "header" -> SecuritySchemeLocation.HEADER
                        "query" -> SecuritySchemeLocation.QUERY
                        else -> return@mapNotNull null
                    }
                SecuritySchemeSpec(
                    name = schemeName,
                    keyName = keyName,
                    location = location,
                    paramName =
                        schemeName
                            .replace("_", " ")
                            .split(" ")
                            .mapIndexed { i, w -> if (i == 0) w.lowercase() else w.replaceFirstChar { it.uppercase() } }
                            .joinToString(""),
                )
            }.orEmpty()

    internal fun isEnum(property: ApiClassProperty): Boolean =
        (!property.asSchema?.enum.isNullOrEmpty()) ||
            property.asReference?.let { !schemas[getRefClassName(it)]?.enum.isNullOrEmpty() } == true

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

    private fun OpenAPIV3Operation.allReferences(): Set<OpenAPIV3Reference> =
        setOfNotNull(requestBody as? OpenAPIV3Reference) +
            (
                (requestBody as? OpenAPIV3RequestBody)?.content?.values?.flatMap { it.schema.allReferences() }
                    ?: emptyList()
            ) +
            (parameters?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
            (
                parameters?.flatMap {
                    (it as? OpenAPIV3Parameter)?.content?.values?.flatMap { v -> v.schema.allReferences() }
                        ?: emptyList()
                }
                    ?: emptyList()
            ) +
            (responses?.values?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
            (
                responses
                    ?.values
                    ?.flatMap {
                        (it as? OpenAPIV3Response)?.content?.values?.flatMap { v -> v.schema.allReferences() }
                            ?: emptyList()
                    } ?: emptyList()
            )

    private fun OpenAPIV3SchemaOrReference?.allReferences(): Set<OpenAPIV3Reference> =
        when (this) {
            is OpenAPIV3Schema -> allReferences()
            is OpenAPIV3Reference -> setOf(this)
            null -> emptySet()
        }

    private fun OpenAPIV3Schema.allReferences(): Set<OpenAPIV3Reference> =
        setOfNotNull(
            not as? OpenAPIV3Reference,
            items as? OpenAPIV3Reference,
            additionalProperties as? OpenAPIV3Reference,
        ) +
            (oneOf?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
            (anyOf?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
            (allOf?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
            (properties?.values?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
            (
                properties?.values?.flatMap { (it as? OpenAPIV3Schema)?.allReferences() ?: emptyList() }
                    ?: emptyList()
            ) +
            ((items as? OpenAPIV3Schema)?.allReferences() ?: emptyList())

    private fun getRefClassName(refValue: String): String = refValue.substringAfterLast("/")

    private fun getRefClassName(ref: OpenAPIV3Reference): String = getRefClassName(ref.ref.value)

    private fun resolveParameter(parameterOrReference: OpenAPIV3ParameterOrReference): OpenAPIV3Parameter? =
        when (parameterOrReference) {
            is OpenAPIV3Parameter -> {
                parameterOrReference
            }

            is OpenAPIV3Reference -> {
                val refName = getRefClassName(parameterOrReference)
                val resolved = components?.parameters?.get(refName)
                if (resolved == null || resolved === parameterOrReference) {
                    null
                } else {
                    resolveParameter(resolved)
                }
            }
        }

    internal fun getComponentParameter(parameterOrReference: OpenAPIV3ParameterOrReference): OpenAPIV3Parameter? =
        when (parameterOrReference) {
            is OpenAPIV3Reference -> {
                val refName = getRefClassName(parameterOrReference)
                val resolved = components?.parameters?.get(refName) ?: return null
                resolveParameter(resolved)
            }

            is OpenAPIV3Parameter -> {
                parameterOrReference
            }
        }

    internal fun resolveSchema(schemaOrReference: OpenAPIV3SchemaOrReference?): OpenAPIV3Schema? =
        when (schemaOrReference) {
            is OpenAPIV3Schema -> {
                schemaOrReference
            }

            is OpenAPIV3Reference -> {
                val refName = getRefClassName(schemaOrReference)
                components?.schemas?.get(refName) as? OpenAPIV3Schema
            }

            null -> {
                null
            }
        }

    private fun getClassName(
        name: String,
        schemaOrReference: OpenAPIV3Schema,
        type: OpenAPIV3Type?,
    ): TypeName =
        when (type) {
            OpenAPIV3Type.STRING -> {
                if (schemaOrReference.enum?.isNotEmpty() == true) {
                    ClassName("", name.sanitizeToIdentifier().snakeToCamelCase().capitalize())
                } else {
                    STRING
                }
            }

            OpenAPIV3Type.NUMBER -> {
                when (schemaOrReference.format) {
                    "float" -> FLOAT
                    else -> DOUBLE
                }
            }

            OpenAPIV3Type.INTEGER -> {
                when (schemaOrReference.format) {
                    "int32" -> INT
                    else -> LONG
                }
            }

            OpenAPIV3Type.BOOLEAN -> {
                BOOLEAN
            }

            OpenAPIV3Type.ARRAY -> {
                (if (schemaOrReference.uniqueItems == true) SET else LIST)
                    .parameterizedBy(
                        listOf(
                            getClassName(
                                name,
                                schemaOrReference.items ?: error("null items for $schemaOrReference"),
                            ),
                        ),
                    )
            }

            OpenAPIV3Type.OBJECT -> {
                val additional =
                    schemaOrReference.additionalProperties?.run {
                        when (this) {
                            is OpenAPIV3Boolean -> error("boolean not allowed for $schemaOrReference")
                            is OpenAPIV3Schema -> this
                            is OpenAPIV3Reference -> this
                        }
                    }
                if (additional == null) {
                    JsonElement::class.asClassName()
                } else {
                    MAP
                        .parameterizedBy(
                            listOf(
                                String::class.asClassName(),
                                getClassName(name, additional),
                            ),
                        )
                }
            }

            else -> {
                val oneOf = schemaOrReference.oneOf
                if (oneOf?.isNotEmpty() == true) {
                    val refs = oneOf.filterIsInstance<OpenAPIV3Reference>()
                    val hasNullSchema = oneOf.any { it.isNullSchema() }
                    when {
                        refs.size == 1 && hasNullSchema -> {
                            getClassName(name, refs.first()).copy(nullable = true)
                        }

                        refs.size == 1 -> {
                            getClassName(name, refs.first())
                        }

                        else -> {
                            val refNames = refs.map { getRefClassName(it) }
                            val parentName =
                                requestBodySealedParents.entries
                                    .firstOrNull { it.value.toSet() == refNames.toSet() }
                                    ?.key
                            if (parentName != null) {
                                ClassName(configuration.resolvedModelPackage, parentName)
                            } else {
                                JsonElement::class.asClassName()
                            }
                        }
                    }
                } else {
                    JsonElement::class.asClassName()
                }
            }
        }

    fun getClassName(
        name: String,
        schemaOrReference: OpenAPIV3SchemaOrReference,
    ): TypeName =
        when (schemaOrReference) {
            is OpenAPIV3Reference -> {
                ClassName(
                    configuration.resolvedModelPackage,
                    getRefClassName(schemaOrReference).let {
                        if (it == "Companion") {
                            "${configuration.resolvedModelPackage}.$it"
                        } else {
                            it
                        }
                    },
                )
            }

            is OpenAPIV3Schema -> {
                when (val type = schemaOrReference.type) {
                    is OpenAPIV3SingleType -> {
                        getClassName(name, schemaOrReference, type.value)
                    }

                    is OpenAPIV3TypeArray -> {
                        if (type.values.size > 1 && (type.values.size > 2 || !type.values.contains(OpenAPIV3Type.NULL))) {
                            logger.warn { "For now only first type is handled for $schemaOrReference" }
                        }
                        getClassName(name, schemaOrReference, type.values.first { it != OpenAPIV3Type.NULL })
                    }

                    null -> {
                        getClassName(name, schemaOrReference, null)
                    }
                }
            }
        }

    fun getClassProperty(
        name: String,
        schemaOrReference: OpenAPIV3SchemaOrReference,
        parentSchema: OpenAPIV3Schema,
    ): ApiClassProperty =
        ApiClassProperty(
            name,
            getClassName(name, schemaOrReference).let { c ->
                if (parentSchema.required?.contains(name) == true) c else c.copy(nullable = true)
            },
            schemaOrReference,
        )

    private fun OpenAPIV3SchemaOrReference.isNullSchema(): Boolean =
        this is OpenAPIV3Schema &&
            when (val t = type) {
                is OpenAPIV3SingleType -> t.value == OpenAPIV3Type.NULL
                is OpenAPIV3TypeArray -> t.values.all { it == OpenAPIV3Type.NULL }
                null -> false
            }
}
