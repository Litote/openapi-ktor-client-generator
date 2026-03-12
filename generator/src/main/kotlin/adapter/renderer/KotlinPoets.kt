package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SchemaOrReference
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SingleType
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Type
import community.flock.kotlinx.openapi.bindings.OpenAPIV3TypeArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import org.litote.openapi.ktor.client.generator.domain.DefaultValue
import org.litote.openapi.ktor.client.generator.domain.DomainType
import org.litote.openapi.ktor.client.generator.domain.GeneratedFile
import org.litote.openapi.ktor.client.generator.shared.capitalize
import org.litote.openapi.ktor.client.generator.shared.sanitizeToIdentifier
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase
import org.litote.openapi.ktor.client.generator.shared.toUpperSnakeCase
import kotlin.text.uppercase

/** Converts a KotlinPoet [FileSpec] to a domain [GeneratedFile] by rendering its content to a string. */
internal fun FileSpec.toGeneratedFile(): GeneratedFile = GeneratedFile(packageName, name, toString())

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
    val isEnum = !schema.enum.isNullOrEmpty()
    return when {
        isEnum -> {
            defaultValue.contentOrNull?.let {
                CodeBlock.of("%L.%L", (typeName as ClassName).simpleName, it.enumFieldName)
            }
        }

        typeName.isString() -> {
            defaultValue.contentOrNull?.let { CodeBlock.of("%S", it) }
        }

        typeName.isBoolean() -> {
            defaultValue.booleanOrNull?.let { CodeBlock.of("%L", it) }
        }

        typeName.isLong() -> {
            defaultValue.longOrNull?.let { CodeBlock.of("%L", it) }
        }

        typeName.isDouble() -> {
            defaultValue.doubleOrNull?.let { CodeBlock.of("%L", it) }
        }

        typeName.isFloat() -> {
            defaultValue.floatOrNull?.let { CodeBlock.of("%LF", it) }
        }

        typeName.isInt() -> {
            defaultValue.intOrNull?.let { CodeBlock.of("%L", it) }
        }

        else -> {
            null
        }
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

internal val ClassName.nonNullableName: String get() = if (isNullable) simpleName.removeSuffix("?") else simpleName

internal val TypeSpec.nonNullableName: String? get() = name?.removeSuffix("?")

internal fun TypeSpec.hasSameName(name: TypeName): Boolean = (name as? ClassName)?.nonNullableName == nonNullableName

internal fun TypeSpec.hasSameName(spec: TypeSpec): Boolean = spec.nonNullableName == nonNullableName

internal val OpenAPIV3Schema.firstType: OpenAPIV3Type?
    get() =
        type?.run {
            when (this) {
                is OpenAPIV3SingleType -> value
                is OpenAPIV3TypeArray -> values.first()
            }
        }

internal val String.enumFieldName: String get() = sanitizeToIdentifier().toUpperSnakeCase()

internal fun DefaultValue.toCodeBlock(): CodeBlock =
    when (this) {
        is DefaultValue.StringDefault -> CodeBlock.of("%S", value)
        is DefaultValue.BooleanDefault -> CodeBlock.of("%L", value)
        is DefaultValue.IntDefault -> CodeBlock.of("%L", value)
        is DefaultValue.LongDefault -> CodeBlock.of("%L", value)
        is DefaultValue.DoubleDefault -> CodeBlock.of("%L", value)
        is DefaultValue.FloatDefault -> CodeBlock.of("%LF", value)
        is DefaultValue.EnumDefault -> CodeBlock.of("%L.%L", typeName, enumValue)
    }

internal fun DomainType.toTypeName(modelPackage: String): TypeName {
    val base: TypeName =
        when (this) {
            is DomainType.Primitive -> {
                when (kind) {
                    DomainType.Primitive.Kind.STRING -> STRING
                    DomainType.Primitive.Kind.INT -> INT
                    DomainType.Primitive.Kind.LONG -> LONG
                    DomainType.Primitive.Kind.DOUBLE -> DOUBLE
                    DomainType.Primitive.Kind.FLOAT -> FLOAT
                    DomainType.Primitive.Kind.BOOLEAN -> BOOLEAN
                }
            }

            is DomainType.ListType -> {
                LIST.parameterizedBy(element.toTypeName(modelPackage))
            }

            is DomainType.SetType -> {
                SET.parameterizedBy(element.toTypeName(modelPackage))
            }

            is DomainType.MapType -> {
                MAP.parameterizedBy(STRING, value.toTypeName(modelPackage))
            }

            is DomainType.ModelReference -> {
                ClassName(modelPackage, name)
            }

            is DomainType.InlineType -> {
                ClassName("", name)
            }

            is DomainType.JsonType -> {
                JsonElement::class.asClassName()
            }
        }
    return if (nullable) base.copy(nullable = true) else base
}
