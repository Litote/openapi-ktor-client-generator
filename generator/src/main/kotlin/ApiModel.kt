package org.litote.openapi.ktor.client.generator

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
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Type
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.litote.openapi.ktor.client.generator.shared.capitalize
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase
import org.litote.openapi.ktor.client.generator.shared.toOrNull
import java.nio.file.Files
import java.nio.file.Path

public class ApiModel private constructor(
    public val model: OpenAPIV3Model,
    public val configuration: ApiGeneratorConfiguration
) {

    internal companion object {
        fun parseOpenApiFile(configuration: ApiGeneratorConfiguration): ApiModel {
            val openApi = OpenAPIV3(
                Json {
                    ignoreUnknownKeys = true
                }
            )
            val openApiFile = configuration.openApiFile
            println("Parsing $openApiFile")
            val json = Files.readString(Path.of(openApiFile))

            return ApiModel(openApi.decodeFromString(json), configuration)
        }
    }

    public val outputDirectory: String get() = configuration.outputDirectory
    public val serverUrl: String = model.servers?.firstOrNull()?.url ?: "http://localhost:8080"

    public val pathsByTags: Map<String, List<ApiOperation>> =
        model.paths.entries.asSequence()
            .flatMap { (path, item) ->
                listOfNotNull(
                    "get" toOrNull item.get,
                    "put" toOrNull item.put,
                    "post" toOrNull item.post,
                    "delete" toOrNull item.delete,
                    "options" toOrNull item.options,
                    "head" toOrNull item.head,
                    "patch" toOrNull item.patch,
                    "trace" toOrNull item.trace
                )
                    .asSequence()
                    .flatMap { (method, operation) ->
                        (
                                operation
                                    .tags
                                    .takeUnless { it.isNullOrEmpty() }
                                    ?: listOf("")
                                )
                            .asSequence()
                            .filterNotNull()
                            .distinct()
                            .map { it to ApiOperation(path.value, method, operation) }
                    }
            }
            .filter { (_, operation) -> configuration.operationFilter(operation) }
            .groupBy(keySelector = { it.first }, valueTransform = { it.second })

    public val components: OpenAPIV3Components? get() = model.components

    public val schemaParentMap: Map<String, Set<String>> =
        components
            ?.schemas
            ?.mapValues { (_, v) -> v.allReferences().map { getRefClassName(it) }.toSet() }
            ?: emptyMap()

    public val schemas: Map<String, OpenAPIV3Schema> =
        (pathsByTags
            .values
            .flatten()
            .distinct()
            .flatMap { o ->
                o.operation.allReferences().map { getRefClassName(it) }
            }
            .toSet()
            .run {
                val set = mutableSetOf<String>()
                forEach { addChildren(set, it) }
                set
            }
                ).let { set ->
                (components
                    ?.schemas
                    ?.filterValues { it is OpenAPIV3Schema }
                    ?.mapValues { it.value as OpenAPIV3Schema }
                    ?: emptyMap())
                    .filterKeys { set.contains(it) }
            }


    public val componentParameters: List<OpenAPIV3Parameter> =
        components?.parameters
            ?.values
            ?.mapNotNull { resolveParameter(it) }
            .orEmpty()

    internal fun isEnum(property: ApiClassProperty): Boolean =
        (!property.asSchema?.enum.isNullOrEmpty()) || property.asReference?.let { !schemas[getRefClassName(it)]?.enum.isNullOrEmpty() } == true

    private fun addChildren(set: MutableSet<String>, name: String) {
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
                ((requestBody as? OpenAPIV3RequestBody)?.content?.values?.mapNotNull { it.schema as? OpenAPIV3Reference }
                    ?: emptyList()) +
                (parameters?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
                (responses?.values?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
                (responses?.values?.flatMap {
                    (it as? OpenAPIV3Response)?.content?.values?.map { it.schema as? OpenAPIV3Reference } ?: emptyList()
                }?.filterNotNull() ?: emptyList()) //+
    //(callbacks?.values?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
    //(callbacks?.values?.flatMap { (it as? OpenAPIV3Callbacks)?.entries?.values?.map { it.schema as? OpenAPIV3Reference} ?: emptyList() }?.filterNotNull() ?: emptyList())

    private fun OpenAPIV3SchemaOrReference.allReferences(): Set<OpenAPIV3Reference> =
        (this as? OpenAPIV3Schema)?.allReferences() ?: emptySet()

    private fun OpenAPIV3Schema.allReferences(): Set<OpenAPIV3Reference> =
        setOfNotNull(
            not as? OpenAPIV3Reference,
            items as? OpenAPIV3Reference,
            additionalProperties as? OpenAPIV3Reference
        ) +
                (oneOf?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
                (anyOf?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
                (allOf?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
                (properties?.values?.mapNotNull { it as? OpenAPIV3Reference } ?: emptyList()) +
                (properties?.values?.flatMap { (it as? OpenAPIV3Schema)?.allReferences() ?: emptyList() }
                    ?: emptyList()) +
                ((items as? OpenAPIV3Schema)?.allReferences() ?: emptyList())

    private fun getRefClassName(refValue: String): String = refValue.substringAfterLast("/")

    private fun getRefClassName(ref: OpenAPIV3Reference): String = getRefClassName(ref.ref.value)

    private fun resolveParameter(parameterOrReference: OpenAPIV3ParameterOrReference): OpenAPIV3Parameter? =
        when (parameterOrReference) {
            is OpenAPIV3Parameter -> parameterOrReference
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

    public fun getClassName(name: String, schemaOrReference: OpenAPIV3SchemaOrReference): TypeName =
        when (schemaOrReference) {
            is OpenAPIV3Reference -> ClassName(
                configuration.modelPackage,
                getRefClassName(schemaOrReference).let {
                    if (it == "Companion") {
                        "${configuration.modelPackage}.$it"
                    } else {
                        it
                    }
                }
            )

            is OpenAPIV3Schema ->
                when (schemaOrReference.type) {

                    OpenAPIV3Type.STRING -> {
                        if (schemaOrReference.enum?.isNotEmpty() == true) {
                            ClassName("", name.snakeToCamelCase().capitalize())
                        } else {
                            STRING
                        }
                    }

                    OpenAPIV3Type.NUMBER -> when (schemaOrReference.format) {
                        "float" -> FLOAT
                        else -> DOUBLE
                    }

                    OpenAPIV3Type.INTEGER -> when (schemaOrReference.format) {
                        "int32" -> INT
                        else -> LONG
                    }

                    OpenAPIV3Type.BOOLEAN -> BOOLEAN
                    OpenAPIV3Type.ARRAY -> (if (schemaOrReference.uniqueItems == true) SET else LIST)
                        .parameterizedBy(
                            listOf(
                                getClassName(
                                    name,
                                    schemaOrReference.items ?: error("null items for $schemaOrReference")
                                )
                            )
                        )

                    OpenAPIV3Type.OBJECT -> {
                        val additional = schemaOrReference.additionalProperties?.run {
                            when (this) {
                                is OpenAPIV3Boolean -> error("boolean not allowed for $schemaOrReference")
                                is OpenAPIV3Schema -> this
                                is OpenAPIV3Reference -> this
                            }
                        }
                        if (additional == null) {
                            // Fallback for free-form objects without declared properties.
                            JsonElement::class.asClassName()
                        } else {
                            MAP
                                .parameterizedBy(
                                    listOf(
                                        String::class.asClassName(),
                                        getClassName(name, additional)
                                    )
                                )
                        }
                    }

                    else -> {
                        val oneOf = schemaOrReference.oneOf
                        if (oneOf?.isNotEmpty() == true) {
                            if (oneOf.size == 1) {
                                getClassName(name, oneOf.first())
                            } else {
                                // Fallback for polymorphic responses.
                                JsonElement::class.asClassName()
                            }
                        } else {
                            // Fallback for other responses.
                            JsonElement::class.asClassName()
                        }
                    }
                }
        }

    public fun getClassProperty(name: String, schemaOrReference: OpenAPIV3SchemaOrReference, parentSchema: OpenAPIV3Schema): ApiClassProperty =
        ApiClassProperty(
            name,
            getClassName(name, schemaOrReference).let { c ->
                if (parentSchema.required?.contains(name) == true) c else c.copy(nullable = true)
            },
            schemaOrReference
        )
}
