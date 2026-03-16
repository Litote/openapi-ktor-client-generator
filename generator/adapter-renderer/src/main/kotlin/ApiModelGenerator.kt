package org.litote.openapi.ktor.client.generator.adapter.renderer

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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.openapi.ktor.client.generator.adapter.writer.KotlinPoetFileWriter
import org.litote.openapi.ktor.client.generator.domain.ModelPropertySpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.domain.enumFieldName
import org.litote.openapi.ktor.client.generator.port.ApiFileSystemWriter
import org.litote.openapi.ktor.client.generator.port.ApiModelGeneratorConfig

public class ApiModelGenerator public constructor(
    private val modelPackage: String,
    private val outputDirectory: String,
    private val fileSystemWriter: ApiFileSystemWriter = KotlinPoetFileWriter(),
    private val modelPackageOverrides: Map<String, String> = emptyMap(),
    /**
     * Fallback package for model type references not found in [modelPackageOverrides].
     * Defaults to [modelPackage]. Set to the global shared model package when this generator
     * produces models for a per-group subproject, so that references to global shared models
     * resolve to the correct package and generate the correct import statement.
     */
    private val fallbackModelPackage: String = modelPackage,
) : ApiModelGeneratorConfig {
    private companion object {
        val serializerName: MemberName = MemberName("kotlinx.serialization.builtins", "serializer")
        val jsonObject: ClassName = ClassName("kotlinx.serialization.json", "JsonObject")
        val jsonClassDiscriminator: ClassName = ClassName("kotlinx.serialization.json", "JsonClassDiscriminator")
        val experimentalSerializationApi: ClassName = ClassName("kotlinx.serialization", "ExperimentalSerializationApi")
        val optIn: ClassName = ClassName("kotlin", "OptIn")
    }

    /**
     * If set, this value will be used as the default value for enum properties.
     * This is useful when the API may return values that are not defined in the enum, and you want
     * to avoid deserialization errors by providing a default value.
     * The default enum value is added as the last constant in the generated enum class and is used
     * when deserialization encounters an unknown value.
     */
    override var defaultEnumValue: String? = null

    public fun buildModel(spec: ModelSpec): TypeSpec? =
        when (spec) {
            is ModelSpec.EnumSpec -> buildEnumClass(spec.name, spec.values)
            is ModelSpec.DataClassSpec -> buildDataClass(spec)
            is ModelSpec.ObjectSpec -> buildObjectClass(spec)
            is ModelSpec.SealedClassSpec -> buildSealedClass(spec)
            is ModelSpec.AliasSpec -> null
        }

    private fun buildEnumClass(
        name: String,
        values: List<String>,
    ): TypeSpec =
        TypeSpec
            .enumBuilder(name)
            .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
            .apply {
                values.forEach { value ->
                    addEnumConstant(
                        value.enumFieldName,
                        anonymousClassBuilder()
                            .addAnnotation(
                                AnnotationSpec
                                    .builder(SerialName::class)
                                    .addMember("%S", value)
                                    .build(),
                            ).build(),
                    )
                }
                defaultEnumValue?.apply {
                    addEnumConstant(this)
                }
                addFunction(serialNameFun(name))
            }.build()

    private fun buildObjectClass(spec: ModelSpec.ObjectSpec): TypeSpec =
        TypeSpec
            .objectBuilder(spec.name)
            .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
            .apply {
                spec.sealedParentName?.let { sealedParentName ->
                    superclass(
                        ClassName(modelPackageOverrides.getOrDefault(sealedParentName, fallbackModelPackage), sealedParentName),
                    )
                    spec.discriminatorValue?.let { serialName ->
                        addAnnotation(
                            AnnotationSpec
                                .builder(SerialName::class)
                                .addMember("%S", serialName)
                                .build(),
                        )
                    }
                }
            }.build()

    private fun buildSealedClass(spec: ModelSpec.SealedClassSpec): TypeSpec =
        TypeSpec
            .classBuilder(spec.name)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
            .apply {
                spec.discriminatorPropertyName?.let { discriminatorProp ->
                    addAnnotation(
                        AnnotationSpec
                            .builder(jsonClassDiscriminator)
                            .addMember("%S", discriminatorProp)
                            .build(),
                    )
                }
            }.build()

    private fun buildDataClass(spec: ModelSpec.DataClassSpec): TypeSpec =
        TypeSpec
            .classBuilder(spec.name)
            .addModifiers(KModifier.DATA)
            .addAnnotation(AnnotationSpec.builder(Serializable::class).build())
            .apply {
                spec.sealedParentName?.let { sealedParentName ->
                    superclass(
                        ClassName(modelPackageOverrides.getOrDefault(sealedParentName, fallbackModelPackage), sealedParentName),
                    )
                    spec.discriminatorValue?.let { serialName ->
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
                        spec.properties.forEach { property ->
                            addParameter(buildPropertyParameter(property))
                        }
                    }.build(),
            ).apply {
                spec.properties.forEach { property ->
                    addProperty(buildPropertySpec(property))
                    property.nestedModels.forEach { nestedModel ->
                        buildModel(nestedModel)?.let { addType(it) }
                    }
                }
            }.build()

    private fun buildPropertyParameter(property: ModelPropertySpec): ParameterSpec {
        val typeName = property.type.toTypeName(fallbackModelPackage, modelPackageOverrides)
        val builder = ParameterSpec.builder(property.camelCaseName, typeName)
        val defaultValueString = computePropertyDefaultValue(property)
        if (defaultValueString != null) {
            val format = computeDefaultValueFormat(property, defaultValueString)
            builder.defaultValue(format, defaultValueString)
        }
        return builder.build()
    }

    private fun computePropertyDefaultValue(property: ModelPropertySpec): String? =
        when {
            property.schemaDefaultValue != null -> {
                val raw = property.schemaDefaultValue
                if (raw == "null" || !property.isEnum) {
                    raw
                } else {
                    val typeName = property.type.toTypeName(fallbackModelPackage, modelPackageOverrides)
                    val simpleName =
                        (typeName as? ClassName)?.simpleName
                            ?: (typeName.copy(nullable = false) as? ClassName)?.simpleName
                    simpleName?.let { "$it.$raw" }
                }
            }

            property.type.nullable -> {
                "null"
            }

            property.isEnum && defaultEnumValue != null -> {
                val typeName = property.type.toTypeName(fallbackModelPackage, modelPackageOverrides)
                val simpleName = (typeName as? ClassName)?.simpleName
                simpleName?.let { "$it.$defaultEnumValue" }
            }

            else -> {
                null
            }
        }

    private fun computeDefaultValueFormat(
        property: ModelPropertySpec,
        defaultValueString: String,
    ): String =
        when {
            defaultValueString == "null" -> "%L"
            property.isEnum -> "%L"
            property.type.isString -> "%S"
            else -> "%L"
        }

    private fun buildPropertySpec(property: ModelPropertySpec): PropertySpec {
        val typeName = property.type.toTypeName(fallbackModelPackage, modelPackageOverrides)
        return PropertySpec
            .builder(property.camelCaseName, typeName)
            .initializer(property.camelCaseName)
            .apply {
                if (property.needsSerialName) {
                    addAnnotation(
                        AnnotationSpec
                            .builder(SerialName::class)
                            .addMember("%S", property.originalName)
                            .build(),
                    )
                }
            }.build()
    }

    private fun serialNameFun(enumName: String): FunSpec =
        FunSpec
            .builder("serialName")
            .returns(STRING)
            .addStatement("return $enumName.%M().descriptor.getElementName(this.ordinal)", serializerName)
            .build()

    public fun writeFile(
        name: String,
        typeSpec: TypeSpec?,
    ) {
        val fileSpec =
            FileSpec
                .builder(modelPackage, name)
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

        fileSystemWriter.write(fileSpec.toGeneratedFile(), outputDirectory)
    }
}
