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
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.json.JsonElement
import org.litote.openapi.ktor.client.generator.domain.DefaultValueSpec
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.GeneratedFileSpec

/** Converts a KotlinPoet [FileSpec] to a domain [GeneratedFileSpec] by rendering its content to a string. */
internal fun FileSpec.toGeneratedFile(): GeneratedFileSpec = GeneratedFileSpec(packageName, name, toString())

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

internal fun DefaultValueSpec.toCodeBlock(): CodeBlock =
    when (this) {
        is DefaultValueSpec.StringDefaultSpec -> CodeBlock.of("%S", value)
        is DefaultValueSpec.BooleanDefaultSpec -> CodeBlock.of("%L", value)
        is DefaultValueSpec.IntDefaultSpec -> CodeBlock.of("%L", value)
        is DefaultValueSpec.LongDefaultSpec -> CodeBlock.of("%L", value)
        is DefaultValueSpec.DoubleDefaultSpec -> CodeBlock.of("%L", value)
        is DefaultValueSpec.FloatDefaultSpec -> CodeBlock.of("%LF", value)
        is DefaultValueSpec.EnumDefaultSpec -> CodeBlock.of("%L.%L", typeName, enumValue)
    }

internal fun DomainTypeSpec.toTypeName(
    modelPackage: String,
    modelPackageOverrides: Map<String, String> = emptyMap(),
): TypeName {
    val base: TypeName =
        when (this) {
            is DomainTypeSpec.PrimitiveSpec -> {
                when (kind) {
                    DomainTypeSpec.PrimitiveSpec.KindSpec.STRING -> STRING
                    DomainTypeSpec.PrimitiveSpec.KindSpec.INT -> INT
                    DomainTypeSpec.PrimitiveSpec.KindSpec.LONG -> LONG
                    DomainTypeSpec.PrimitiveSpec.KindSpec.DOUBLE -> DOUBLE
                    DomainTypeSpec.PrimitiveSpec.KindSpec.FLOAT -> FLOAT
                    DomainTypeSpec.PrimitiveSpec.KindSpec.BOOLEAN -> BOOLEAN
                }
            }

            is DomainTypeSpec.ListTypeSpec -> {
                LIST.parameterizedBy(element.toTypeName(modelPackage, modelPackageOverrides))
            }

            is DomainTypeSpec.SetTypeSpec -> {
                SET.parameterizedBy(element.toTypeName(modelPackage, modelPackageOverrides))
            }

            is DomainTypeSpec.MapTypeSpec -> {
                MAP.parameterizedBy(STRING, value.toTypeName(modelPackage, modelPackageOverrides))
            }

            is DomainTypeSpec.ModelReferenceSpec -> {
                ClassName(modelPackageOverrides.getOrDefault(name, modelPackage), name)
            }

            is DomainTypeSpec.InlineTypeSpec -> {
                ClassName("", name)
            }

            is DomainTypeSpec.JsonTypeSpec -> {
                JsonElement::class.asClassName()
            }
        }
    return if (nullable) base.copy(nullable = true) else base
}
