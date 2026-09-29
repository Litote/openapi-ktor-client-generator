package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec.PrimitiveSpec.KindSpec
import org.litote.openapi.ktor.client.generator.domain.ResponseHeaderSpec
import org.litote.openapi.ktor.client.generator.port.StringFormatType
import org.litote.openapi.ktor.client.generator.domain.ResponseEntrySpec as DomainResponseEntry

/**
 * Builds response types for API operations.
 */
internal class ResponseBuilder(
    private val stringFormatTypes: Map<String, StringFormatType> = emptyMap(),
) {
    private companion object {
        val serializableAnnotation: AnnotationSpec = AnnotationSpec.builder(Serializable::class).build()
        val transientAnnotation: AnnotationSpec = AnnotationSpec.builder(Transient::class).build()
        val headersClass: ClassName = ClassName("io.ktor.http", "Headers")
        const val HEADERS_PROPERTY = "headers"
    }

    private data class ResponseGroup(
        val typeName: TypeName?,
        val isSuccess: Boolean,
        val statusCodes: List<Int>,
        val headers: List<ResponseHeaderSpec>,
    )

    /**
     * Builds the sealed response class and its subclasses for an operation.
     */
    fun buildResponseTypes(
        responses: List<DomainResponseEntry>,
        clientBuilder: TypeSpec.Builder,
        responseBaseName: String,
        responseSealedClass: ClassName,
        modelPackage: String,
        modelPackageOverrides: Map<String, String> = emptyMap(),
    ): List<RenderedResponseEntry> {
        val entries =
            buildResponseEntries(responses, clientBuilder, responseBaseName, responseSealedClass, modelPackage, modelPackageOverrides)
        addUnknownFailureType(clientBuilder, responseBaseName, responseSealedClass)
        return entries
    }

    fun createSealedResponseClass(responseSealedName: String): TypeSpec =
        TypeSpec
            .classBuilder(responseSealedName)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(serializableAnnotation)
            .addProperty(PropertySpec.builder(HEADERS_PROPERTY, headersClass, KModifier.ABSTRACT).build())
            .build()

    private fun buildResponseEntries(
        responses: List<DomainResponseEntry>,
        clientBuilder: TypeSpec.Builder,
        responseBaseName: String,
        responseSealedClass: ClassName,
        modelPackage: String,
        modelPackageOverrides: Map<String, String> = emptyMap(),
    ): List<RenderedResponseEntry> {
        val grouped: List<ResponseGroup> =
            responses.map { entry ->
                ResponseGroup(
                    typeName =
                        entry.bodyType?.toTypeName(
                            modelPackage,
                            modelPackageOverrides,
                            stringFormatTypes,
                            responseSealedClass.enclosingClassName(),
                        ),
                    isSuccess = entry.isSuccess,
                    statusCodes = entry.statusCodes,
                    headers = entry.headers,
                )
            }

        if (grouped.isEmpty()) {
            error("no response specified")
        }

        return grouped.mapIndexed { index, group ->
            val suffix = determineClassNameSuffix(index, group.isSuccess, group.statusCodes, grouped)
            val responseTypeName = "${responseBaseName}Response$suffix"
            clientBuilder.addType(createResponseType(responseTypeName, group, responseSealedClass))
            RenderedResponseEntry(group.statusCodes, group.typeName, responseSealedClass.peerClass(responseTypeName))
        }
    }

    private fun determineClassNameSuffix(
        index: Int,
        success: Boolean,
        statusCodes: List<Int>,
        all: List<ResponseGroup>,
    ): String =
        when {
            success -> if (all.getOrNull(index + 1)?.isSuccess == true) "Success${statusCodes.first()}" else "Success"
            all.getOrNull(index + 1) != null -> "Failure${statusCodes.first()}"
            else -> "Failure"
        }

    private fun createResponseType(
        name: String,
        group: ResponseGroup,
        superclass: ClassName,
    ): TypeSpec {
        val constructorBuilder = FunSpec.constructorBuilder()
        val typeBuilder =
            TypeSpec
                .classBuilder(name)
                .addModifiers(KModifier.DATA)
                .addAnnotation(serializableAnnotation)
                .superclass(superclass)
        group.typeName?.let { typeName ->
            constructorBuilder.addParameter("body", typeName)
            typeBuilder.addProperty(PropertySpec.builder("body", typeName).initializer("body").build())
        }
        addHeadersProperty(constructorBuilder, typeBuilder)
        group.headers.forEach { typeBuilder.addProperty(buildTypedHeaderProperty(it)) }
        return typeBuilder.primaryConstructor(constructorBuilder.build()).build()
    }

    private fun addHeadersProperty(
        constructorBuilder: FunSpec.Builder,
        typeBuilder: TypeSpec.Builder,
    ) {
        constructorBuilder.addParameter(
            ParameterSpec
                .builder(HEADERS_PROPERTY, headersClass)
                .defaultValue("%T.Empty", headersClass)
                .build(),
        )
        typeBuilder.addProperty(
            PropertySpec
                .builder(HEADERS_PROPERTY, headersClass, KModifier.OVERRIDE)
                .addAnnotation(transientAnnotation)
                .initializer(HEADERS_PROPERTY)
                .build(),
        )
    }

    private fun buildTypedHeaderProperty(header: ResponseHeaderSpec): PropertySpec {
        val conversion =
            when (header.type.kind) {
                KindSpec.STRING -> ""
                KindSpec.INT -> "?.toIntOrNull()"
                KindSpec.LONG -> "?.toLongOrNull()"
                KindSpec.DOUBLE -> "?.toDoubleOrNull()"
                KindSpec.FLOAT -> "?.toFloatOrNull()"
                KindSpec.BOOLEAN -> "?.toBooleanStrictOrNull()"
            }
        val builder =
            PropertySpec
                .builder(header.propertyName, header.type.toTypeName("").copy(nullable = true))
                .getter(
                    FunSpec
                        .getterBuilder()
                        .addStatement("return %N[%S]$conversion", HEADERS_PROPERTY, header.originalName)
                        .build(),
                )
        header.description?.let { builder.addKdoc("%L\n", it) }
        return builder.build()
    }

    private fun addUnknownFailureType(
        clientBuilder: TypeSpec.Builder,
        responseBaseName: String,
        superclass: ClassName,
    ) {
        clientBuilder.addType(
            TypeSpec
                .classBuilder("${responseBaseName}ResponseUnknownFailure")
                .addModifiers(KModifier.DATA)
                .addAnnotation(serializableAnnotation)
                .addProperty(PropertySpec.builder("statusCode", INT).initializer("statusCode").build())
                .superclass(superclass)
                .apply {
                    val constructorBuilder = FunSpec.constructorBuilder().addParameter("statusCode", INT)
                    addHeadersProperty(constructorBuilder, this)
                    primaryConstructor(constructorBuilder.build())
                }.build(),
        )
    }
}

internal data class RenderedResponseEntry(
    val statusCodes: List<Int>,
    val bodyTypeName: TypeName?,
    val className: ClassName,
) {
    val isSuccess: Boolean get() = statusCodes.any { it in 200 until 300 }
}
