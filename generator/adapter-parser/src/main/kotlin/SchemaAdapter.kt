package org.litote.openapi.ktor.client.generator.adapter.parser

import community.flock.kotlinx.openapi.bindings.MediaType
import community.flock.kotlinx.openapi.bindings.MediaTypeObject
import community.flock.kotlinx.openapi.bindings.OpenAPIV30Model
import community.flock.kotlinx.openapi.bindings.OpenAPIV30Parameter
import community.flock.kotlinx.openapi.bindings.OpenAPIV30ParameterLocation
import community.flock.kotlinx.openapi.bindings.OpenAPIV30RequestBody
import community.flock.kotlinx.openapi.bindings.OpenAPIV30Response
import community.flock.kotlinx.openapi.bindings.OpenAPIV30Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV30SecurityScheme
import community.flock.kotlinx.openapi.bindings.OpenAPIV30SecuritySchemeType
import community.flock.kotlinx.openapi.bindings.OpenAPIV30SingleType
import community.flock.kotlinx.openapi.bindings.OpenAPIV30Type
import community.flock.kotlinx.openapi.bindings.OpenAPIV30TypeArray
import community.flock.kotlinx.openapi.bindings.OpenAPIV31Model
import community.flock.kotlinx.openapi.bindings.OpenAPIV31Parameter
import community.flock.kotlinx.openapi.bindings.OpenAPIV31ParameterLocation
import community.flock.kotlinx.openapi.bindings.OpenAPIV31RequestBody
import community.flock.kotlinx.openapi.bindings.OpenAPIV31Response
import community.flock.kotlinx.openapi.bindings.OpenAPIV31Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV31SecurityScheme
import community.flock.kotlinx.openapi.bindings.OpenAPIV31SecuritySchemeType
import community.flock.kotlinx.openapi.bindings.OpenAPIV31SingleType
import community.flock.kotlinx.openapi.bindings.OpenAPIV31Type
import community.flock.kotlinx.openapi.bindings.OpenAPIV31TypeArray
import community.flock.kotlinx.openapi.bindings.OpenAPIV32Model
import community.flock.kotlinx.openapi.bindings.OpenAPIV32Parameter
import community.flock.kotlinx.openapi.bindings.OpenAPIV32ParameterLocation
import community.flock.kotlinx.openapi.bindings.OpenAPIV32RequestBody
import community.flock.kotlinx.openapi.bindings.OpenAPIV32Response
import community.flock.kotlinx.openapi.bindings.OpenAPIV32Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV32SecurityScheme
import community.flock.kotlinx.openapi.bindings.OpenAPIV32SecuritySchemeType
import community.flock.kotlinx.openapi.bindings.OpenAPIV32SingleType
import community.flock.kotlinx.openapi.bindings.OpenAPIV32Type
import community.flock.kotlinx.openapi.bindings.OpenAPIV32TypeArray
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Model
import community.flock.kotlinx.openapi.bindings.ParameterOrReference
import community.flock.kotlinx.openapi.bindings.Reference
import community.flock.kotlinx.openapi.bindings.RequestBodyOrReference
import community.flock.kotlinx.openapi.bindings.ResponseOrReference
import community.flock.kotlinx.openapi.bindings.Schema
import community.flock.kotlinx.openapi.bindings.SchemaOrReference
import community.flock.kotlinx.openapi.bindings.SecuritySchemeOrReference
import community.flock.kotlinx.openapi.bindings.Server
import org.litote.openapi.ktor.client.generator.domain.ParameterLocationSpec
import org.litote.openapi.ktor.client.generator.domain.SecuritySchemeLocationSpec
import org.litote.openapi.ktor.client.generator.domain.SecuritySchemeSpec

internal enum class ApiSchemaType { STRING, NUMBER, INTEGER, BOOLEAN, ARRAY, OBJECT, NULL }

