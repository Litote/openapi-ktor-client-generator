package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.application.GenerationSpecPartitioner
import org.litote.openapi.ktor.client.generator.domain.ClientConfigurationSpec
import org.litote.openapi.ktor.client.generator.domain.ClientSpec
import org.litote.openapi.ktor.client.generator.domain.DomainTypeSpec
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.ModelPropertySpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.domain.OperationSpec
import org.litote.openapi.ktor.client.generator.domain.ResponseEntrySpec
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
                    ResponseEntrySpec(
                        statusCodes = listOf(200),
                        bodyType = DomainTypeSpec.ModelReferenceSpec(modelName),
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
            responses = listOf(ResponseEntrySpec(statusCodes = listOf(200), bodyType = null, isSuccess = true)),
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
    fun `GIVEN spec with unused model WHEN partition THEN unused model is excluded from generation`() {
        val unusedModel = ModelSpec.DataClassSpec(name = "UnusedModel", properties = emptyList())
        val client1 = ClientSpec(name = "Client1", operations = listOf(noResponseOperation()))
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client1),
                models = listOf(unusedModel),
            )

        val result = GenerationSpecPartitioner().partition(spec)

        result.sharedGroups.forEach { group ->
            assertTrue(group.spec.models.none { it.name == "UnusedModel" }, "UnusedModel must not appear in any shared group")
        }
        result.perClient.forEach { perClient ->
            assertTrue(perClient.spec.models.none { it.name == "UnusedModel" }, "UnusedModel must not appear in any per-client module")
        }
    }

    @Test
    fun `GIVEN sealed subtype has List property WHEN partition THEN list element model inherits subtype clients`() {
        // TagHistory is only referenced via Tag.history: List<TagHistory>
        // Tag is a subtype of a sealed class SealedParent.
        // SealedParent is directly used by 2 clients.
        // Without the propagateTransitiveDeps fix, TagHistory would be classified as orphan (0 clients)
        // because propagateSealedClassUsage doesn't traverse properties of newly-marked subtypes.
        val tagHistory = ModelSpec.DataClassSpec(name = "TagHistory", properties = emptyList())
        val tag =
            ModelSpec.DataClassSpec(
                name = "Tag",
                sealedParentName = "SealedParent",
                properties =
                    listOf(
                        ModelPropertySpec(
                            originalName = "history",
                            camelCaseName = "history",
                            type = DomainTypeSpec.ListTypeSpec(DomainTypeSpec.ModelReferenceSpec("TagHistory")),
                            needsSerialName = false,
                        ),
                    ),
            )
        val sealedParent =
            ModelSpec.SealedClassSpec(
                name = "SealedParent",
                discriminatorPropertyName = null,
            )
        val client1 = ClientSpec(name = "Client1", operations = listOf(operationWithResponse("SealedParent")))
        val client2 = ClientSpec(name = "Client2", operations = listOf(operationWithResponse("SealedParent")))
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client1, client2),
                models = listOf(tagHistory, tag, sealedParent),
            )

        val result = GenerationSpecPartitioner().partition(spec)

        val tagHistoryInShared = result.sharedGroups.any { g -> g.spec.models.any { it.name == "TagHistory" } }
        assertTrue(
            tagHistoryInShared,
            "TagHistory must be in a shared group (transitively reachable via Tag which is a sealed subtype of SealedParent)",
        )
    }
}
