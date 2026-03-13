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
import kotlinx.serialization.json.JsonElement
import org.litote.openapi.ktor.client.generator.domain.DefaultValue
import org.litote.openapi.ktor.client.generator.domain.DomainType
import org.litote.openapi.ktor.client.generator.domain.GeneratedFile

/** Converts a KotlinPoet [FileSpec] to a domain [GeneratedFile] by rendering its content to a string. */
internal fun FileSpec.toGeneratedFile(): GeneratedFile = GeneratedFile(packageName, name, toString())

public fun isConstSupported(typeName: TypeName): Boolean = typeName.isPrimitive()

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

internal fun DomainType.toTypeName(
    modelPackage: String,
    modelPackageOverrides: Map<String, String> = emptyMap(),
): TypeName {
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
                LIST.parameterizedBy(element.toTypeName(modelPackage, modelPackageOverrides))
            }

            is DomainType.SetType -> {
                SET.parameterizedBy(element.toTypeName(modelPackage, modelPackageOverrides))
            }

            is DomainType.MapType -> {
                MAP.parameterizedBy(STRING, value.toTypeName(modelPackage, modelPackageOverrides))
            }

            is DomainType.ModelReference -> {
                ClassName(modelPackageOverrides.getOrDefault(name, modelPackage), name)
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
