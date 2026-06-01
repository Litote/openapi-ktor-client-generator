package org.litote.openapi.ktor.client.generator.adapter.parser

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import community.flock.kotlinx.openapi.bindings.OpenAPIV30Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV30SchemaOrReference
import community.flock.kotlinx.openapi.bindings.OpenAPIV30SingleType
import community.flock.kotlinx.openapi.bindings.OpenAPIV30Type
import community.flock.kotlinx.openapi.bindings.OpenAPIV30TypeArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import org.litote.openapi.ktor.client.generator.domain.enumFieldName
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase

private val nameRegex = "[^A-Za-z0-9]+".toRegex()

internal fun constName(name: String): String = name.replace(nameRegex, "_").trim('_').uppercase()

internal fun parameterTypeBaseName(name: String): String = name.replace(nameRegex, "_")

internal fun parameterVariableName(name: String): String {
    val normalized = parameterTypeBaseName(name)
    return normalized.snakeToCamelCase().replaceFirstChar { it.lowercase() }
}

internal fun parameterDefaultLiteral(
    schemaOrReference: OpenAPIV30SchemaOrReference?,
    typeName: TypeName,
): CodeBlock? {
    val schema = schemaOrReference as? OpenAPIV30Schema ?: return null
    val defaultValue = schema.default as? JsonPrimitive ?: return null
    val isEnum = !schema.enum.isNullOrEmpty()
    return when {
        isEnum -> defaultValue.contentOrNull?.let { CodeBlock.of("%L.%L", (typeName as ClassName).simpleName, it.enumFieldName) }
        typeName.isString() -> defaultValue.contentOrNull?.let { CodeBlock.of("%S", it) }
        typeName.isBoolean() -> defaultValue.booleanOrNull?.let { CodeBlock.of("%L", it) }
        typeName.isLong() -> defaultValue.longOrNull?.let { CodeBlock.of("%L", it) }
        typeName.isDouble() -> defaultValue.doubleOrNull?.let { CodeBlock.of("%L", it) }
        typeName.isFloat() -> defaultValue.floatOrNull?.let { CodeBlock.of("%LF", it) }
        typeName.isInt() -> defaultValue.intOrNull?.let { CodeBlock.of("%L", it) }
        else -> null
    }
}

internal val OpenAPIV30Schema.firstType: OpenAPIV30Type?
    get() =
        type?.run {
            when (this) {
                is OpenAPIV30SingleType -> value
                is OpenAPIV30TypeArray -> values.first()
            }
        }
