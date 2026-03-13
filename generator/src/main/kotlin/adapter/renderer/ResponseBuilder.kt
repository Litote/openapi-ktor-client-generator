/*
 * Copyright 2026 litote.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.Serializable
import org.litote.openapi.ktor.client.generator.domain.ResponseEntry as DomainResponseEntry

/**
 * Builds response types for API operations.
 */
internal class ResponseBuilder {
    private companion object {
        val serializableAnnotation: AnnotationSpec = AnnotationSpec.builder(Serializable::class).build()
    }

    /**
     * Builds the sealed response class and its subclasses for an operation.
     */
    fun buildResponseTypes(
        responses: List<DomainResponseEntry>,
        clientBuilder: TypeSpec.Builder,
        responseBaseName: String,
        responseSealedClass: ClassName,
        modelPackage: String,
    ): List<RenderedResponseEntry> {
        val entries = buildResponseEntries(responses, clientBuilder, responseBaseName, responseSealedClass, modelPackage)
        addUnknownFailureType(clientBuilder, responseBaseName, responseSealedClass)
        return entries
    }

    fun createSealedResponseClass(responseSealedName: String): TypeSpec =
        TypeSpec
            .classBuilder(responseSealedName)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(serializableAnnotation)
            .build()

    private fun buildResponseEntries(
        responses: List<DomainResponseEntry>,
        clientBuilder: TypeSpec.Builder,
        responseBaseName: String,
        responseSealedClass: ClassName,
        modelPackage: String,
    ): List<RenderedResponseEntry> {
        val grouped: List<Triple<TypeName?, Boolean, List<Int>>> =
            responses.map { entry ->
                val typeName = entry.bodyType?.toTypeName(modelPackage)
                Triple(typeName, entry.isSuccess, entry.statusCodes)
            }

        if (grouped.isEmpty()) {
            error("no response specified")
        }

        return grouped.mapIndexed { index, triple ->
            val typeName = triple.first
            val success = triple.second
            val statusCodes = triple.third
            val suffix = determineClassNameSuffix(index, success, statusCodes, grouped)
            val responseType = createResponseType("${responseBaseName}Response$suffix", typeName, responseSealedClass)
            clientBuilder.addType(responseType)
            RenderedResponseEntry(statusCodes, typeName, responseType)
        }
    }

    private fun determineClassNameSuffix(
        index: Int,
        success: Boolean,
        statusCodes: List<Int>,
        all: List<Triple<TypeName?, Boolean, List<Int>>>,
    ): String =
        when {
            success -> if (all.getOrNull(index + 1)?.second == true) "Success${statusCodes.first()}" else "Success"
            all.getOrNull(index + 1) != null -> "Failure${statusCodes.first()}"
            else -> "Failure"
        }

    private fun createResponseType(
        name: String,
        typeName: TypeName?,
        superclass: ClassName,
    ): TypeSpec =
        if (typeName == null) {
            TypeSpec
                .objectBuilder(name)
                .addAnnotation(serializableAnnotation)
                .superclass(superclass)
                .build()
        } else {
            TypeSpec
                .classBuilder(name)
                .addModifiers(KModifier.DATA)
                .addAnnotation(serializableAnnotation)
                .primaryConstructor(FunSpec.constructorBuilder().addParameter("body", typeName).build())
                .addProperty(PropertySpec.builder("body", typeName).initializer("body").build())
                .superclass(superclass)
                .build()
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
                .primaryConstructor(FunSpec.constructorBuilder().addParameter("statusCode", INT).build())
                .addProperty(PropertySpec.builder("statusCode", INT).initializer("statusCode").build())
                .superclass(superclass)
                .build(),
        )
    }
}

internal data class RenderedResponseEntry(
    val statusCodes: List<Int>,
    val bodyTypeName: TypeName?,
    val type: TypeSpec,
) {
    val isSuccess: Boolean get() = statusCodes.any { it in 200 until 300 }
}
