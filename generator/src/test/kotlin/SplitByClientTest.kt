package org.litote.openapi.ktor.client.generator

import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for the split-by-client generation mode.
 *
 * The test spec `split-by-client.json` has:
 * - `User` tag  → `UserClient`  using `UserResponse` (private) + `Address` (shared)
 * - `Order` tag → `OrderClient` using `OrderResponse` (private) + `Address` (shared)
 *
 * Expected partition:
 * - shared:              `Address`        (used by 2 clients)
 * - userClient:   `UserResponse`  (used only by UserClient)
 * - orderClient:  `OrderResponse` (used only by OrderClient)
 */
class SplitByClientTest {
    @TempDir
    lateinit var tempDir: File

    private companion object {
        private const val SPEC = "src/test/resources/split-by-client.json"
        private const val BASE_PKG = "com.example.split"
    }

    private fun generateShared(): GenerationResult =
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = BASE_PKG,
                splitByClient = true,
                targetClientName = null,
            ),
        )

    private fun generateClient(clientName: String): GenerationResult =
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = BASE_PKG,
                splitByClient = true,
                targetClientName = clientName,
            ),
        )

    private fun generatedFiles(): List<String> =
        tempDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .map { it.relativeTo(tempDir).path }
            .sorted()
            .toList()

    // ─────────────────────────────────────────────────────────────
    // Shared generation (targetClientName = null)
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN split spec WHEN generating shared THEN result is success`() {
        val result = generateShared()
        assertIs<GenerationResult.Success>(result)
    }

    @Test
    fun `GIVEN split spec WHEN generating shared THEN no client files are generated`() {
        generateShared()

        val files = generatedFiles()
        assertTrue(files.none { it.contains("Client.kt") }, "No client files expected in shared output, got: $files")
    }

    @Test
    fun `GIVEN split spec WHEN generating shared THEN ClientConfiguration is generated`() {
        generateShared()

        val files = generatedFiles()
        assertTrue(
            files.any { it.endsWith("ClientConfiguration.kt") },
            "ClientConfiguration.kt expected in shared output, got: $files",
        )
    }

    @Test
    fun `GIVEN split spec WHEN generating shared THEN Address (shared model) is generated`() {
        generateShared()

        val files = generatedFiles()
        assertTrue(
            files.any { it.endsWith("Address.kt") },
            "Address.kt (shared model) expected in shared output, got: $files",
        )
    }

    @Test
    fun `GIVEN split spec WHEN generating shared THEN private models are NOT generated`() {
        generateShared()

        val files = generatedFiles()
        assertFalse(
            files.any { it.endsWith("UserResponse.kt") },
            "UserResponse.kt should NOT be in shared output, got: $files",
        )
        assertFalse(
            files.any { it.endsWith("OrderResponse.kt") },
            "OrderResponse.kt should NOT be in shared output, got: $files",
        )
    }

    @Test
    fun `GIVEN split spec WHEN generating shared THEN shared model count is 1`() {
        val result = generateShared()
        val success = assertIs<GenerationResult.Success>(result)
        assertEquals(1, success.modelsGenerated, "Only Address (shared model) should be generated")
    }

    // ─────────────────────────────────────────────────────────────
    // Per-client generation (targetClientName = "UserClient")
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN split spec WHEN generating UserClient THEN result is success`() {
        val result = generateClient("UserClient")
        assertIs<GenerationResult.Success>(result)
    }

    @Test
    fun `GIVEN split spec WHEN generating UserClient THEN UserClient file is generated`() {
        generateClient("UserClient")

        val files = generatedFiles()
        assertTrue(
            files.any { it.endsWith("UserClient.kt") },
            "UserClient.kt expected, got: $files",
        )
    }

    @Test
    fun `GIVEN split spec WHEN generating UserClient THEN UserResponse (private) is generated`() {
        generateClient("UserClient")

        val files = generatedFiles()
        assertTrue(
            files.any { it.endsWith("UserResponse.kt") },
            "UserResponse.kt (private model) expected, got: $files",
        )
    }

    @Test
    fun `GIVEN split spec WHEN generating UserClient THEN shared model Address is NOT generated`() {
        generateClient("UserClient")

        val files = generatedFiles()
        assertFalse(
            files.any { it.endsWith("Address.kt") },
            "Address.kt (shared model) should NOT be in UserClient output, got: $files",
        )
    }

    @Test
    fun `GIVEN split spec WHEN generating UserClient THEN ClientConfiguration is NOT generated`() {
        generateClient("UserClient")

        val files = generatedFiles()
        assertFalse(
            files.any { it.endsWith("ClientConfiguration.kt") },
            "ClientConfiguration.kt should NOT be in client output, got: $files",
        )
    }

    @Test
    fun `GIVEN split spec WHEN generating UserClient THEN OrderClient is NOT generated`() {
        generateClient("UserClient")

        val files = generatedFiles()
        assertFalse(
            files.any { it.endsWith("OrderClient.kt") },
            "OrderClient.kt should NOT be in UserClient output, got: $files",
        )
    }

    // ─────────────────────────────────────────────────────────────
    // Per-client generation (targetClientName = "OrderClient")
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN split spec WHEN generating OrderClient THEN OrderResponse (private) is generated`() {
        generateClient("OrderClient")

        val files = generatedFiles()
        assertTrue(
            files.any { it.endsWith("OrderResponse.kt") },
            "OrderResponse.kt (private model) expected, got: $files",
        )
    }

    @Test
    fun `GIVEN split spec WHEN generating OrderClient THEN shared model Address is NOT generated`() {
        generateClient("OrderClient")

        val files = generatedFiles()
        assertFalse(
            files.any { it.endsWith("Address.kt") },
            "Address.kt (shared model) should NOT be in OrderClient output, got: $files",
        )
    }

    // ─────────────────────────────────────────────────────────────
    // Error cases
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN split spec WHEN generating unknown client THEN result is failure`() {
        val failure = assertIs<GenerationResult.Failure>(generateClient("UnknownClient"))
        assertTrue(failure.message.contains("UnknownClient"), "Error should mention the unknown client name")
    }

    // ─────────────────────────────────────────────────────────────
    // Cross-module imports when basePackage differs from sharedBasePackage
    // ─────────────────────────────────────────────────────────────

    private fun generateClientWithSharedPackage(clientName: String): File {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = "$BASE_PKG.${clientName.removeSuffix("Client").lowercase()}",
                sharedBasePackage = BASE_PKG,
                splitByClient = true,
                targetClientName = clientName,
            ),
        )
        return tempDir.walkTopDown().first { it.name == "$clientName.kt" }
    }

    @Test
    fun `GIVEN client with distinct basePackage WHEN generating THEN UserClient imports ClientConfiguration from sharedBasePackage`() {
        val clientFile = generateClientWithSharedPackage("UserClient").readText()

        assertTrue(
            clientFile.contains("$BASE_PKG.client.ClientConfiguration"),
            "UserClient should import ClientConfiguration from '$BASE_PKG.client', got:\n$clientFile",
        )
        assertFalse(
            clientFile.contains("$BASE_PKG.user.client.ClientConfiguration"),
            "UserClient must NOT import ClientConfiguration from its own package, got:\n$clientFile",
        )
    }

    @Test
    fun `GIVEN client with distinct basePackage WHEN generating THEN UserClient references shared model Address from sharedBasePackage`() {
        val clientFile = generateClientWithSharedPackage("UserClient").readText()

        assertTrue(
            clientFile.contains("$BASE_PKG.model.Address") || clientFile.contains("import $BASE_PKG.model"),
            "UserClient should reference Address from '$BASE_PKG.model', got:\n$clientFile",
        )
        assertFalse(
            clientFile.contains("$BASE_PKG.user.model.Address"),
            "UserClient must NOT reference Address from its own model package, got:\n$clientFile",
        )
    }

    @Test
    fun `GIVEN client with distinct basePackage WHEN generating THEN private model UserResponse uses sharedBasePackage model package`() {
        generateClientWithSharedPackage("UserClient")

        val modelFile =
            tempDir
                .walkTopDown()
                .first { it.name == "UserResponse.kt" }
                .readText()

        assertTrue(
            modelFile.startsWith("package $BASE_PKG.model"),
            "UserResponse.kt should declare 'package $BASE_PKG.model', got:\n${modelFile.lines().first()}",
        )
    }

    @Test
    fun `GIVEN client with distinct basePackage WHEN generating THEN response sealed class uses client package not shared package`() {
        val clientFile = generateClientWithSharedPackage("UserClient").readText()

        // GetUsersResponse is a nested type of UserClient.
        // Its FQN must use the client's own package (basePackage.user.client),
        // NOT the shared client package (sharedBasePackage.client).
        assertFalse(
            clientFile.contains("$BASE_PKG.client.UserClient"),
            "Response sealed class must NOT reference UserClient from the shared client package, got:\n$clientFile",
        )
    }
}
