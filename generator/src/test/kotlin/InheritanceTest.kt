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
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import kotlinx.serialization.SerialName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class InheritanceTest {
    @Test
    fun `GIVEN openapi with oneOf WHEN building model THEN generates sealed class`() {
        val apiModel = loadModel("inheritance.json")
        val statusSchema = apiModel.schemas["Status"] as OpenAPIV3Schema
        val modelGenerator = ModelGenerator(apiModel)

        val typeSpec = modelGenerator.buildModel("Status", statusSchema)

        assertNotNull(typeSpec)
        assertTrue(typeSpec.modifiers.contains(KModifier.SEALED), "Should be a sealed class")
        assertEquals("Status", typeSpec.name)
    }

    @Test
    fun `GIVEN openapi with oneOf WHEN building model THEN sealed class has JsonClassDiscriminator annotation`() {
        val apiModel = loadModel("inheritance.json")
        val statusSchema = apiModel.schemas["Status"] as OpenAPIV3Schema
        val modelGenerator = ModelGenerator(apiModel)

        val typeSpec = modelGenerator.buildModel("Status", statusSchema)

        assertNotNull(typeSpec)
        val discriminatorAnnotation =
            typeSpec.annotations.firstOrNull { annotation ->
                (annotation.typeName as? ClassName)?.simpleName == "JsonClassDiscriminator"
            }
        assertNotNull(discriminatorAnnotation, "Should have @JsonClassDiscriminator annotation")
    }

    @Test
    fun `GIVEN openapi with oneOf WHEN building sub-type THEN generates data class extending sealed class`() {
        val apiModel = loadModel("inheritance.json")
        val textStatusSchema = apiModel.schemas["TextStatus"] as OpenAPIV3Schema
        val modelGenerator = ModelGenerator(apiModel)

        val typeSpec = modelGenerator.buildModel("TextStatus", textStatusSchema)

        assertNotNull(typeSpec)
        assertTrue(typeSpec.modifiers.contains(KModifier.DATA), "Should be a data class")
        assertEquals(
            ClassName(apiModel.configuration.modelPackage, "Status"),
            typeSpec.superclass,
            "Should extend Status",
        )
    }

    @Test
    fun `GIVEN openapi with oneOf WHEN building sub-type THEN generates SerialName annotation from discriminator`() {
        val apiModel = loadModel("inheritance.json")
        val mediaStatusSchema = apiModel.schemas["MediaStatus"] as OpenAPIV3Schema
        val modelGenerator = ModelGenerator(apiModel)

        val typeSpec = modelGenerator.buildModel("MediaStatus", mediaStatusSchema)

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
        val apiModel = loadModel("inheritance.json")

        assertEquals(
            listOf("TextStatus", "MediaStatus", "PollStatus"),
            apiModel.sealedParents["Status"],
        )
        assertEquals("Status", apiModel.sealedSubTypes["TextStatus"])
        assertEquals("Status", apiModel.sealedSubTypes["MediaStatus"])
        assertEquals("Status", apiModel.sealedSubTypes["PollStatus"])
    }

    private fun loadModel(fileName: String): ApiModel {
        val configuration =
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/$fileName",
                outputDirectory = "build/openapi-test",
            )
        return ApiModel.parseOpenApiFile(configuration)
    }
}
