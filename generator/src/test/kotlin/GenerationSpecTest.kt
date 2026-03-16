package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.ClientConfigurationSpec
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.domain.ParameterLocationSpec
import org.litote.openapi.ktor.client.generator.domain.SecuritySchemeLocationSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GenerationSpecTest {
    // region helpers

    private fun parse(fileName: String): GenerationSpec {
        val config =
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/$fileName",
                outputDirectory = "build/openapi-spec-test",
            )
        return OpenApiSpecificationParser(config).parse(config.operationFilter)
    }

    // endregion

    @Test
    fun `GIVEN minimal openapi WHEN parsing THEN GenerationSpec has expected client and models`() {
        val spec = parse("openapi.json")

        assertEquals(1, spec.clients.size, "Should have one client (untagged)")
        assertTrue(spec.models.isNotEmpty(), "Should have models")
    }

    @Test
    fun `GIVEN minimal openapi WHEN parsing THEN client has operations`() {
        val spec = parse("openapi.json")
        val client = spec.clients.first()

        assertTrue(client.operations.isNotEmpty(), "Client should have at least one operation")
    }

    @Test
    fun `GIVEN minimal openapi WHEN parsing THEN models include TestRequest and TestResponse`() {
        val spec = parse("openapi.json")
        val modelNames = spec.models.map { it.name }.toSet()

        assertTrue("TestRequest" in modelNames, "Should include TestRequest")
        assertTrue("TestResponse" in modelNames, "Should include TestResponse")
    }

    @Test
    fun `GIVEN minimal openapi WHEN parsing THEN TestResponse has id as Long and name as String`() {
        val spec = parse("openapi.json")
        val testResponse = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "TestResponse" }

        val idProp = testResponse.properties.first { it.originalName == "id" }
        val nameProp = testResponse.properties.first { it.originalName == "name" }

        assertEquals(DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.LONG), idProp.type)
        assertEquals(DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING), nameProp.type)
    }

    @Test
    fun `GIVEN minimal openapi WHEN parsing THEN clientConfiguration has no apiKeySchemes`() {
        val spec = parse("openapi.json")
        assertTrue(spec.clientConfiguration.apiKeySchemes.isEmpty())
    }

    @Test
    fun `GIVEN sample openapi with security WHEN parsing THEN clientConfiguration has two apiKeySchemes`() {
        val spec = parse("sample.json")

        assertEquals(2, spec.clientConfiguration.apiKeySchemes.size)

        val headerScheme = spec.clientConfiguration.apiKeySchemes.first { it.location == SecuritySchemeLocationSpec.HEADER }
        assertEquals("api_key_header", headerScheme.name)
        assertEquals("X-Api-Key", headerScheme.keyName)

        val queryScheme = spec.clientConfiguration.apiKeySchemes.first { it.location == SecuritySchemeLocationSpec.QUERY }
        assertEquals("api_key_query_param", queryScheme.name)
        assertEquals("api_key", queryScheme.keyName)
    }

    @Test
    fun `GIVEN sample openapi with security WHEN parsing THEN clientConfiguration serverUrl is set`() {
        val spec = parse("sample.json")
        assertTrue(spec.clientConfiguration.serverUrl.isNotBlank())
    }

    @Test
    fun `GIVEN inheritance openapi WHEN parsing THEN Status is a SealedClassSpec`() {
        val spec = parse("inheritance.json")
        val statusModel = spec.models.first { it.name == "Status" }
        assertIs<ModelSpec.SealedClassSpec>(statusModel)
    }

    @Test
    fun `GIVEN inheritance openapi WHEN parsing THEN subtypes reference sealed parent`() {
        val spec = parse("inheritance.json")
        val textStatus = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "TextStatus" }
        assertEquals("Status", textStatus.sealedParentName)
    }

    @Test
    fun `GIVEN minimal openapi WHEN parsing THEN operation parameters have correct location`() {
        val spec = parse("openapi.json")
        val client = spec.clients.first()
        val operation = client.operations.first()

        // testId path param
        val pathParam = operation.parameters.firstOrNull { it.location == ParameterLocationSpec.PATH }
        assertNotNull(pathParam, "Should have a path parameter")
        assertEquals("testId", pathParam.originalName)
    }

    @Test
    fun `GIVEN minimal openapi WHEN parsing THEN operation has request body`() {
        val spec = parse("openapi.json")
        val client = spec.clients.first()
        val operation = client.operations.first { it.requestBody != null }

        assertNotNull(operation.requestBody)
        val requestBody = operation.requestBody
        assertNotNull(requestBody)
        assertEquals("request", requestBody.parameterName)
    }

    @Test
    fun `GIVEN minimal openapi WHEN parsing THEN operation has response entries`() {
        val spec = parse("openapi.json")
        val client = spec.clients.first()
        val operation = client.operations.first()

        assertTrue(operation.responses.isNotEmpty(), "Should have at least one response entry")
        assertTrue(operation.responses.any { it.isSuccess }, "Should have at least one success response")
    }

    @Test
    fun `GIVEN DomainTypeSpec WHEN asNullable called THEN returns nullable variant`() {
        val stringType = DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING)
        val nullableString = stringType.asNullable()

        assertTrue(nullableString.nullable)
        assertEquals(DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING, nullable = true), nullableString)
    }

    @Test
    fun `GIVEN DomainTypeSpec ModelReference WHEN asNullable called THEN returns nullable variant`() {
        val ref = DomainTypeSpec.ModelReferenceSpec("TestResponse")
        val nullableRef = ref.asNullable()

        assertTrue(nullableRef.nullable)
        assertEquals(DomainTypeSpec.ModelReferenceSpec("TestResponse", nullable = true), nullableRef)
    }

    @Test
    fun `GIVEN ClientConfigurationSpec WHEN created THEN holds correct data`() {
        val spec =
            ClientConfigurationSpec(
                serverUrl = "https://api.example.com/",
                apiKeySchemes = emptyList(),
                componentParameters = emptyList(),
            )

        assertEquals("https://api.example.com/", spec.serverUrl)
        assertTrue(spec.apiKeySchemes.isEmpty())
    }
}
