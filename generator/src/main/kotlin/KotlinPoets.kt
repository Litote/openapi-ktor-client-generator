package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SchemaOrReference
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase

public fun isConstSupported(typeName: TypeName): Boolean = typeName.isPrimitive()

private val constNameRegex = "[^A-Za-z0-9]+".toRegex()

public fun constName(name: String): String = name.replace(constNameRegex, "_").trim('_').uppercase()

public fun parameterTypeBaseName(name: String): String = name.replace(constNameRegex, "_")

public fun parameterVariableName(name: String): String {
    val normalized = parameterTypeBaseName(name)
    return normalized.snakeToCamelCase().replaceFirstChar { it.lowercase() }
}

public fun parameterDefaultLiteral(
    schemaOrReference: OpenAPIV3SchemaOrReference?,
    typeName: TypeName,
): CodeBlock? {
    val schema = schemaOrReference as? OpenAPIV3Schema ?: return null
    val defaultValue = schema.default as? JsonPrimitive ?: return null
    return when {
        typeName.isString() -> defaultValue.contentOrNull?.let { CodeBlock.of("%S", it) }
        typeName.isBoolean() -> defaultValue.booleanOrNull?.let { CodeBlock.of("%L", it) }
        typeName.isLong() -> defaultValue.longOrNull?.let { CodeBlock.of("%L", it) }
        typeName.isDouble() -> defaultValue.doubleOrNull?.let { CodeBlock.of("%L", it) }
        typeName.isFloat() -> defaultValue.floatOrNull?.let { CodeBlock.of("%L", it) }
        typeName.isInt() -> defaultValue.intOrNull?.let { CodeBlock.of("%L", it) }
        else -> null
    }
}

private val NULLABLE_STRING = STRING.copy(nullable = true)
private val NULLABLE_BOOLEAN = BOOLEAN.copy(nullable = true)
private val NULLABLE_LONG = LONG.copy(nullable = true)
private val NULLABLE_DOUBLE = DOUBLE.copy(nullable = true)
private val NULLABLE_FLOAT = FLOAT.copy(nullable = true)
private val NULLABLE_INT = INT.copy(nullable = true)

internal fun TypeName.isPrimitive(): Boolean =
    isString() ||
        isBoolean() ||
        isLong() ||
        isDouble() ||
        isFloat() ||
        isInt()

internal fun TypeName.isString(): Boolean = if (isNullable) this == NULLABLE_STRING else this == STRING

internal fun TypeName.isBoolean(): Boolean = if (isNullable) this == NULLABLE_BOOLEAN else this == BOOLEAN

internal fun TypeName.isLong(): Boolean = if (isNullable) this == NULLABLE_LONG else this == LONG

internal fun TypeName.isDouble(): Boolean = if (isNullable) this == NULLABLE_DOUBLE else this == DOUBLE

internal fun TypeName.isFloat(): Boolean = if (isNullable) this == NULLABLE_FLOAT else this == FLOAT

internal fun TypeName.isInt(): Boolean = if (isNullable) this == NULLABLE_INT else this == INT