internal val Schema.firstApiType: ApiSchemaType?
    get() =
        when (this) {
            is OpenAPIV30Schema -> {
                when (val t = type) {
                    is OpenAPIV30SingleType -> t.value.toApiSchemaType()
                    is OpenAPIV30TypeArray -> t.values.firstOrNull()?.toApiSchemaType()
                    null -> null
                }
            }

            is OpenAPIV31Schema -> {
                when (val t = type) {
                    is OpenAPIV31SingleType -> {
                        t.value.toApiSchemaType()
                    }

                    is OpenAPIV31TypeArray -> {
                        (
                            t.values.firstOrNull { it != OpenAPIV31Type.NULL }
                                ?: t.values.firstOrNull()
                        )?.toApiSchemaType()
                    }

                    null -> {
                        null
                    }
                }
            }

            is OpenAPIV32Schema -> {
                when (val t = type) {
                    is OpenAPIV32SingleType -> {
                        t.value.toApiSchemaType()
                    }

                    is OpenAPIV32TypeArray -> {
                        (
                            t.values.firstOrNull { it != OpenAPIV32Type.NULL }
                                ?: t.values.firstOrNull()
                        )?.toApiSchemaType()
                    }

                    null -> {
                        null
                    }
                }
            }

            else -> {
                null
            }
        }

internal val Schema.typeIncludesNull: Boolean
    get() =
        when (this) {
            is OpenAPIV30Schema -> {
                nullable == true ||
                    when (val t = type) {
                        is OpenAPIV30SingleType -> t.value == OpenAPIV30Type.NULL
                        is OpenAPIV30TypeArray -> t.values.contains(OpenAPIV30Type.NULL)
                        null -> false
                    }
            }

            is OpenAPIV31Schema -> {
                when (val t = type) {
                    is OpenAPIV31SingleType -> t.value == OpenAPIV31Type.NULL
                    is OpenAPIV31TypeArray -> t.values.contains(OpenAPIV31Type.NULL)
                    null -> false
                }
            }

            is OpenAPIV32Schema -> {
                when (val t = type) {
                    is OpenAPIV32SingleType -> t.value == OpenAPIV32Type.NULL
                    is OpenAPIV32TypeArray -> t.values.contains(OpenAPIV32Type.NULL)
                    null -> false
                }
            }

            else -> {
                false
            }
        }

internal val Schema.oneOfSchemas: List<SchemaOrReference>?
    get() =
        when (this) {
            is OpenAPIV30Schema -> oneOf
            is OpenAPIV31Schema -> oneOf
            is OpenAPIV32Schema -> oneOf
            else -> null
        }

internal val Schema.anyOfSchemas: List<SchemaOrReference>?
    get() =
        when (this) {
            is OpenAPIV30Schema -> anyOf
            is OpenAPIV31Schema -> anyOf
            is OpenAPIV32Schema -> anyOf
            else -> null
        }

internal val Schema.notSchema: SchemaOrReference?
    get() =
        when (this) {
            is OpenAPIV30Schema -> not
            is OpenAPIV31Schema -> not
            is OpenAPIV32Schema -> not
            else -> null
        }

internal val Schema.discriminatorPropertyName: String?
    get() =
        when (this) {
            is OpenAPIV30Schema -> discriminator?.propertyName
            is OpenAPIV31Schema -> discriminator?.propertyName
            is OpenAPIV32Schema -> discriminator?.propertyName
            else -> null
        }

internal val Schema.discriminatorMapping: Map<String, String>?
    get() =
        when (this) {
            is OpenAPIV30Schema -> discriminator?.mapping
            is OpenAPIV31Schema -> discriminator?.mapping
            is OpenAPIV32Schema -> discriminator?.mapping
            else -> null
        }

internal val Schema.isDeprecated: Boolean
    get() =
        when (this) {
            is OpenAPIV30Schema -> deprecated == true
            is OpenAPIV31Schema -> deprecated == true
            is OpenAPIV32Schema -> deprecated == true
            else -> false
        }

internal val Reference.refClassName: String
    get() = ref.value.substringAfterLast("/")

