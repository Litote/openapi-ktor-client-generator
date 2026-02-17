package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeSpec.Companion.anonymousClassBuilder
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Type
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.litote.openapi.ktor.client.generator.shared.capitalize
import org.litote.openapi.ktor.client.generator.shared.sanitizeToIdentifier
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase
import java.io.File

public class ApiModelGenerator internal constructor(
    public val apiModel: ApiModel,
) {
    private companion object {
        private val logger = KotlinLogging.logger {}
        val serializerName: MemberName = MemberName("kotlinx.serialization.builtins", "serializer")
    }

    /**
     * If set, this value will be used as the default value for enum properties.
     * This is useful when the API may return values that are not defined in the enum, and you want to avoid deserialization errors by providing a default value.
     * The default enum value is added as the last constant in the generated enum class and is used when deserialization encounters an unknown value.
     */
    public var defaultEnumValue: String? = null

    internal fun buildModel(
        name: String,
        schema: OpenAPIV3Schema,
    ): TypeSpec {
        val properties: List<ApiClassProperty> =
            schema.properties
                ?.asSequence()
                ?.mapNotNull { (name, schemaOrReference) ->
                    if (schemaOrReference is OpenAPIV3Schema && schemaOrReference.deprecated == true) {
                        null
                    } else {
                        apiModel.getClassProperty(name, schemaOrReference, schema)
                    }
                }?.sortedBy { it.camelCaseName }
                ?.toList()
                ?: emptyList()

        return if (properties.isEmpty()) {
            if (schema.enum.isNullOrEmpty()) {
                TypeSpec
                    .objectBuilder(name)
                    .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
                    .build()
            } else {
                TypeSpec
                    .enumBuilder(name)
                    .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
                    .apply {
                        schema.enum?.forEach { e ->
                            e.contentOrNull?.let { name ->
                                addEnumConstant(
                                    name.sanitizeToIdentifier().snakeToCamelCase().uppercase(),
                                    anonymousClassBuilder()
                                        .addAnnotation(
                                            AnnotationSpec
                                                .builder(SerialName::class)
                                                .addMember("%S", name)
                                                .build(),
                                        ).build(),
                                )
                            }
                        }
                        defaultEnumValue?.apply {
                            addEnumConstant(this)
                        }
                        addFunction(serialNameFun(name))
                    }.build()
            }
        } else {
            TypeSpec
                .classBuilder(name)
                .addModifiers(KModifier.DATA)
                .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .apply {
                            properties.forEach { property ->
                                addParameter(
                                    ParameterSpec
                                        .builder(property.camelCaseName, property.type)
                                        .apply {
                                            val isEnum = apiModel.isEnum(property)
                                            (
                                                when {
                                                    property.asSchema?.default != null -> {
                                                        (property.asSchema?.default as? JsonPrimitive)?.content?.let {
                                                            if (it == "null" || !isEnum) {
                                                                it
                                                            } else {
                                                                "${(property.type as ClassName).simpleName}.$it"
                                                            }
                                                        }
                                                    }

                                                    property.type.isNullable -> {
                                                        "null"
                                                    }

                                                    else -> {
                                                        if (isEnum &&
                                                            defaultEnumValue != null
                                                        ) {
                                                            "${(property.type as ClassName).simpleName}.$defaultEnumValue"
                                                        } else {
                                                            null
                                                        }
                                                    }
                                                }
                                            )?.apply {
                                                val format =
                                                    if (this != "null") {
                                                        property.asSchema?.let {
                                                            if (it.firstType == OpenAPIV3Type.STRING) {
                                                                if (isEnum) {
                                                                    "%L"
                                                                } else {
                                                                    "%S"
                                                                }
                                                            } else {
                                                                "%L"
                                                            }
                                                        }
                                                            ?: "%L"
                                                    } else {
                                                        "%L"
                                                    }
                                                defaultValue(format, this)
                                            }
                                        }.build(),
                                )
                            }
                        }.build(),
                ).apply {
                    properties.forEach { property ->
                        addProperty(
                            PropertySpec
                                .builder(property.camelCaseName, property.type)
                                .initializer(property.camelCaseName)
                                .apply {
                                    if (property.needsSerialName) {
                                        addAnnotation(
                                            AnnotationSpec
                                                .builder(SerialName::class)
                                                .addMember("%S", property.initialName)
                                                .build(),
                                        )
                                    }
                                }.build(),
                        )

                        val enum = property.asSchema?.enum ?: (property.asSchema?.items as? OpenAPIV3Schema)?.enum
                        if (!enum.isNullOrEmpty()) {
                            val enumName = property.camelCaseName.capitalize()
                            addType(
                                TypeSpec
                                    .enumBuilder(enumName)
                                    .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
                                    .apply {
                                        enum.forEach { e ->
                                            e.contentOrNull?.let { name ->
                                                addEnumConstant(
                                                    name.sanitizeToIdentifier().snakeToCamelCase().uppercase(),
                                                    anonymousClassBuilder()
                                                        .addAnnotation(
                                                            AnnotationSpec
                                                                .builder(SerialName::class)
                                                                .addMember("%S", name)
                                                                .build(),
                                                        ).build(),
                                                )
                                            }
                                        }
                                        defaultEnumValue?.apply {
                                            addEnumConstant(this)
                                        }
                                        addFunction(serialNameFun(enumName))
                                    }.build(),
                            )
                        }
                    }
                }.build()
        }
    }

    private fun serialNameFun(enumName: String): FunSpec =
        FunSpec
            .builder("serialName")
            .returns(STRING)
            .addStatement("return $enumName.%M().descriptor.getElementName(this.ordinal)", serializerName)
            .build()

    internal fun writeFile(
        name: String,
        typeSpec: TypeSpec,
    ) {
        val fileSpec = FileSpec.builder(apiModel.configuration.modelPackage, name).addTypes(listOf(typeSpec)).build()

        val basePath = File(apiModel.outputDirectory).resolve("src/main/kotlin")
        logger.debug { "Writing $name to $basePath" }
        fileSpec.writeTo(basePath)
    }
}
