package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Type
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.litote.openapi.ktor.client.generator.shared.capitalize
import java.io.File

public class ApiModelGenerator internal constructor(
    public val apiModel: ApiModel,
) {
    private companion object {
        private val logger = KotlinLogging.logger {}
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
                    .apply {
                        schema.enum?.forEach { e ->
                            e.contentOrNull?.let { name -> addEnumConstant(name) }
                        }
                        defaultEnumValue?.apply {
                            addEnumConstant(this)
                        }
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
                                                            if (it.type == OpenAPIV3Type.STRING) {
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

                        if (!property.asSchema?.enum.isNullOrEmpty()) {
                            addType(
                                TypeSpec
                                    .enumBuilder(property.camelCaseName.capitalize())
                                    .apply {
                                        property.asSchema?.enum?.forEach { e ->
                                            e.contentOrNull?.let { name -> addEnumConstant(name) }
                                        }
                                        defaultEnumValue?.apply {
                                            addEnumConstant(this)
                                        }
                                    }.build(),
                            )
                        }
                    }
                }.build()
        }
    }

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