internal val community.flock.kotlinx.openapi.bindings.Parameter.isRequired: Boolean
    get() =
        when (this) {
            is OpenAPIV30Parameter -> required == true
            is OpenAPIV31Parameter -> required == true
            is OpenAPIV32Parameter -> required == true
            else -> false
        }

internal val community.flock.kotlinx.openapi.bindings.Parameter.parameterLocation: ParameterLocationSpec?
    get() =
        when (this) {
            is OpenAPIV30Parameter -> `in`.toLocationSpec()
            is OpenAPIV31Parameter -> `in`.toLocationSpec()
            is OpenAPIV32Parameter -> `in`.toLocationSpec()
            else -> null
        }

internal val community.flock.kotlinx.openapi.bindings.Parameter.parameterContent: Map<MediaType, MediaTypeObject>?
    get() =
        when (this) {
            is OpenAPIV30Parameter -> content
            is OpenAPIV31Parameter -> content
            is OpenAPIV32Parameter -> content
            else -> null
        }

internal val ResponseOrReference.responseContent: Map<MediaType, MediaTypeObject>?
    get() =
        when (this) {
            is OpenAPIV30Response -> content
            is OpenAPIV31Response -> content
            is OpenAPIV32Response -> content
            else -> null
        }

internal val RequestBodyOrReference.asRequestBody: community.flock.kotlinx.openapi.bindings.RequestBody?
    get() =
        when (this) {
            is OpenAPIV30RequestBody -> this
            is OpenAPIV31RequestBody -> this
            is OpenAPIV32RequestBody -> this
            else -> null
        }

internal val OpenAPIV3Model.apiServers: List<Server>?
    get() =
        when (this) {
            is OpenAPIV30Model -> servers
            is OpenAPIV31Model -> servers
            is OpenAPIV32Model -> servers
        }

internal val OpenAPIV3Model.componentSchemas: Map<String, SchemaOrReference>?
    get() =
        when (this) {
            is OpenAPIV30Model -> components?.schemas
            is OpenAPIV31Model -> components?.schemas
            is OpenAPIV32Model -> components?.schemas
        }

internal val OpenAPIV3Model.componentParameters: Map<String, ParameterOrReference>?
    get() =
        when (this) {
            is OpenAPIV30Model -> components?.parameters
            is OpenAPIV31Model -> components?.parameters
            is OpenAPIV32Model -> components?.parameters
        }

internal val OpenAPIV3Model.componentSecuritySchemes: Map<String, SecuritySchemeOrReference>?
    get() =
        when (this) {
            is OpenAPIV30Model -> components?.securitySchemes
            is OpenAPIV31Model -> components?.securitySchemes
            is OpenAPIV32Model -> components?.securitySchemes
        }

internal data class ApiKeySchemeData(
    val keyName: String,
    val location: SecuritySchemeLocationSpec,
)

internal val SecuritySchemeOrReference.apiKeySchemeData: ApiKeySchemeData?
    get() =
        when (this) {
            is OpenAPIV30SecurityScheme -> {
                if (type != OpenAPIV30SecuritySchemeType.API_KEY) return null
                val keyName = name ?: return null
                val inValue = `in` ?: return null
                val location = inValue.toSecuritySchemeLocation() ?: return null
                ApiKeySchemeData(keyName, location)
            }

            is OpenAPIV31SecurityScheme -> {
                if (type != OpenAPIV31SecuritySchemeType.API_KEY) return null
                val keyName = name ?: return null
                val inValue = `in` ?: return null
                val location = inValue.toSecuritySchemeLocation() ?: return null
                ApiKeySchemeData(keyName, location)
            }

            is OpenAPIV32SecurityScheme -> {
                if (type != OpenAPIV32SecuritySchemeType.API_KEY) return null
                val keyName = name ?: return null
                val inValue = `in` ?: return null
                val location = inValue.toSecuritySchemeLocation() ?: return null
                ApiKeySchemeData(keyName, location)
            }

            else -> {
                null
            }
        }

