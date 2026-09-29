package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec.PrimitiveSpec
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec.PrimitiveSpec.KindSpec
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.OperationSpec
import org.litote.openapi.ktor.client.generator.domain.ResponseEntrySpec
import org.litote.openapi.ktor.client.generator.domain.ResponseHeaderSpec
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResponseHeadersTest {
    private val config =
        ApiGeneratorConfiguration(
            openApiFile = "src/test/resources/response-headers.json",
            outputDirectory = "build/response-headers-test",
            basePackage = "headers.test",
        )

    private val spec: GenerationSpec by lazy { OpenApiSpecificationParser(config).parse(config.operationFilter) }

    private fun operation(name: String): OperationSpec = spec.clients.flatMap { it.operations }.first { it.name == name }

    private fun entry(
        name: String,
        statusCode: Int,
    ): ResponseEntrySpec = operation(name).responses.first { statusCode in it.statusCodes }

    private fun ResponseEntrySpec.header(originalName: String): ResponseHeaderSpec = headers.first { it.originalName == originalName }

    @Test
    fun `GIVEN inline integer header WHEN parsing THEN header is typed Int with description`() {
        val header = entry("ListItems", 200).header("X-Total-Count")

        assertEquals("xTotalCount", header.propertyName)
        assertEquals(PrimitiveSpec(KindSpec.INT), header.type)
        assertEquals("Total number of items", header.description)
    }

    @Test
    fun `GIVEN referenced component header WHEN parsing THEN header is resolved`() {
        val header = entry("ListItems", 200).header("Link")

        assertEquals("link", header.propertyName)
        assertEquals(PrimitiveSpec(KindSpec.STRING), header.type)
        assertEquals("Pagination links", header.description)
    }

    @Test
    fun `GIVEN primitive header schemas WHEN parsing THEN headers are typed accordingly`() {
        val entry = entry("ListItems", 200)

        assertEquals(PrimitiveSpec(KindSpec.LONG), entry.header("X-Big-Counter").type)
        assertEquals(PrimitiveSpec(KindSpec.DOUBLE), entry.header("X-Score").type)
        assertEquals(PrimitiveSpec(KindSpec.BOOLEAN), entry.header("X-Cached").type)
    }

    @Test
    fun `GIVEN non primitive header schemas WHEN parsing THEN headers fall back to String`() {
        val entry = entry("ListItems", 200)

        assertEquals(PrimitiveSpec(KindSpec.STRING), entry.header("X-Expires-At").type)
        assertEquals(PrimitiveSpec(KindSpec.STRING), entry.header("X-Mode").type)
        assertEquals(PrimitiveSpec(KindSpec.STRING), entry.header("X-Tags").type)
    }

    @Test
    fun `GIVEN Content-Type response header WHEN parsing THEN it is ignored`() {
        assertTrue(entry("ListItems", 200).headers.none { it.originalName.equals("Content-Type", ignoreCase = true) })
    }

    @Test
    fun `GIVEN headers colliding with generated properties WHEN parsing THEN property names get a Header suffix`() {
        assertEquals("bodyHeader", entry("ListItems", 200).header("Body").propertyName)
        assertEquals("headersHeader", entry("DeleteItem", 204).header("Headers").propertyName)
        assertEquals("statusCodeHeader", entry("DeleteItem", 204).header("Status-Code").propertyName)
    }

    @Test
    fun `GIVEN failure response with header WHEN parsing THEN failure entry exposes the header`() {
        val header = entry("ListItems", 429).header("Retry-After")

        assertEquals("retryAfter", header.propertyName)
        assertEquals(PrimitiveSpec(KindSpec.INT), header.type)
    }

    @Test
    fun `GIVEN grouped status codes with different headers WHEN parsing THEN headers are merged and sorted`() {
        val entry = entry("CreateItem", 201)

        assertEquals(listOf(201, 202), entry.statusCodes)
        assertEquals(listOf("ETag", "Location"), entry.headers.map { it.originalName })
    }

    @Test
    fun `GIVEN response headers WHEN generating THEN response classes expose raw and typed headers`() {
        File(config.outputDirectory).deleteRecursively()

        val result = generate(config)

        assertTrue(result.isSuccess, "Generation should succeed: $result")
        val clientSource =
            File(config.outputDirectory)
                .walkTopDown()
                .first { it.name == "ItemsClient.kt" }
                .readText()
        assertTrue("public abstract val headers: Headers" in clientSource, clientSource)
        assertTrue("override val headers: Headers = Headers.Empty" in clientSource, clientSource)
        assertTrue("public data class DeleteItemResponseSuccess(" in clientSource, clientSource)
        assertTrue("get() = headers[\"X-Total-Count\"]?.toIntOrNull()" in clientSource, clientSource)
        assertTrue("get() = headers[\"X-Big-Counter\"]?.toLongOrNull()" in clientSource, clientSource)
        assertTrue("get() = headers[\"X-Score\"]?.toDoubleOrNull()" in clientSource, clientSource)
        assertTrue("get() = headers[\"X-Cached\"]?.toBooleanStrictOrNull()" in clientSource, clientSource)
        assertTrue("get() = headers[\"Link\"]\n" in clientSource, clientSource)
        assertTrue("ListItemsResponseSuccess(response.body<List<Item>>(), response.headers)" in clientSource, clientSource)
        assertTrue("ListItemsResponseFailure(response.headers)" in clientSource, clientSource)
        assertTrue("ListItemsResponseUnknownFailure(response.status.value, response.headers)" in clientSource, clientSource)
    }
}
