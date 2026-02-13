/*
 *    Copyright 2026 litote.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.STRING
import community.flock.kotlinx.openapi.bindings.OpenAPIV3RequestBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApiModelTest {
    @Test
    fun `GIVEN openapi without servers WHEN building model THEN serverUrl defaults`() {
        // Given
        val apiModel = loadModel("openapi.json")

        // When
        val serverUrl = apiModel.serverUrl

        // Then
        assertEquals("http://localhost:8080/", serverUrl)
    }

    @Test
    fun `GIVEN openapi without tags WHEN building pathsByTags THEN uses empty tag key`() {
        // Given
        val apiModel = loadModel("openapi.json")

        // When
        val operations = apiModel.pathsByTags

        // Then
        assertEquals(setOf(""), operations.keys)
        val firstOperation = operations[""]?.firstOrNull()
        assertNotNull(firstOperation)
        assertEquals("/test/{testId}", firstOperation.path)
        assertEquals("post", firstOperation.method)
    }

    @Test
    fun `GIVEN referenced schemas WHEN building schemas THEN includes request and response`() {
        // Given
        val apiModel = loadModel("openapi.json")

        // When
        val schemaNames = apiModel.schemas.keys

        // Then
        assertEquals(setOf("TestRequest", "TestResponse"), schemaNames)
    }

    @Test
    fun `GIVEN response properties WHEN getClassName THEN maps to expected kotlin types`() {
        // Given
        val apiModel = loadModel("openapi.json")
        val responseSchema = apiModel.schemas["TestResponse"]
        val idSchema = responseSchema?.properties?.get("id")
        val nameSchema = responseSchema?.properties?.get("name")
        assertNotNull(idSchema)
        assertNotNull(nameSchema)

        // When
        val idType = apiModel.getClassName("id", idSchema)
        val nameType = apiModel.getClassName("name", nameSchema)

        // Then
        assertEquals(LONG, idType)
        assertEquals(STRING, nameType)
    }

    @Test
    fun `GIVEN request body reference WHEN getClassName THEN maps to model class`() {
        // Given
        val apiModel = loadModel("openapi.json")
        val operation =
            apiModel.model.paths.entries
                .first()
                .value.post
        assertNotNull(operation)
        val requestBody = operation.requestBody as OpenAPIV3RequestBody
        val schema =
            requestBody.content
                ?.values
                ?.firstOrNull()
                ?.schema
        assertNotNull(schema)

        // When
        val className = apiModel.getClassName("TestRequest", schema)

        // Then
        assertEquals(ClassName("org.example.model", "TestRequest"), className)
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
