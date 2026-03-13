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

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.DomainType
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApiModelTest {
    private fun loadSpec(fileName: String): GenerationSpec {
        val configuration =
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/$fileName",
                outputDirectory = "build/openapi-test",
            )
        return OpenApiSpecificationParser(configuration).parse(configuration.operationFilter)
    }

    @Test
    fun `GIVEN openapi without servers WHEN building model THEN serverUrl defaults`() {
        val spec = loadSpec("openapi.json")

        assertEquals("http://localhost:8080/", spec.clientConfiguration.serverUrl)
    }

    @Test
    fun `GIVEN openapi without tags WHEN building pathsByTags THEN uses empty tag key`() {
        val spec = loadSpec("openapi.json")

        assertEquals(1, spec.clients.size)
        val client = spec.clients.first()
        assertNotNull(client)
    }

    @Test
    fun `GIVEN referenced schemas WHEN building schemas THEN includes request and response`() {
        val spec = loadSpec("openapi.json")
        val modelNames = spec.models.map { it.name }.toSet()

        assertEquals(setOf("TestRequest", "TestResponse"), modelNames)
    }

    @Test
    fun `GIVEN response properties WHEN getClassName THEN maps to expected kotlin types`() {
        val spec = loadSpec("openapi.json")
        val testResponse = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "TestResponse" }
        val idProp = testResponse.properties.first { it.originalName == "id" }
        val nameProp = testResponse.properties.first { it.originalName == "name" }

        assertEquals(DomainType.Primitive(DomainType.Primitive.Kind.LONG), idProp.type)
        assertEquals(DomainType.Primitive(DomainType.Primitive.Kind.STRING), nameProp.type)
    }

    @Test
    fun `GIVEN request body reference WHEN getClassName THEN maps to model class`() {
        val spec = loadSpec("openapi.json")
        val client = spec.clients.first()
        val operation = client.operations.first { it.requestBody != null }
        val requestBody = operation.requestBody
        assertNotNull(requestBody)

        assertEquals(DomainType.ModelReference("TestRequest"), requestBody.type)
    }
}
