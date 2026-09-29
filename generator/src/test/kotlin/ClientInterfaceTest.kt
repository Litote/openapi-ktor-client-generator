package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.domain.OperationMetaSpec
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClientInterfaceTest {
    private companion object {
        /** Generates [openApiFile] once and returns the source of [clientFileName]. */
        fun generateClient(
            openApiFile: String,
            outputDirectory: String,
            clientFileName: String,
            operationFilter: (OperationMetaSpec) -> Boolean = { true },
        ): String {
            File(outputDirectory).deleteRecursively()
            val result =
                generate(
                    ApiGeneratorConfiguration(
                        openApiFile = openApiFile,
                        outputDirectory = outputDirectory,
                        basePackage = "iface.test",
                        operationFilter = operationFilter,
                    ),
                )
            assertTrue(result.isSuccess, "Generation should succeed: $result")
            return File(outputDirectory)
                .walkTopDown()
                .first { it.name == clientFileName }
                .readText()
        }

        val petClientSource: String by lazy {
            generateClient("src/test/resources/sample.json", "build/client-interface-test/sample", "PetClient.kt")
        }

        val itemsClientSource: String by lazy {
            generateClient("src/test/resources/parameter-styles.json", "build/client-interface-test/styles", "ItemsClient.kt")
        }

        val streamingClientSource: String by lazy {
            generateClient(
                "src/test/resources/mastodon.json",
                "build/client-interface-test/sse",
                "StreamingClient.kt",
            ) { it.path.startsWith("/api/v1/streaming") }
        }
    }

    private fun assertContains(
        source: String,
        code: String,
    ) = assertTrue(code in source, "Missing `$code` in:\n$source")

    /** Returns the part of [source] declaring the implementation class. */
    private fun implementationPart(
        source: String,
        clientName: String,
    ): String = source.substringAfter("public class Default$clientName(")

    /** Returns the part of [source] declaring the interface. */
    private fun interfacePart(
        source: String,
        clientName: String,
    ): String = source.substringAfter("public interface $clientName {").substringBefore("public class Default$clientName(")

    @Test
    fun `GIVEN spec WHEN generating THEN client is an interface with nested response types`() {
        val interfaceSource = interfacePart(petClientSource, "PetClient")

        assertContains(petClientSource, "public interface PetClient {")
        assertContains(interfaceSource, "public sealed class AddPetResponse")
        assertContains(interfaceSource, "public data class AddPetResponseUnknownFailure(")
        assertContains(interfaceSource, "public suspend fun addPet(request: Pet): AddPetResponse\n")
        assertContains(interfaceSource, "test: String? = null,")
    }

    @Test
    fun `GIVEN spec WHEN generating THEN DefaultClient implements the interface with overrides without default values`() {
        val implementation = implementationPart(petClientSource, "PetClient")

        assertContains(petClientSource, "public class DefaultPetClient(")
        assertContains(implementation, ") : PetClient {")
        assertContains(implementation, "override suspend fun addPet(request: Pet): PetClient.AddPetResponse {")
        assertFalse("= null" in implementation.substringBefore("try {"), "Overrides must not declare default values")
        assertFalse("sealed class" in implementation, "Nested types must be declared in the interface")
    }

    @Test
    fun `GIVEN spec WHEN generating THEN a factory function named like the interface returns DefaultClient`() {
        assertContains(
            petClientSource,
            "public fun PetClient(configuration: ClientConfiguration = defaultClientConfiguration): PetClient =",
        )
        assertContains(petClientSource, "DefaultPetClient(configuration)")
    }

    @Test
    fun `GIVEN object parameters WHEN generating THEN serialization helpers are private members of DefaultClient`() {
        assertFalse("private fun" in interfacePart(itemsClientSource, "ItemsClient"), "Helpers must not be declared in the interface")
        assertContains(implementationPart(itemsClientSource, "ItemsClient"), "private fun ParametersBuilder.appendDeepObject(")
    }

    @Test
    fun `GIVEN SSE operation WHEN generating THEN it is abstract in the interface and overridden in DefaultClient`() {
        assertContains(
            interfacePart(streamingClientSource, "StreamingClient"),
            "public suspend fun getStreamingDirect(block: suspend ClientSSESession.() -> Unit)\n",
        )
        assertContains(
            implementationPart(streamingClientSource, "StreamingClient"),
            "override suspend fun getStreamingDirect(block: suspend ClientSSESession.() -> Unit) {",
        )
    }
}
