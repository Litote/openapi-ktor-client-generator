package org.litote.openapi.ktor.client.generator.adapter.parser

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.json.JsonElement
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec

internal fun TypeName.toDomainType(modelPackage: String): DomainTypeSpec {
    val nonNullable = if (isNullable) copy(nullable = false) else this
    val base: DomainTypeSpec =
        when {
            nonNullable == STRING -> {
                DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING)
            }

            nonNullable == INT -> {
                DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.INT)
            }

            nonNullable == LONG -> {
                DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.LONG)
            }

            nonNullable == DOUBLE -> {
                DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.DOUBLE)
            }

            nonNullable == FLOAT -> {
                DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.FLOAT)
            }

            nonNullable == BOOLEAN -> {
                DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.BOOLEAN)
            }

            nonNullable == JsonElement::class.asClassName() -> {
                DomainTypeSpec.JsonTypeSpec()
            }

            nonNullable is ClassName && nonNullable.packageName == modelPackage -> {
                DomainTypeSpec.ModelReferenceSpec(nonNullable.simpleName)
            }

            nonNullable is ClassName && nonNullable.packageName == "" -> {
                DomainTypeSpec.InlineTypeSpec(nonNullable.simpleName, isEnum = false)
            }

            nonNullable is ParameterizedTypeName -> {
                val arg0 = nonNullable.typeArguments[0]
                when (nonNullable.rawType) {
                    LIST -> DomainTypeSpec.ListTypeSpec(arg0.toDomainType(modelPackage))
                    SET -> DomainTypeSpec.SetTypeSpec(arg0.toDomainType(modelPackage))
                    MAP -> DomainTypeSpec.MapTypeSpec(nonNullable.typeArguments[1].toDomainType(modelPackage))
                    else -> DomainTypeSpec.JsonTypeSpec()
                }
            }

            else -> {
                DomainTypeSpec.JsonTypeSpec()
            }
        }
    return if (isNullable) base.asNullable() else base
}
