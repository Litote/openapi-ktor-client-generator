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
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.openapi.ktor.client.generator.adapter.writer.KotlinPoetFileWriter
import org.litote.openapi.ktor.client.generator.domain.DomainType
import org.litote.openapi.ktor.client.generator.domain.ModelProperty
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.port.FileSystemWriter
import org.litote.openapi.ktor.client.generator.port.ModelGeneratorConfig
import org.litote.openapi.ktor.client.generator.shared.capitalize

public class ApiModelGenerator internal constructor(
    private val modelPackage: String,
    private val outputDirectory: String,
    private val fileSystemWriter: FileSystemWriter = KotlinPoetFileWriter(),
) : ModelGeneratorConfig {
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
     * This is useful when the API may return values that are not defined in the enum, and you want
     * to avoid deserialization errors by providing a default value.
     * The default enum value is added as the last constant in the generated enum class and is used
     * when deserialization encounters an unknown value.
     */
    override var defaultEnumValue: String? = null

    internal fun buildModel(spec: ModelSpec): TypeSpec? =
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
                if (spec.sealedParentName != null) {
                    superclass(ClassName(modelPackage, spec.sealedParentName))
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
                if (spec.sealedParentName != null) {
                    superclass(ClassName(modelPackage, spec.sealedParentName))
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

    private fun buildPropertyParameter(property: ModelProperty): ParameterSpec {
        val typeName = property.type.toTypeName(modelPackage)
        val builder = ParameterSpec.builder(property.camelCaseName, typeName)
        val defaultValueString = computePropertyDefaultValue(property)
        if (defaultValueString != null) {
            val format = computeDefaultValueFormat(property, defaultValueString)
            builder.defaultValue(format, defaultValueString)
        }
        return builder.build()
    }

    private fun computePropertyDefaultValue(property: ModelProperty): String? =
        when {
            property.schemaDefaultValue != null -> {
                val raw = property.schemaDefaultValue
                if (raw == "null" || !property.isEnum) {
                    raw
                } else {
                    val typeName = property.type.toTypeName(modelPackage)
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
                val typeName = property.type.toTypeName(modelPackage)
                val simpleName = (typeName as? ClassName)?.simpleName
                simpleName?.let { "$it.$defaultEnumValue" }
            }

            else -> {
                null
            }
        }

    private fun computeDefaultValueFormat(
        property: ModelProperty,
        defaultValueString: String,
    ): String =
        when {
            defaultValueString == "null" -> "%L"
            property.isEnum -> "%L"
            property.type.isString -> "%S"
            else -> "%L"
        }

    private fun buildPropertySpec(property: ModelProperty): PropertySpec {
        val typeName = property.type.toTypeName(modelPackage)
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

    internal fun writeFile(
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
