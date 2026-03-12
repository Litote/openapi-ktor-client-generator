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

package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import kotlinx.serialization.SerialName
import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.adapter.renderer.ApiModelGenerator
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InheritanceTest {
    @Test
    fun `GIVEN openapi with oneOf WHEN building model THEN generates sealed class`() {
        val (config, generationSpec) = loadSpec("inheritance.json")
        val modelSpec = generationSpec.models.first { it.name == "Status" }
        val generator = ApiModelGenerator(config.modelPackage, "build/openapi-test")

        val typeSpec = generator.buildModel(modelSpec)

        assertNotNull(typeSpec)
        assertTrue(typeSpec.modifiers.contains(KModifier.SEALED), "Should be a sealed class")
        assertEquals("Status", typeSpec.name)
    }

    @Test
    fun `GIVEN openapi with oneOf WHEN building model THEN sealed class has JsonClassDiscriminator annotation`() {
        val (config, generationSpec) = loadSpec("inheritance.json")
        val modelSpec = generationSpec.models.first { it.name == "Status" }
        val generator = ApiModelGenerator(config.modelPackage, "build/openapi-test")

        val typeSpec = generator.buildModel(modelSpec)

        assertNotNull(typeSpec)
        val discriminatorAnnotation =
            typeSpec.annotations.firstOrNull { annotation ->
                (annotation.typeName as? ClassName)?.simpleName == "JsonClassDiscriminator"
            }
        assertNotNull(discriminatorAnnotation, "Should have @JsonClassDiscriminator annotation")
    }

    @Test
    fun `GIVEN openapi with oneOf WHEN building sub-type THEN generates data class extending sealed class`() {
        val (config, generationSpec) = loadSpec("inheritance.json")
        val modelSpec = generationSpec.models.first { it.name == "TextStatus" }
        val generator = ApiModelGenerator(config.modelPackage, "build/openapi-test")

        val typeSpec = generator.buildModel(modelSpec)

        assertNotNull(typeSpec)
        assertTrue(typeSpec.modifiers.contains(KModifier.DATA), "Should be a data class")
        assertEquals(
            ClassName(config.modelPackage, "Status"),
            typeSpec.superclass,
            "Should extend Status",
        )
    }

    @Test
    fun `GIVEN openapi with oneOf WHEN building sub-type THEN generates SerialName annotation from discriminator`() {
        val (config, generationSpec) = loadSpec("inheritance.json")
        val modelSpec = generationSpec.models.first { it.name == "MediaStatus" }
        val generator = ApiModelGenerator(config.modelPackage, "build/openapi-test")

        val typeSpec = generator.buildModel(modelSpec)

        assertNotNull(typeSpec)
        val serialNameAnnotation =
            typeSpec.annotations.firstOrNull { annotation ->
                (annotation.typeName as? ClassName)?.simpleName == SerialName::class.simpleName
            }
        assertNotNull(serialNameAnnotation, "Should have @SerialName annotation")
        val serialNameValue = serialNameAnnotation.members.firstOrNull()?.toString()
        assertEquals("\"media\"", serialNameValue, "SerialName should be the discriminator enum value")
    }

    @Test
    fun `GIVEN openapi with oneOf of 3 subtypes WHEN building model THEN all subtypes are recognized`() {
        val (_, generationSpec) = loadSpec("inheritance.json")

        val statusModel = generationSpec.models.first { it.name == "Status" }
        assertIs<org.litote.openapi.ktor.client.generator.domain.ModelSpec.SealedClassSpec>(statusModel)

        val subtypeNames =
            generationSpec.models
                .filterIsInstance<org.litote.openapi.ktor.client.generator.domain.ModelSpec.DataClassSpec>()
                .filter { it.sealedParentName == "Status" }
                .map { it.name }
        assertTrue(subtypeNames.containsAll(listOf("TextStatus", "MediaStatus", "PollStatus")))
    }

    private fun loadSpec(
        fileName: String,
    ): Pair<ApiGeneratorConfiguration, org.litote.openapi.ktor.client.generator.domain.GenerationSpec> {
        val configuration =
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/$fileName",
                outputDirectory = "build/openapi-test",
            )
        val parser = OpenApiSpecificationParser()
        return configuration to parser.parse(configuration, configuration.operationFilter)
    }
}