internal fun ApiKeySchemeData.toSpec(schemeName: String): SecuritySchemeSpec =
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

private fun String.toSecuritySchemeLocation(): SecuritySchemeLocationSpec? =
    when (this) {
        "header" -> SecuritySchemeLocationSpec.HEADER
        "query" -> SecuritySchemeLocationSpec.QUERY
        else -> null
    }

private fun OpenAPIV30Type.toApiSchemaType(): ApiSchemaType =
    when (this) {
        OpenAPIV30Type.STRING -> ApiSchemaType.STRING
        OpenAPIV30Type.NUMBER -> ApiSchemaType.NUMBER
        OpenAPIV30Type.INTEGER -> ApiSchemaType.INTEGER
        OpenAPIV30Type.BOOLEAN -> ApiSchemaType.BOOLEAN
        OpenAPIV30Type.ARRAY -> ApiSchemaType.ARRAY
        OpenAPIV30Type.OBJECT -> ApiSchemaType.OBJECT
        OpenAPIV30Type.NULL -> ApiSchemaType.NULL
    }

private fun OpenAPIV31Type.toApiSchemaType(): ApiSchemaType =
    when (this) {
        OpenAPIV31Type.STRING -> ApiSchemaType.STRING
        OpenAPIV31Type.NUMBER -> ApiSchemaType.NUMBER
        OpenAPIV31Type.INTEGER -> ApiSchemaType.INTEGER
        OpenAPIV31Type.BOOLEAN -> ApiSchemaType.BOOLEAN
        OpenAPIV31Type.ARRAY -> ApiSchemaType.ARRAY
        OpenAPIV31Type.OBJECT -> ApiSchemaType.OBJECT
        OpenAPIV31Type.NULL -> ApiSchemaType.NULL
    }

private fun OpenAPIV32Type.toApiSchemaType(): ApiSchemaType =
    when (this) {
        OpenAPIV32Type.STRING -> ApiSchemaType.STRING
        OpenAPIV32Type.NUMBER -> ApiSchemaType.NUMBER
        OpenAPIV32Type.INTEGER -> ApiSchemaType.INTEGER
        OpenAPIV32Type.BOOLEAN -> ApiSchemaType.BOOLEAN
        OpenAPIV32Type.ARRAY -> ApiSchemaType.ARRAY
        OpenAPIV32Type.OBJECT -> ApiSchemaType.OBJECT
        OpenAPIV32Type.NULL -> ApiSchemaType.NULL
    }

private fun OpenAPIV30ParameterLocation.toLocationSpec(): ParameterLocationSpec =
    when (this) {
        OpenAPIV30ParameterLocation.HEADER -> ParameterLocationSpec.HEADER
        OpenAPIV30ParameterLocation.PATH -> ParameterLocationSpec.PATH
        OpenAPIV30ParameterLocation.QUERY -> ParameterLocationSpec.QUERY
        OpenAPIV30ParameterLocation.COOKIE -> ParameterLocationSpec.QUERY
    }

private fun OpenAPIV31ParameterLocation.toLocationSpec(): ParameterLocationSpec =
    when (this) {
        OpenAPIV31ParameterLocation.HEADER -> ParameterLocationSpec.HEADER
        OpenAPIV31ParameterLocation.PATH -> ParameterLocationSpec.PATH
        OpenAPIV31ParameterLocation.QUERY -> ParameterLocationSpec.QUERY
        OpenAPIV31ParameterLocation.COOKIE -> ParameterLocationSpec.QUERY
    }

private fun OpenAPIV32ParameterLocation.toLocationSpec(): ParameterLocationSpec =
    when (this) {
        OpenAPIV32ParameterLocation.HEADER -> ParameterLocationSpec.HEADER
        OpenAPIV32ParameterLocation.PATH -> ParameterLocationSpec.PATH
        OpenAPIV32ParameterLocation.QUERY -> ParameterLocationSpec.QUERY
        OpenAPIV32ParameterLocation.COOKIE -> ParameterLocationSpec.QUERY
    }
