package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.asTypeName
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.longOrNull
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase

public fun isConstSupported(typeName: com.squareup.kotlinpoet.TypeName): Boolean =
    typeName == String::class.asTypeName() ||
        typeName == Boolean::class.asTypeName() ||
        typeName == Long::class.asTypeName() ||
        typeName == Double::class.asTypeName() ||
        typeName == Float::class.asTypeName() ||
        typeName == Int::class.asTypeName()

private val constNameRegex = "[^A-Za-z0-9]+".toRegex()

public fun constName(name: String): String = name.replace(constNameRegex, "_").trim('_').uppercase()

public fun parameterTypeBaseName(name: String): String = name.replace(constNameRegex, "_")

public fun parameterVariableName(name: String): String {
    val normalized = parameterTypeBaseName(name)
    return normalized.snakeToCamelCase().replaceFirstChar { it.lowercase() }
}

public fun parameterDefaultLiteral(
    schemaOrReference: community.flock.kotlinx.openapi.bindings.OpenAPIV3SchemaOrReference?,
    typeName: com.squareup.kotlinpoet.TypeName,
): CodeBlock? {
    val schema = schemaOrReference as? OpenAPIV3Schema ?: return null
    val defaultValue = schema.default as? JsonPrimitive ?: return null
    return when (typeName) {
        String::class.asTypeName() -> defaultValue.contentOrNull?.let { CodeBlock.of("%S", it) }
        Boolean::class.asTypeName() -> defaultValue.booleanOrNull?.let { CodeBlock.of("%L", it) }
        Long::class.asTypeName() -> defaultValue.longOrNull?.let { CodeBlock.of("%L", it) }
        Double::class.asTypeName() -> defaultValue.doubleOrNull?.let { CodeBlock.of("%L", it) }
        Float::class.asTypeName() -> defaultValue.floatOrNull?.let { CodeBlock.of("%L", it) }
        Int::class.asTypeName() -> defaultValue.longOrNull?.let { CodeBlock.of("%L", it.toInt()) }
        else -> null
    }
}
