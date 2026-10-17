package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec.PrimitiveSpec.KindSpec
import org.litote.openapi.ktor.client.generator.domain.ResponseHeaderSpec
import org.litote.openapi.ktor.client.generator.port.StringFormatType
import org.litote.openapi.ktor.client.generator.domain.ResponseEntrySpec as DomainResponseEntry

/**
 * Builds response types for API operations.
 *
 * The response types implement the common interfaces of [responseTypes]: the sealed class of an operation is an
 * `OperationResponse<T>`, `T` being the body type of its successful responses (`Unit` without body, `Any?` when they
 * differ, `Nothing` without successful response).
 */
internal class ResponseBuilder(
    private val responseTypes: OperationResponseTypes,
    private val stringFormatTypes: Map<String, StringFormatType> = emptyMap(),
) {
    private companion object {
        val serializableAnnotation: AnnotationSpec = AnnotationSpec.builder(Serializable::class).build()
        val transientAnnotation: AnnotationSpec = AnnotationSpec.builder(Transient::class).build()
        val headersClass: ClassName = ClassName("io.ktor.http", "Headers")
        const val HEADERS_PROPERTY = "headers"
        const val BODY_PROPERTY = "body"
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
        val grouped = groupResponses(responses, responseSealedClass, modelPackage, modelPackageOverrides)
        val successType = successType(grouped)
        clientBuilder.addType(createSealedResponseClass(responseSealedClass.simpleName, successType))
        val entries = buildResponseEntries(grouped, clientBuilder, responseBaseName, responseSealedClass, successType)
        addUnknownFailureType(clientBuilder, responseBaseName, responseSealedClass)
        return entries
    }

    private fun successType(grouped: List<ResponseGroup>): TypeName {
        val bodyTypes = grouped.filter { it.isSuccess }.map { it.typeName ?: UNIT }.distinct()
        return when (bodyTypes.size) {
            0 -> NOTHING
            1 -> bodyTypes.single()
            else -> ANY.copy(nullable = true)
        }
    }

    private fun createSealedResponseClass(
        responseSealedName: String,
        successType: TypeName,
    ): TypeSpec =
        TypeSpec
            .classBuilder(responseSealedName)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(serializableAnnotation)
            .addSuperinterface(responseTypes.response.parameterizedBy(successType))
            .addProperty(PropertySpec.builder(HEADERS_PROPERTY, headersClass, KModifier.ABSTRACT, KModifier.OVERRIDE).build())
            .build()

    private fun groupResponses(
        responses: List<DomainResponseEntry>,
        responseSealedClass: ClassName,
        modelPackage: String,
        modelPackageOverrides: Map<String, String>,
    ): List<ResponseGroup> {
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
        return grouped
    }

    private fun buildResponseEntries(
        grouped: List<ResponseGroup>,
        clientBuilder: TypeSpec.Builder,
        responseBaseName: String,
        responseSealedClass: ClassName,
        successType: TypeName,
    ): List<RenderedResponseEntry> =
        grouped.mapIndexed { index, group ->
            val suffix = determineClassNameSuffix(index, group.isSuccess, group.statusCodes, grouped)
            val responseTypeName = "${responseBaseName}Response$suffix"
            clientBuilder.addType(createResponseType(responseTypeName, group, responseSealedClass, successType))
            RenderedResponseEntry(group.statusCodes, group.typeName, responseSealedClass.peerClass(responseTypeName))
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
        successType: TypeName,
    ): TypeSpec {
        val constructorBuilder = FunSpec.constructorBuilder()
        val typeBuilder =
            TypeSpec
                .classBuilder(name)
                .addModifiers(KModifier.DATA)
                .addAnnotation(serializableAnnotation)
                .superclass(superclass)
                .addSuperinterface(
                    if (group.isSuccess) {
                        responseTypes.success.parameterizedBy(successType)
                    } else {
                        responseTypes.failure.parameterizedBy(group.typeName ?: UNIT)
                    },
                )
        val typeName = group.typeName
        if (typeName != null) {
            constructorBuilder.addParameter(BODY_PROPERTY, typeName)
            typeBuilder.addProperty(PropertySpec.builder(BODY_PROPERTY, typeName, KModifier.OVERRIDE).initializer(BODY_PROPERTY).build())
        } else {
            typeBuilder.addProperty(
                PropertySpec
                    .builder(BODY_PROPERTY, UNIT, KModifier.OVERRIDE)
                    .getter(FunSpec.getterBuilder().addStatement("return %T", UNIT).build())
                    .build(),
            )
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
                .addProperty(PropertySpec.builder("statusCode", INT, KModifier.OVERRIDE).initializer("statusCode").build())
                .superclass(superclass)
                .addSuperinterface(responseTypes.unknownFailure)
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
