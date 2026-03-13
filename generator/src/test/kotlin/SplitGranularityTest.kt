package org.litote.openapi.ktor.client.generator

import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [SplitGranularity] support.
 *
 * The test spec `split-by-client.json` has:
 * - `User` tag → `GET /users`
 * - `Order` tag → `GET /orders`
 */
class SplitGranularityTest {
    @TempDir
    lateinit var tempDir: File

    private companion object {
        private const val SPEC = "src/test/resources/split-by-client.json"
        private const val BASE_PKG = "com.example.granularity"
    }

    private fun generate(granularity: SplitGranularity): GenerationResult =
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = BASE_PKG,
                splitGranularity = granularity,
            ),
        )

    private fun generatedClientNames(): List<String> =
        tempDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.path.contains("/client/") }
            .map { it.nameWithoutExtension }
            .sorted()
            .toList()

    // ─────────────────────────────────────────────────────────────
    // BY_TAG (default)
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN BY_TAG WHEN generating THEN result is success`() {
        assertIs<GenerationResult.Success>(generate(SplitGranularity.BY_TAG))
    }

    @Test
    fun `GIVEN BY_TAG WHEN generating THEN one client per tag`() {
        generate(SplitGranularity.BY_TAG)
        val clients = generatedClientNames().filter { it.endsWith("Client") }
        assertEquals(listOf("OrderClient", "UserClient"), clients)
    }

    @Test
    fun `GIVEN BY_TAG WHEN parseClientNames THEN returns tag-based names`() {
        val names = parseClientNames(SPEC, SplitGranularity.BY_TAG)
        assertEquals(listOf("UserClient", "OrderClient"), names)
    }

    // ─────────────────────────────────────────────────────────────
    // BY_TAG_AND_PATH
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN BY_TAG_AND_PATH WHEN generating THEN result is success`() {
        assertIs<GenerationResult.Success>(generate(SplitGranularity.BY_TAG_AND_PATH))
    }

    @Test
    fun `GIVEN BY_TAG_AND_PATH WHEN generating THEN client names include path`() {
        generate(SplitGranularity.BY_TAG_AND_PATH)
        val clients = generatedClientNames().filter { it.endsWith("Client") }
        // /users path → UserUsersClient, /orders path → OrderOrdersClient
        assertTrue(clients.any { it.contains("Users") }, "Expected a client containing 'Users', got $clients")
        assertTrue(clients.any { it.contains("Orders") }, "Expected a client containing 'Orders', got $clients")
    }

    @Test
    fun `GIVEN BY_TAG_AND_PATH WHEN parseClientNames THEN returns path-qualified names`() {
        val names = parseClientNames(SPEC, SplitGranularity.BY_TAG_AND_PATH)
        assertTrue(names.all { it.endsWith("Client") }, "All names should end with 'Client': $names")
        assertTrue(names.any { it.contains("Users") }, "Expected name with 'Users' segment: $names")
        assertTrue(names.any { it.contains("Orders") }, "Expected name with 'Orders' segment: $names")
    }

    // ─────────────────────────────────────────────────────────────
    // BY_TAG_AND_OPERATION
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN BY_TAG_AND_OPERATION WHEN generating THEN result is success`() {
        assertIs<GenerationResult.Success>(generate(SplitGranularity.BY_TAG_AND_OPERATION))
    }

    @Test
    fun `GIVEN BY_TAG_AND_OPERATION WHEN generating THEN client names include path and method`() {
        generate(SplitGranularity.BY_TAG_AND_OPERATION)
        val clients = generatedClientNames().filter { it.endsWith("Client") }
        // /users GET → UserUsersGetClient
        assertTrue(clients.any { it.contains("Get") }, "Expected a client containing 'Get': $clients")
    }

    @Test
    fun `GIVEN BY_TAG_AND_OPERATION WHEN parseClientNames THEN returns operation-qualified names`() {
        val names = parseClientNames(SPEC, SplitGranularity.BY_TAG_AND_OPERATION)
        assertTrue(names.all { it.endsWith("Client") }, "All names should end with 'Client': $names")
        // Each operation becomes a unique client
        assertEquals(2, names.size, "Expected one client per operation, got $names")
    }

    // ─────────────────────────────────────────────────────────────
    // parseClientNames backward-compat (no granularity arg)
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN default parseClientNames WHEN called THEN uses BY_TAG`() {
        val defaultNames = parseClientNames(SPEC)
        val byTagNames = parseClientNames(SPEC, SplitGranularity.BY_TAG)
        assertEquals(byTagNames, defaultNames)
    }
}
