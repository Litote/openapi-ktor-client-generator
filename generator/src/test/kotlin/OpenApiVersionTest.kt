package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.domain.ParameterLocationSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OpenApiVersionTest {
    private fun parseSpec(fileName: String) =
        OpenApiSpecificationParser(
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/$fileName",
                outputDirectory = "build/openapi-version-test",
            ),
        ).parse { true }

    @Test
    fun `GIVEN openapi 3_1 spec WHEN generating THEN generation succeeds`() {
        val result =
            generate(
                ApiGeneratorConfiguration(
                    openApiFile = "src/test/resources/openapi-31.json",
                    outputDirectory = "build/openapi-31",
                ),
            )
        assertTrue(result.isSuccess, "Generation should succeed for OpenAPI 3.1")
        val success = result.getOrThrow()
        assertTrue(success.clientsGenerated > 0)
        assertTrue(success.modelsGenerated > 0)
    }

    @Test
    fun `GIVEN openapi 3_2 spec WHEN generating THEN generation succeeds`() {
        val result =
            generate(
                ApiGeneratorConfiguration(
                    openApiFile = "src/test/resources/openapi-32.json",
                    outputDirectory = "build/openapi-32",
                ),
            )
        assertTrue(result.isSuccess, "Generation should succeed for OpenAPI 3.2")
        val success = result.getOrThrow()
        assertTrue(success.clientsGenerated > 0)
        assertTrue(success.modelsGenerated > 0)
    }

    @Test
    fun `GIVEN openapi 3_1 spec with nullable type array on required field WHEN parsing THEN field is nullable`() {
        val spec = parseSpec("openapi-31.json")
        val item = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "Item" }
        val nullableRequired = item.properties.first { it.originalName == "nullableRequired" }
        assertTrue(
            nullableRequired.type.nullable,
            "nullableRequired should be nullable despite being in required (type: [\"string\", \"null\"])",
        )
    }

    @Test
    fun `GIVEN openapi 3_2 spec with nullable type array on required field WHEN parsing THEN field is nullable`() {
        val spec = parseSpec("openapi-32.json")
        val item = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "Item" }
        val nullableRequired = item.properties.first { it.originalName == "nullableRequired" }
        assertTrue(
            nullableRequired.type.nullable,
            "nullableRequired should be nullable despite being in required (type: [\"string\", \"null\"])",
        )
    }

    @Test
    fun `GIVEN openapi 3_1 spec WHEN parsing THEN non-nullable required field stays non-nullable`() {
        val spec = parseSpec("openapi-31.json")
        val item = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "Item" }
        val name = item.properties.first { it.originalName == "name" }
        val domainType = name.type
        assertTrue(domainType is DomainTypeSpec.PrimitiveSpec, "name should be a string primitive")
        assertTrue(!domainType.nullable, "name should not be nullable (required, type: string)")
    }

    @Test
    fun `GIVEN openapi 3_1 spec WHEN parsing THEN optional field stays nullable`() {
        val spec = parseSpec("openapi-31.json")
        val item = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "Item" }
        val description = item.properties.first { it.originalName == "description" }
        assertTrue(description.type.nullable, "description should be nullable (not in required)")
    }

    @Test
    fun `GIVEN openapi 3_1 spec WHEN parsing item model THEN id is Int type`() {
        val spec = parseSpec("openapi-31.json")
        val item = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "Item" }
        val id = item.properties.first { it.originalName == "id" }
        assertEquals(DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.INT), id.type)
    }

    @Test
    fun `GIVEN openapi 3_1 spec with apiKey security scheme WHEN parsing THEN scheme is detected`() {
        val spec = parseSpec("openapi-31.json")
        val schemes = spec.clientConfiguration.apiKeySchemes
        assertEquals(1, schemes.size)
        assertEquals("X-API-KEY", schemes.first().keyName)
    }

    @Test
    fun `GIVEN openapi 3_2 spec with apiKey security scheme WHEN parsing THEN scheme is detected`() {
        val spec = parseSpec("openapi-32.json")
        val schemes = spec.clientConfiguration.apiKeySchemes
        assertEquals(1, schemes.size)
        assertEquals("X-API-KEY", schemes.first().keyName)
    }

    @Test
    fun `GIVEN openapi 3_1 spec with path parameter WHEN parsing THEN parameter location is PATH`() {
        val spec = parseSpec("openapi-31.json")
        val client = spec.clients.first()
        val getItemOp = client.operations.first { it.name == "GetItem" }
        val idParam = getItemOp.parameters.first { it.originalName == "id" }
        assertEquals(ParameterLocationSpec.PATH, idParam.location)
        assertTrue(idParam.required)
    }

    @Test
    fun `GIVEN openapi 3_2 spec with path parameter WHEN parsing THEN parameter location is PATH`() {
        val spec = parseSpec("openapi-32.json")
        val client = spec.clients.first()
        val getItemOp = client.operations.first { it.name == "GetItem" }
        val idParam = getItemOp.parameters.first { it.originalName == "id" }
        assertEquals(ParameterLocationSpec.PATH, idParam.location)
        assertTrue(idParam.required)
    }

    @Test
    fun `GIVEN openapi 3_1 spec with allOf schema WHEN parsing THEN merged properties are generated`() {
        val spec = parseSpec("openapi-31.json")
        val extended = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "ExtendedItem" }
        val propNames = extended.properties.map { it.originalName }.toSet()
        assertTrue("id" in propNames, "ExtendedItem should have 'id' from allOf BaseItem")
        assertTrue("name" in propNames, "ExtendedItem should have 'name' from inline allOf")
    }

    @Test
    fun `GIVEN openapi 3_2 spec with allOf schema WHEN parsing THEN merged properties are generated`() {
        val spec = parseSpec("openapi-32.json")
        val extended = spec.models.filterIsInstance<ModelSpec.DataClassSpec>().first { it.name == "ExtendedItem" }
        val propNames = extended.properties.map { it.originalName }.toSet()
        assertTrue("id" in propNames, "ExtendedItem should have 'id' from allOf BaseItem")
        assertTrue("name" in propNames, "ExtendedItem should have 'name' from inline allOf")
    }
}
