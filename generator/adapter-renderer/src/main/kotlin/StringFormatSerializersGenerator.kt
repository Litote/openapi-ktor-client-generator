package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import org.litote.openapi.ktor.client.generator.port.ApiFileSystemWriter
import org.litote.openapi.ktor.client.generator.port.StringFormatType

/**
 * Generates one `KSerializer` per string format whose [StringFormatType.formatSuffix] is not `toString()`,
 * so that JSON bodies use the same format as the parameters. Values are decoded with [StringFormatType.parseFunction].
 */
internal class StringFormatSerializersGenerator(
    private val clientPackage: String,
    private val outputDirectory: String,
    private val stringFormatTypes: Map<String, StringFormatType>,
    private val fileSystemWriter: ApiFileSystemWriter,
) {
    private companion object {
        const val FILE_NAME = "StringFormatSerializers"
        const val SERIALIZATION_PACKAGE = "kotlinx.serialization"
        const val DESCRIPTORS_PACKAGE = "kotlinx.serialization.descriptors"
        const val ENCODING_PACKAGE = "kotlinx.serialization.encoding"

        val kSerializer: ClassName = ClassName(SERIALIZATION_PACKAGE, "KSerializer")
        val serialDescriptor: ClassName = ClassName(DESCRIPTORS_PACKAGE, "SerialDescriptor")
        val primitiveSerialDescriptor: ClassName = ClassName(DESCRIPTORS_PACKAGE, "PrimitiveSerialDescriptor")
        val primitiveKind: ClassName = ClassName(DESCRIPTORS_PACKAGE, "PrimitiveKind")
        val encoder: ClassName = ClassName(ENCODING_PACKAGE, "Encoder")
        val decoder: ClassName = ClassName(ENCODING_PACKAGE, "Decoder")
    }

    fun render() {
        val serializers =
            stringFormatTypes
                .filterValues { it.needsSerializer }
                .toSortedMap()
                .map { (format, formatType) -> buildSerializer(format, formatType) }
        if (serializers.isEmpty()) return
        val fileSpec =
            FileSpec
                .builder(clientPackage, FILE_NAME)
                .addTypes(serializers)
                .build()
        fileSystemWriter.write(fileSpec.toGeneratedFile(), outputDirectory)
    }

    private fun buildSerializer(
        format: String,
        formatType: StringFormatType,
    ): TypeSpec {
        val serializerClass = stringFormatSerializerClass(clientPackage, format)
        val valueType = ClassName.bestGuess(formatType.qualifiedName)
        return TypeSpec
            .objectBuilder(serializerClass)
            .addKdoc("Serializes OpenAPI `%L` strings as `%L`.\n", format, formatType.qualifiedName)
            .addSuperinterface(kSerializer.parameterizedBy(valueType))
            .addProperty(
                PropertySpec
                    .builder("descriptor", serialDescriptor, KModifier.OVERRIDE)
                    .initializer("%T(%S, %T.STRING)", primitiveSerialDescriptor, serializerClass.canonicalName, primitiveKind)
                    .build(),
            ).addFunction(
                FunSpec
                    .builder("serialize")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("encoder", encoder)
                    .addParameter("value", valueType)
                    .addStatement("encoder.encodeString(value${formatType.formatSuffix})")
                    .build(),
            ).addFunction(
                FunSpec
                    .builder("deserialize")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("decoder", decoder)
                    .returns(valueType)
                    .addStatement("return %T.%N(decoder.decodeString())", valueType, formatType.parseFunction)
                    .build(),
            ).build()
    }
}
