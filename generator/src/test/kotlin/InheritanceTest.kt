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
        assertIs<ModelSpec.SealedClassSpec>(statusModel)

        val subtypeNames =
            generationSpec.models
                .filterIsInstance<ModelSpec.DataClassSpec>()
                .filter { it.sealedParentName == "Status" }
                .map { it.name }
        assertTrue(subtypeNames.containsAll(listOf("TextStatus", "MediaStatus", "PollStatus")))
    }

    @Test
    fun `GIVEN allOf schema with ref WHEN building model THEN merges properties from referenced schema`() {
        val (_, generationSpec) = loadSpec("allof-inheritance.json")

        val textStatusModel = generationSpec.models.first { it.name == "TextStatus" }
        assertIs<ModelSpec.DataClassSpec>(textStatusModel)

        val propertyNames = textStatusModel.properties.map { it.originalName }
        assertTrue(propertyNames.contains("status"), "Should have own property 'status'")
        assertTrue(propertyNames.contains("language"), "Should have BaseStatus property 'language'")
        assertTrue(propertyNames.contains("sensitive"), "Should have BaseStatus property 'sensitive'")
    }

    @Test
    fun `GIVEN allOf schema with ref and sealed parent WHEN building model THEN extends sealed parent`() {
        val (_, generationSpec) = loadSpec("allof-inheritance.json")

        val textStatusModel = generationSpec.models.first { it.name == "TextStatus" }
        assertIs<ModelSpec.DataClassSpec>(textStatusModel)

        assertEquals(
            "CreateStatusRequest",
            textStatusModel.sealedParentName,
            "TextStatus should extend CreateStatusRequest (from oneOf in request body)",
        )
    }

    @Test
    fun `GIVEN schema referenced only via allOf WHEN building model THEN generates interface`() {
        val (_, generationSpec) = loadSpec("allof-inheritance.json")

        val baseStatusModel = generationSpec.models.first { it.name == "BaseStatus" }
        assertIs<ModelSpec.InterfaceSpec>(baseStatusModel)
    }

    @Test
    fun `GIVEN DataClass implementing interface via allOf WHEN building model THEN interface is listed as parent`() {
        val (_, generationSpec) = loadSpec("allof-inheritance.json")

        val textStatusModel = generationSpec.models.first { it.name == "TextStatus" }
        assertIs<ModelSpec.DataClassSpec>(textStatusModel)
        assertTrue(
            textStatusModel.interfaceParentNames.contains("BaseStatus"),
            "TextStatus should implement BaseStatus interface",
        )
    }

    @Test
    fun `GIVEN DataClass implementing interface via allOf WHEN building model THEN interface properties are marked override`() {
        val (_, generationSpec) = loadSpec("allof-inheritance.json")

        val textStatusModel = generationSpec.models.first { it.name == "TextStatus" }
        assertIs<ModelSpec.DataClassSpec>(textStatusModel)

        val overrideProps = textStatusModel.properties.filter { it.isOverride }.map { it.originalName }
        assertTrue(overrideProps.contains("language"), "'language' should be override (from BaseStatus)")
        assertTrue(overrideProps.contains("sensitive"), "'sensitive' should be override (from BaseStatus)")

        val ownProps = textStatusModel.properties.filter { !it.isOverride }.map { it.originalName }
        assertTrue(ownProps.contains("status"), "'status' should NOT be override (own property)")
    }

    private fun loadSpec(
        fileName: String,
    ): Pair<ApiGeneratorConfiguration, org.litote.openapi.ktor.client.generator.domain.GenerationSpec> {
        val configuration =
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/$fileName",
                outputDirectory = "build/openapi-test",
            )
        val parser = OpenApiSpecificationParser(configuration)
        return configuration to parser.parse(configuration.operationFilter)
    }
}
