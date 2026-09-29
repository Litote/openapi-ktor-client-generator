package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.ClientConfigurationSpec
import org.litote.openapi.ktor.client.generator.domain.ClientSpec
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.domain.OperationSpec
import org.litote.openapi.ktor.client.generator.domain.ResponseEntrySpec
import org.litote.openapi.ktor.client.generator.domain.analyzeModelUsage
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BinaryResponseTest {
    private val config =
        ApiGeneratorConfiguration(
            openApiFile = "src/test/resources/binary-responses.json",
            outputDirectory = "build/binary-response-test",
            basePackage = "binary.test",
        )

    private val spec: GenerationSpec by lazy { OpenApiSpecificationParser(config).parse(config.operationFilter) }

    private fun operation(name: String): OperationSpec = spec.clients.flatMap { it.operations }.first { it.name == name }

    private fun successEntry(name: String): ResponseEntrySpec = operation(name).responses.first { it.isSuccess }

    @Test
    fun `GIVEN octet-stream response without schema WHEN parsing THEN body type is binary`() {
        val entry = successEntry("DownloadFile")

        assertIs<DomainTypeSpec.BinaryTypeSpec>(entry.bodyType)
        assertEquals(listOf("application/octet-stream"), entry.contentTypes)
    }

    @Test
    fun `GIVEN octet-stream response with json error WHEN parsing THEN error body keeps model type`() {
        val failure = operation("DownloadFile").responses.first { !it.isSuccess }

        assertEquals(DomainTypeSpec.ModelReferenceSpec("Error"), failure.bodyType)
        assertTrue(failure.contentTypes.isEmpty())
    }

    @Test
    fun `GIVEN image response WHEN parsing THEN body type is binary`() {
        val entry = successEntry("GetImage")

        assertIs<DomainTypeSpec.BinaryTypeSpec>(entry.bodyType)
        assertEquals(listOf("image/png"), entry.contentTypes)
    }

    @Test
    fun `GIVEN text plain response WHEN parsing THEN body type is String`() {
        val entry = successEntry("GetReport")

        assertEquals(DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING), entry.bodyType)
        assertEquals(listOf("text/plain"), entry.contentTypes)
    }

    @Test
    fun `GIVEN text csv response with charset WHEN parsing THEN body type is String and content type is normalized`() {
        val entry = successEntry("ExportCsv")

        assertEquals(DomainTypeSpec.PrimitiveSpec(DomainTypeSpec.PrimitiveSpec.KindSpec.STRING), entry.bodyType)
        assertEquals(listOf("text/csv"), entry.contentTypes)
    }

    @Test
    fun `GIVEN wildcard response with binary schema WHEN parsing THEN body type is binary`() {
        val entry = successEntry("Download")

        assertIs<DomainTypeSpec.BinaryTypeSpec>(entry.bodyType)
        assertTrue(entry.contentTypes.isEmpty())
    }

    @Test
    fun `GIVEN json and pdf responses WHEN parsing THEN json schema wins`() {
        val entry = successEntry("GetDocument")

        assertEquals(DomainTypeSpec.ModelReferenceSpec("Document"), entry.bodyType)
        assertTrue(entry.contentTypes.isEmpty())
    }

    @Test
    fun `GIVEN binary response WHEN analyzeModelUsage THEN no model is referenced`() {
        val binarySpec =
            GenerationSpec(
                clientConfiguration =
                    ClientConfigurationSpec(
                        serverUrl = "https://example.com",
                        apiKeySchemes = emptyList(),
                        componentParameters = emptyList(),
                    ),
                clients =
                    listOf(
                        ClientSpec(
                            name = "FilesClient",
                            operations =
                                listOf(
                                    OperationSpec(
                                        name = "download",
                                        path = "/download",
                                        method = "GET",
                                        parameters = emptyList(),
                                        responses =
                                            listOf(
                                                ResponseEntrySpec(
                                                    statusCodes = listOf(200),
                                                    bodyType = DomainTypeSpec.BinaryTypeSpec(),
                                                    isSuccess = true,
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                    ),
                models = listOf(ModelSpec.DataClassSpec(name = "Unused", properties = emptyList())),
            )

        val usage = analyzeModelUsage(binarySpec)

        assertEquals(emptySet(), usage["Unused"])
    }

    @Test
    fun `GIVEN binary type WHEN toggling nullability THEN nullable flag is updated`() {
        val binary = DomainTypeSpec.BinaryTypeSpec()

        assertTrue(binary.asNullable().nullable)
        assertTrue(!binary.asNullable().asNonNullable().nullable)
        assertTrue(!binary.isPrimitive)
    }

    @Test
    fun `GIVEN binary and text responses WHEN generating THEN client reads ByteArray and String bodies with accept header`() {
        File(config.outputDirectory).deleteRecursively()

        val result = generate(config)

        assertTrue(result.isSuccess, "Generation should succeed: $result")
        val clientSource =
            File(config.outputDirectory)
                .walkTopDown()
                .first { it.name == "FilesClient.kt" }
                .readText()
        assertTrue("body: ByteArray" in clientSource, clientSource)
        assertTrue("response.body<ByteArray>()" in clientSource, clientSource)
        assertTrue("response.body<String>()" in clientSource, clientSource)
        assertTrue("accept(ContentType.parse(\"application/octet-stream\"))" in clientSource, clientSource)
        assertTrue("accept(ContentType.parse(\"image/png\"))" in clientSource, clientSource)
        assertTrue("accept(ContentType.parse(\"text/csv\"))" in clientSource, clientSource)
    }
}
