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

package org.litote.openapi.ktor.client.generator.client

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Operation
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Response
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import org.litote.openapi.ktor.client.generator.ApiModel

/**
 * Builds response types for API operations.
 */
internal class ResponseBuilder(
    private val apiModel: ApiModel,
) {
    private companion object {
        val serializableAnnotation: AnnotationSpec = AnnotationSpec.builder(Serializable::class).build()
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Builds the sealed response class and its subclasses for an operation.
     */
    fun buildResponseTypes(
        operation: OpenAPIV3Operation,
        clientBuilder: TypeSpec.Builder,
        responseBaseName: String,
        responseSealedClass: ClassName,
    ): List<ResponseEntry> {
        val entries = buildResponseEntries(operation, clientBuilder, responseBaseName, responseSealedClass)
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
        operation: OpenAPIV3Operation,
        clientBuilder: TypeSpec.Builder,
        responseBaseName: String,
        responseSealedClass: ClassName,
    ): List<ResponseEntry> {
        val responses = operation.responses ?: error("no response specified")

        val parsedResponses: List<Pair<Int, TypeName?>> =
            responses.entries
                .map { entry ->
                    parseResponse(entry.key.value, entry.value, responseBaseName)
                }.sortedBy { it.first }

        val grouped: List<Triple<TypeName?, Boolean, List<Int>>> =
            parsedResponses
                .groupBy { pair -> pair.second to pair.first.isSuccess }
                .map { entry ->
                    val typeAndSuccess = entry.key
                    val statusCodes = entry.value.map { it.first }
                    Triple(typeAndSuccess.first, typeAndSuccess.second, statusCodes)
                }

        if (grouped.isEmpty()) error("no response specified")

        return grouped.mapIndexed { index, triple ->
            val typeName = triple.first
            val success = triple.second
            val statusCodes = triple.third
            val suffix = determineClassNameSuffix(index, success, statusCodes, grouped)
            val responseType = createResponseType("${responseBaseName}Response$suffix", typeName, responseSealedClass)
            clientBuilder.addType(responseType)
            ResponseEntry(statusCodes, typeName, responseType)
        }
    }

    private fun parseResponse(
        statusCodeValue: String,
        responseOrReference: Any,
        responseBaseName: String,
    ): Pair<Int, TypeName?> {
        val code = statusCodeValue.toIntOrNull() ?: error("Invalid status code: $statusCodeValue")
        val response = responseOrReference as? OpenAPIV3Response ?: error("Unsupported response reference: $responseOrReference")
        val schema =
            response.content
                ?.values
                ?.firstOrNull()
                ?.schema
        if ((response.content?.size ?: 0) > 1) logger.warn { "More than one response content - taking first" }
        return code to schema?.let { apiModel.getClassName("${responseBaseName}ResponseBody", it) }
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

internal data class ResponseEntry(
    val statusCodes: List<Int>,
    val bodyType: TypeName?,
    val type: TypeSpec,
) {
    val isSuccess: Boolean get() = statusCodes.any { it in 200 until 300 }
}

private val Int.isSuccess: Boolean get() = this in 200 until 300
