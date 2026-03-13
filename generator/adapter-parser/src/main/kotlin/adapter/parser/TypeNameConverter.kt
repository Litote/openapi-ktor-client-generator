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
import org.litote.openapi.ktor.client.generator.domain.DomainType

internal fun TypeName.toDomainType(modelPackage: String): DomainType {
    val nonNullable = if (isNullable) copy(nullable = false) else this
    val base: DomainType =
        when {
            nonNullable == STRING -> {
                DomainType.Primitive(DomainType.Primitive.Kind.STRING)
            }

            nonNullable == INT -> {
                DomainType.Primitive(DomainType.Primitive.Kind.INT)
            }

            nonNullable == LONG -> {
                DomainType.Primitive(DomainType.Primitive.Kind.LONG)
            }

            nonNullable == DOUBLE -> {
                DomainType.Primitive(DomainType.Primitive.Kind.DOUBLE)
            }

            nonNullable == FLOAT -> {
                DomainType.Primitive(DomainType.Primitive.Kind.FLOAT)
            }

            nonNullable == BOOLEAN -> {
                DomainType.Primitive(DomainType.Primitive.Kind.BOOLEAN)
            }

            nonNullable == JsonElement::class.asClassName() -> {
                DomainType.JsonType()
            }

            nonNullable is ClassName && nonNullable.packageName == modelPackage -> {
                DomainType.ModelReference(nonNullable.simpleName)
            }

            nonNullable is ClassName && nonNullable.packageName == "" -> {
                DomainType.InlineType(nonNullable.simpleName, isEnum = false)
            }

            nonNullable is ParameterizedTypeName -> {
                val arg0 = nonNullable.typeArguments[0]
                when (nonNullable.rawType) {
                    LIST -> DomainType.ListType(arg0.toDomainType(modelPackage))
                    SET -> DomainType.SetType(arg0.toDomainType(modelPackage))
                    MAP -> DomainType.MapType(nonNullable.typeArguments[1].toDomainType(modelPackage))
                    else -> DomainType.JsonType()
                }
            }

            else -> {
                DomainType.JsonType()
            }
        }
    return if (isNullable) base.asNullable() else base
}
