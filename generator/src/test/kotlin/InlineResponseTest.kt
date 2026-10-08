package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.domain.OperationSpec
import org.litote.openapi.ktor.client.generator.domain.analyzeModelUsage
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InlineResponseTest {
    private val testSpec = "src/test/resources/inline-responses.json"

    private val spec: GenerationSpec by lazy {
        val configuration = ApiGeneratorConfiguration(openApiFile = testSpec, outputDirectory = "")
        OpenApiSpecificationParser(configuration).parse { true }
    }

    private fun operation(name: String): OperationSpec = spec.clients.flatMap { it.operations }.first { it.name == name }

    private fun generateFiles(
        splitByClient: Boolean = false,
        targetClientName: String? = null,
    ): Map<String, String> {
        val outputDir = Files.createTempDirectory("inline-responses-test").toFile()
        try {
            val result =
                generate(
                    ApiGeneratorConfiguration(
                        openApiFile = testSpec,
                        outputDirectory = outputDir.absolutePath,
                        splitByClient = splitByClient,
                        targetClientName = targetClientName,
                    ),
                )
            assertTrue(result.isSuccess, "Generation should succeed: $result")
            return outputDir
                .walkTopDown()
                .filter(File::isFile)
                .associate { it.name to it.readText() }
        } finally {
            outputDir.deleteRecursively()
        }
    }

    @Test
    fun `GIVEN inline object response schema WHEN parsing THEN body type is an inline model`() {
        val plan = operation("Plan")
        val success = plan.responses.first { it.isSuccess }

        assertEquals(DomainTypeSpec.InlineTypeSpec("PlanResponseBody"), success.bodyType)
        val model = plan.inlineModels.single { it.name == "PlanResponseBody" }
        assertIs<ModelSpec.DataClassSpec>(model)
        assertEquals(listOf("debug", "itineraries", "nextPageCursor"), model.properties.map { it.originalName })
        assertEquals(
            DomainTypeSpec.ListTypeSpec(DomainTypeSpec.ModelReferenceSpec("Itinerary")),
            model.properties.first { it.originalName == "itineraries" }.type,
        )
    }

    @Test
    fun `GIVEN referenced response schema WHEN parsing THEN body type stays a model reference`() {
        val failure = operation("Plan").responses.first { !it.isSuccess }

        assertEquals(DomainTypeSpec.ModelReferenceSpec("Error"), failure.bodyType)
    }

    @Test
    fun `GIVEN several inline response schemas WHEN parsing THEN each model name contains its status code`() {
        val stops = operation("Stops")

        assertEquals(
            listOf(DomainTypeSpec.InlineTypeSpec("StopsResponse200Body"), DomainTypeSpec.InlineTypeSpec("StopsResponse404Body")),
            stops.responses.map { it.bodyType },
        )
        assertEquals(listOf("StopsResponse200Body", "StopsResponse404Body"), stops.inlineModels.map { it.name })
    }

    @Test
    fun `GIVEN free-form object response schema WHEN parsing THEN body type stays JsonElement`() {
        val raw = operation("Raw")

        assertIs<DomainTypeSpec.JsonTypeSpec>(raw.responses.single().bodyType)
        assertTrue(raw.inlineModels.isEmpty())
    }

    @Test
    fun `GIVEN model referenced only by an inline response WHEN analyzing usage THEN it is used by the client`() {
        val usage = analyzeModelUsage(spec)

        assertEquals(setOf("RoutingClient"), usage["Itinerary"])
    }

    @Test
    fun `GIVEN inline object response schema WHEN generating THEN client returns the typed nested class`() {
        val client = generateFiles().getValue("RoutingClient.kt")

        assertTrue(client.contains("public data class PlanResponseBody("), client)
        assertTrue(client.contains("public val itineraries: List<Itinerary>"), client)
        assertTrue(client.contains("public val nextPageCursor: String"), client)
        assertTrue(client.contains("public val body: PlanResponseBody,"), client)
        assertTrue(client.contains("response.body<RoutingClient.PlanResponseBody>()"), client)
    }

    @Test
    fun `GIVEN model used only by an inline response and split by client WHEN generating client THEN the model is generated`() {
        val files = generateFiles(splitByClient = true, targetClientName = "RoutingClient")

        assertTrue(files.containsKey("Itinerary.kt"), files.keys.toString())
    }
}
