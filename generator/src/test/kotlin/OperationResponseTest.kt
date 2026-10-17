package org.litote.openapi.ktor.client.generator

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class OperationResponseTest {
    private val config =
        ApiGeneratorConfiguration(
            openApiFile = "src/test/resources/operation-responses.json",
            outputDirectory = "build/operation-responses-test",
            basePackage = "responses.test",
        )

    private val sources: Map<String, String> by lazy {
        File(config.outputDirectory).deleteRecursively()
        val result = generate(config)
        assertTrue(result.isSuccess, "Generation should succeed: $result")
        File(config.outputDirectory)
            .walkTopDown()
            .filter { it.isFile }
            .associate { it.name to it.readText() }
    }

    private fun source(fileName: String): String = sources[fileName] ?: error("$fileName not generated: ${sources.keys}")

    private fun assertContains(
        source: String,
        expected: String,
    ) {
        assertTrue(expected in source, "Expected:\n$expected\nin:\n$source")
    }

    @Test
    fun `GIVEN a spec WHEN generating THEN the common response interfaces are generated with the client configuration`() {
        val responses = source("OperationResponse.kt")

        assertContains(responses, "package responses.test.client")
        assertContains(responses, "public interface OperationResponse<out T> {")
        assertContains(responses, "public interface OperationResponseSuccess<out T> : OperationResponse<T> {")
        assertContains(responses, "public interface OperationResponseFailure<out E> {")
        assertContains(responses, "public interface OperationResponseUnknownFailure {")
    }

    @Test
    fun `GIVEN a single success body type WHEN generating THEN the response is typed by it`() {
        val client = source("ItemsClient.kt")

        assertContains(client, "public sealed class GetItemResponse : OperationResponse<Item> {")
        assertContains(client, "abstract override val headers: Headers")
        assertContains(client, ") : GetItemResponse(),\n      OperationResponseSuccess<Item>")
        assertContains(client, "override val body: Item,")
        assertContains(client, ") : GetItemResponse(),\n      OperationResponseFailure<Error>")
        assertContains(client, "override val body: Error,")
        assertContains(client, ") : GetItemResponse(),\n      OperationResponseUnknownFailure")
        assertContains(client, "override val statusCode: Int,")
    }

    @Test
    fun `GIVEN responses without body WHEN generating THEN their body is Unit`() {
        val client = source("ItemsClient.kt")

        assertContains(client, "public sealed class DeleteItemResponse : OperationResponse<Unit> {")
        assertContains(
            client,
            ") : DeleteItemResponse(),\n      OperationResponseSuccess<Unit> {\n    override val body: Unit\n      get() = Unit",
        )
        assertContains(
            client,
            ") : DeleteItemResponse(),\n      OperationResponseFailure<Unit> {\n    override val body: Unit\n      get() = Unit",
        )
    }

    @Test
    fun `GIVEN success responses of different body types WHEN generating THEN the response is typed by Any`() {
        val client = source("ItemsClient.kt")

        assertContains(client, "public sealed class CreateItemResponse : OperationResponse<Any?> {")
        assertContains(client, ") : CreateItemResponse(),\n      OperationResponseSuccess<Any?>")
        assertContains(client, "override val body: Job,")
    }
}
