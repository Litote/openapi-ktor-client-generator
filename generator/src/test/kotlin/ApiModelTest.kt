package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.DefaultValueSpec
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
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

        assertEquals(DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.LONG), idProp.type)
        assertEquals(DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING), nameProp.type)
    }

    @Test
    fun `GIVEN request body reference WHEN getClassName THEN maps to model class`() {
        val spec = loadSpec("openapi.json")
        val client = spec.clients.first()
        val operation = client.operations.first { it.requestBody != null }
        val requestBody = operation.requestBody
        assertNotNull(requestBody)

        assertEquals(DomainTypeSpec.ModelReferenceSpec("TestRequest"), requestBody.type)
    }

    @Test
    fun `GIVEN component parameters with numeric defaults WHEN parsing THEN defaults are resolved to correct types`() {
        val spec = loadSpec("component-params.json")
        val params = spec.clientConfiguration.componentParameters.associateBy { it.originalName }

        assertEquals(DefaultValueSpec.DoubleDefaultSpec(3.14), params["doubleParam"]?.defaultValue)
        assertEquals(DefaultValueSpec.FloatDefaultSpec(1.5f), params["floatParam"]?.defaultValue)
        assertEquals(DefaultValueSpec.IntDefaultSpec(10), params["intParam"]?.defaultValue)
        assertEquals(DefaultValueSpec.LongDefaultSpec(100L), params["longParam"]?.defaultValue)
        assertEquals(DefaultValueSpec.EnumDefaultSpec(typeName = "STATUSPARAM", enumValue = "ACTIVE"), params["statusParam"]?.defaultValue)
    }

    @Test
    fun `GIVEN component parameters WHEN referenced in operation THEN constName and constDefaultName are set`() {
        val spec = loadSpec("component-params.json")
        val operation =
            spec.clients
                .first()
                .operations
                .first()
        val intParam = operation.parameters.first { it.originalName == "intParam" }

        assertEquals("PARAMETER_INTPARAM", intParam.constName)
        assertEquals("PARAMETER_INTPARAM_DEFAULT_VALUE", intParam.constDefaultName)
    }
}
