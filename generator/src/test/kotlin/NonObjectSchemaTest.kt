package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec.PrimitiveSpec.KindSpec
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NonObjectSchemaTest {
    private val config =
        ApiGeneratorConfiguration(
            openApiFile = "src/test/resources/non-object-schemas.json",
            outputDirectory = "build/non-object-schema-test",
            basePackage = "nonobject.test",
        )

    private val spec: GenerationSpec by lazy { OpenApiSpecificationParser(config).parse(config.operationFilter) }

    private fun routeProperty(name: String): DomainTypeSpec =
        spec.models
            .filterIsInstance<ModelSpec.DataClassSpec>()
            .first { it.name == "Route" }
            .properties
            .first { it.camelCaseName == name }
            .type

    @Test
    fun `GIVEN array component schema WHEN parsing THEN reference is replaced by the list type`() {
        val number = DomainTypeSpec.PrimitiveSpec(KindSpec.DOUBLE)

        assertEquals(DomainTypeSpec.ListTypeSpec(DomainTypeSpec.ListTypeSpec(number)), routeProperty("tokens"))
    }

    @Test
    fun `GIVEN array component schema of models WHEN parsing THEN items keep the model reference`() {
        assertEquals(
            DomainTypeSpec.ListTypeSpec(DomainTypeSpec.ModelReferenceSpec("Point"), nullable = true),
            routeProperty("shape"),
        )
        assertTrue(spec.models.any { it.name == "Point" })
    }

    @Test
    fun `GIVEN primitive component schemas WHEN parsing THEN references are replaced by primitive types`() {
        assertEquals(DomainTypeSpec.PrimitiveSpec(KindSpec.DOUBLE, nullable = true), routeProperty("speed"))
        assertEquals(DomainTypeSpec.PrimitiveSpec(KindSpec.STRING, nullable = true), routeProperty("label"))
    }

    @Test
    fun `GIVEN primitive component schema used by a parameter WHEN parsing THEN parameter has the primitive type`() {
        val parameter =
            spec.clients
                .flatMap { it.operations }
                .flatMap { it.parameters }
                .first { it.originalName == "speed" }

        assertEquals(DomainTypeSpec.PrimitiveSpec(KindSpec.DOUBLE, nullable = true), parameter.type)
    }

    @Test
    fun `GIVEN non object component schemas WHEN parsing THEN no model is generated for them`() {
        val names = spec.models.map { it.name }.toSet()

        assertTrue(names.none { it in setOf("Token", "Shape", "Speed", "Label") }, "unexpected models: $names")
    }
}
