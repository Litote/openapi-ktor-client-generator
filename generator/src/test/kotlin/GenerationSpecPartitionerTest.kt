package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.application.GenerationSpecPartitioner
import org.litote.openapi.ktor.client.generator.domain.ClientConfigurationSpec
import org.litote.openapi.ktor.client.generator.domain.ClientSpec
import org.litote.openapi.ktor.client.generator.domain.DomainType
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.domain.OperationSpec
import org.litote.openapi.ktor.client.generator.domain.ResponseEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerationSpecPartitionerTest {
    private fun minimalConfig() =
        ClientConfigurationSpec(
            serverUrl = "https://example.com",
            apiKeySchemes = emptyList(),
            componentParameters = emptyList(),
        )

    private fun operationWithResponse(modelName: String): OperationSpec =
        OperationSpec(
            name = "doSomething",
            path = "/something",
            method = "GET",
            parameters = emptyList(),
            responses =
                listOf(
                    ResponseEntry(
                        statusCodes = listOf(200),
                        bodyType = DomainType.ModelReference(modelName),
                        isSuccess = true,
                    ),
                ),
        )

    private fun noResponseOperation(): OperationSpec =
        OperationSpec(
            name = "ping",
            path = "/ping",
            method = "GET",
            parameters = emptyList(),
            responses = listOf(ResponseEntry(statusCodes = listOf(200), bodyType = null, isSuccess = true)),
        )

    @Test
    fun `GIVEN spec with shared model (2 clients) WHEN partition THEN shared spec contains that model`() {
        val sharedModel = ModelSpec.DataClassSpec(name = "SharedModel", properties = emptyList())
        val client1 = ClientSpec(name = "Client1", operations = listOf(operationWithResponse("SharedModel")))
        val client2 = ClientSpec(name = "Client2", operations = listOf(operationWithResponse("SharedModel")))
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client1, client2),
                models = listOf(sharedModel),
            )

        val result = GenerationSpecPartitioner().partition(spec)

        assertTrue(result.shared.models.any { it.name == "SharedModel" })
    }

    @Test
    fun `GIVEN spec with private model (1 client) WHEN partition THEN perClient spec contains that model`() {
        val privateModel = ModelSpec.DataClassSpec(name = "PrivateModel", properties = emptyList())
        val client1 = ClientSpec(name = "Client1", operations = listOf(operationWithResponse("PrivateModel")))
        val client2 = ClientSpec(name = "Client2", operations = listOf(noResponseOperation()))
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client1, client2),
                models = listOf(privateModel),
            )

        val result = GenerationSpecPartitioner().partition(spec)

        val client1Spec = result.perClient.find { it.clientName == "Client1" }
        assertTrue(client1Spec?.spec?.models?.any { it.name == "PrivateModel" } == true)
        val sharedModelNames = result.shared.models.map { it.name }
        assertTrue("PrivateModel" !in sharedModelNames)
    }

    @Test
    fun `GIVEN spec WHEN partition THEN shared spec has empty clients list`() {
        val model = ModelSpec.DataClassSpec(name = "SomeModel", properties = emptyList())
        val client1 = ClientSpec(name = "Client1", operations = listOf(operationWithResponse("SomeModel")))
        val client2 = ClientSpec(name = "Client2", operations = listOf(operationWithResponse("SomeModel")))
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client1, client2),
                models = listOf(model),
            )

        val result = GenerationSpecPartitioner().partition(spec)

        assertTrue(result.shared.clients.isEmpty())
    }

    @Test
    fun `GIVEN spec WHEN partition THEN each perClient has exactly 1 client`() {
        val model1 = ModelSpec.DataClassSpec(name = "Model1", properties = emptyList())
        val model2 = ModelSpec.DataClassSpec(name = "Model2", properties = emptyList())
        val client1 = ClientSpec(name = "Client1", operations = listOf(operationWithResponse("Model1")))
        val client2 = ClientSpec(name = "Client2", operations = listOf(operationWithResponse("Model2")))
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client1, client2),
                models = listOf(model1, model2),
            )

        val result = GenerationSpecPartitioner().partition(spec)

        assertEquals(2, result.perClient.size)
        result.perClient.forEach { perClient ->
            assertEquals(1, perClient.spec.clients.size)
        }
    }

    @Test
    fun `GIVEN spec with unused model WHEN partition THEN unused model goes to shared`() {
        val unusedModel = ModelSpec.DataClassSpec(name = "UnusedModel", properties = emptyList())
        val client1 = ClientSpec(name = "Client1", operations = listOf(noResponseOperation()))
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client1),
                models = listOf(unusedModel),
            )

        val result = GenerationSpecPartitioner().partition(spec)

        assertTrue(result.shared.models.any { it.name == "UnusedModel" })
        result.perClient.forEach { perClient ->
            assertTrue(perClient.spec.models.none { it.name == "UnusedModel" })
        }
    }
}
