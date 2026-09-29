package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE_ARRAY
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
import org.litote.openapi.ktor.client.generator.port.StringFormatType

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

/** Returns the [StringFormatType] mapped for this type's `string` format, or `null` when it is rendered as [STRING]. */
internal fun DomainTypeSpec.stringFormatType(stringFormatTypes: Map<String, StringFormatType>): StringFormatType? =
    (this as? DomainTypeSpec.PrimitiveSpec)?.format?.let { stringFormatTypes[it] }

/** Renders a string literal, or `Type.parse("…")` when [formatType] maps it to a richer type. */
internal fun stringLiteralCodeBlock(
    value: String,
    formatType: StringFormatType?,
): CodeBlock =
    if (formatType == null) {
        CodeBlock.of("%S", value)
    } else {
        CodeBlock.of("%T.%N(%S)", ClassName.bestGuess(formatType.qualifiedName), formatType.parseFunction, value)
    }

internal fun DefaultValueSpec.toCodeBlock(formatType: StringFormatType? = null): CodeBlock =
    when (this) {
        is DefaultValueSpec.StringDefaultSpec -> stringLiteralCodeBlock(value, formatType)
        is DefaultValueSpec.BooleanDefaultSpec -> CodeBlock.of("%L", value)
        is DefaultValueSpec.IntDefaultSpec -> CodeBlock.of("%L", value)
        is DefaultValueSpec.LongDefaultSpec -> CodeBlock.of("%L", value)
        is DefaultValueSpec.DoubleDefaultSpec -> CodeBlock.of("%L", value)
        is DefaultValueSpec.FloatDefaultSpec -> CodeBlock.of("%LF", value)
        is DefaultValueSpec.EnumDefaultSpec -> CodeBlock.of("%L.%L", typeName, enumValue)
    }

/**
 * @param inlineTypeOwner class declaring the [DomainTypeSpec.InlineTypeSpec] types, for references from outside of it
 * (e.g. the client interface, referenced from its implementation). When null, inline types are referenced by simple name.
 */
internal fun DomainTypeSpec.toTypeName(
    modelPackage: String,
    modelPackageOverrides: Map<String, String> = emptyMap(),
    stringFormatTypes: Map<String, StringFormatType> = emptyMap(),
    inlineTypeOwner: ClassName? = null,
): TypeName {
    val base: TypeName =
        when (this) {
            is DomainTypeSpec.PrimitiveSpec -> {
                when (kind) {
                    DomainTypeSpec.PrimitiveSpec.KindSpec.STRING -> {
                        stringFormatType(stringFormatTypes)?.let { ClassName.bestGuess(it.qualifiedName) } ?: STRING
                    }

                    DomainTypeSpec.PrimitiveSpec.KindSpec.INT -> {
                        INT
                    }

                    DomainTypeSpec.PrimitiveSpec.KindSpec.LONG -> {
                        LONG
                    }

                    DomainTypeSpec.PrimitiveSpec.KindSpec.DOUBLE -> {
                        DOUBLE
                    }

                    DomainTypeSpec.PrimitiveSpec.KindSpec.FLOAT -> {
                        FLOAT
                    }

                    DomainTypeSpec.PrimitiveSpec.KindSpec.BOOLEAN -> {
                        BOOLEAN
                    }
                }
            }

            is DomainTypeSpec.ListTypeSpec -> {
                LIST.parameterizedBy(element.toTypeName(modelPackage, modelPackageOverrides, stringFormatTypes, inlineTypeOwner))
            }

            is DomainTypeSpec.SetTypeSpec -> {
                SET.parameterizedBy(element.toTypeName(modelPackage, modelPackageOverrides, stringFormatTypes, inlineTypeOwner))
            }

            is DomainTypeSpec.MapTypeSpec -> {
                MAP.parameterizedBy(STRING, value.toTypeName(modelPackage, modelPackageOverrides, stringFormatTypes, inlineTypeOwner))
            }

            is DomainTypeSpec.ModelReferenceSpec -> {
                ClassName(modelPackageOverrides.getOrDefault(name, modelPackage), name)
            }

            is DomainTypeSpec.InlineTypeSpec -> {
                inlineTypeOwner?.nestedClass(name) ?: ClassName("", name)
            }

            is DomainTypeSpec.JsonTypeSpec -> {
                JsonElement::class.asClassName()
            }

            is DomainTypeSpec.BinaryTypeSpec -> {
                BYTE_ARRAY
            }
        }
    return if (nullable) base.copy(nullable = true) else base
}
