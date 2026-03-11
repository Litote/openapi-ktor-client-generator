package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeSpec.Companion.anonymousClassBuilder
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Reference
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
        val serializerName: MemberName = MemberName("kotlinx.serialization.builtins", "serializer")
        val jsonObject: ClassName = ClassName("kotlinx.serialization.json", "JsonObject")
        val jsonClassDiscriminator: ClassName = ClassName("kotlinx.serialization.json", "JsonClassDiscriminator")
        val experimentalSerializationApi: ClassName = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")
        val optIn: ClassName = ClassName("kotlin", "OptIn")
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
    ): TypeSpec? {
        // Schemas with oneOf of 2+ $ref entries become sealed classes.
        val oneOfRefs = schema.oneOf?.filterIsInstance<OpenAPIV3Reference>()
        if (oneOfRefs != null && oneOfRefs.size >= 2) {
            return buildSealedClass(name, schema)
        }

        // Flatten allOf: merge properties and required from inline object parts.
        val allOfParts: List<OpenAPIV3Schema> = schema.allOf?.mapNotNull { it as? OpenAPIV3Schema } ?: emptyList()
        val mergedProperties =
            (schema.properties ?: emptyMap()) +
                allOfParts.flatMap { it.properties?.entries ?: emptyList() }.associate { it.key to it.value }
        val mergedRequired = ((schema.required ?: emptyList()) + allOfParts.flatMap { it.required ?: emptyList() }).distinct()
        val effectiveSchema = if (allOfParts.isNotEmpty()) schema.copy(properties = mergedProperties, required = mergedRequired) else schema

        val rawProperties: List<ApiClassProperty> =
            effectiveSchema.properties
                ?.asSequence()
                ?.mapNotNull { (name, schemaOrReference) ->
                    if (schemaOrReference is OpenAPIV3Schema && schemaOrReference.deprecated == true) {
                        null
                    } else {
                        apiModel.getClassProperty(name, schemaOrReference, effectiveSchema)
                    }
                }?.sortedBy { it.camelCaseName }
                ?.toList()
                ?: emptyList()

        // Replace JsonElement with a nested ClassName for inline object properties.
        val properties: List<ApiClassProperty> =
            rawProperties.map { property ->
                val inlineSchema =
                    property.asSchema?.takeIf {
                        it.firstType == OpenAPIV3Type.OBJECT && !it.properties.isNullOrEmpty()
                    }
                if (inlineSchema != null) {
                    val nestedName = ClassName("", property.camelCaseName.capitalize())
                    property.copy(type = nestedName.copy(nullable = property.type.isNullable))
                } else {
                    property
                }
            }

        return if (properties.isEmpty()) {
            if (effectiveSchema.enum.isNullOrEmpty()) {
                val sealedParentName = apiModel.sealedSubTypes[name]
                TypeSpec
                    .objectBuilder(name)
                    .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
                    .apply {
                        if (sealedParentName != null) {
                            superclass(ClassName(apiModel.configuration.modelPackage, sealedParentName))
                            resolveDiscriminatorValue(name, sealedParentName)?.let { serialName ->
                                addAnnotation(
                                    AnnotationSpec
                                        .builder(SerialName::class)
                                        .addMember("%S", serialName)
                                        .build(),
                                )
                            }
                        }
                    }.build()
            } else {
                TypeSpec
                    .enumBuilder(name)
                    .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
                    .apply {
                        effectiveSchema.enum?.forEach { e ->
                            e.contentOrNull?.let { name ->
                                addEnumConstant(
                                    name.enumFieldName,
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
            if (effectiveSchema.discriminator != null) {
                null
            } else {
                val sealedParentName = apiModel.sealedSubTypes[name]
                TypeSpec
                    .classBuilder(name)
                    .addModifiers(KModifier.DATA)
                    .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
                    .apply {
                        if (sealedParentName != null) {
                            superclass(ClassName(apiModel.configuration.modelPackage, sealedParentName))
                            resolveDiscriminatorValue(name, sealedParentName)?.let { serialName ->
                                addAnnotation(
                                    AnnotationSpec
                                        .builder(SerialName::class)
                                        .addMember("%S", serialName)
                                        .build(),
                                )
                            }
                        }
                    }.primaryConstructor(
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
                                                        name.enumFieldName,
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
                            val inlineObjectSchema =
                                property.asSchema?.takeIf {
                                    it.firstType == OpenAPIV3Type.OBJECT && !it.properties.isNullOrEmpty()
                                }
                            if (inlineObjectSchema != null) {
                                val nestedName = property.camelCaseName.capitalize()
                                buildModel(nestedName, inlineObjectSchema)?.let { addType(it) }
                            }
                        }
                    }.build()
            }
        }
    }

    internal fun buildSealedClass(
        name: String,
        schema: OpenAPIV3Schema,
    ): TypeSpec =
        TypeSpec
            .classBuilder(name)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
            .apply {
                schema.discriminator?.propertyName?.let { discriminatorProp ->
                    addAnnotation(
                        AnnotationSpec
                            .builder(jsonClassDiscriminator)
                            .addMember("%S", discriminatorProp)
                            .build(),
                    )
                }
            }.build()

    /**
     * Resolves the `@SerialName` value for a sealed sub-class.
     *
     * Priority:
     * 1. Explicit `discriminator.mapping` on the parent schema (maps discriminator value → `$ref` name).
     * 2. The enum value of the property named `discriminator.propertyName` in the sub-schema.
     */
    private fun resolveDiscriminatorValue(
        subName: String,
        parentName: String,
    ): String? {
        val parentSchema = apiModel.components?.schemas?.get(parentName) as? OpenAPIV3Schema ?: return null
        val discriminator = parentSchema.discriminator ?: return null

        discriminator.mapping
            ?.entries
            ?.firstOrNull { it.value.substringAfterLast("/") == subName }
            ?.let { return it.key }

        val subSchema = apiModel.components?.schemas?.get(subName) as? OpenAPIV3Schema ?: return null
        val discriminatorProp = subSchema.properties?.get(discriminator.propertyName) as? OpenAPIV3Schema
        return discriminatorProp?.enum?.firstOrNull()?.contentOrNull
    }

    private fun serialNameFun(enumName: String): FunSpec =
        FunSpec
            .builder("serialName")
            .returns(STRING)
            .addStatement("return $enumName.%M().descriptor.getElementName(this.ordinal)", serializerName)
            .build()

    internal fun writeFile(
        name: String,
        typeSpec: TypeSpec?,
    ) {
        val fileSpec =
            FileSpec
                .builder(apiModel.configuration.modelPackage, name)
                .apply {
                    if (typeSpec != null) {
                        if (typeSpec.modifiers.contains(KModifier.SEALED)) {
                            addAnnotation(
                                AnnotationSpec
                                    .builder(optIn)
                                    .addMember(CodeBlock.of("%T::class", experimentalSerializationApi))
                                    .build(),
                            )
                        }
                        addTypes(listOf(typeSpec))
                    } else {
                        addTypeAlias(TypeAliasSpec.builder(name, jsonObject).build())
                    }
                }.build()

        val basePath = File(apiModel.outputDirectory).resolve("src/main/kotlin")
        logger.debug { "Writing $name to $basePath" }
        fileSpec.writeTo(basePath)
    }
}
