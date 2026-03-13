package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.domain.ClientConfigurationSpec
import org.litote.openapi.ktor.client.generator.domain.ClientSpec
import org.litote.openapi.ktor.client.generator.domain.DomainType
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.ModelProperty
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.domain.OperationParameter
import org.litote.openapi.ktor.client.generator.domain.OperationSpec
import org.litote.openapi.ktor.client.generator.domain.ParameterLocation
import org.litote.openapi.ktor.client.generator.domain.ResponseEntry
import org.litote.openapi.ktor.client.generator.domain.analyzeModelUsage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModelUsageAnalyzerTest {
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

    @Test
    fun `GIVEN spec with 2 clients sharing a model WHEN analyzeModelUsage THEN model appears in both clients sets`() {
        val sharedModel =
            ModelSpec.DataClassSpec(
                name = "SharedModel",
                properties = emptyList(),
            )
        val client1 =
            ClientSpec(
                name = "Client1",
                operations = listOf(operationWithResponse("SharedModel")),
            )
        val client2 =
            ClientSpec(
                name = "Client2",
                operations = listOf(operationWithResponse("SharedModel")),
            )
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client1, client2),
                models = listOf(sharedModel),
            )

        val usage = analyzeModelUsage(spec)

        assertEquals(setOf("Client1", "Client2"), usage["SharedModel"])
    }

    @Test
    fun `GIVEN spec with model used only by 1 client WHEN analyzeModelUsage THEN model is private to that client`() {
        val privateModel = ModelSpec.DataClassSpec(name = "PrivateModel", properties = emptyList())
        val client1 =
            ClientSpec(
                name = "Client1",
                operations = listOf(operationWithResponse("PrivateModel")),
            )
        val client2 =
            ClientSpec(
                name = "Client2",
                operations =
                    listOf(
                        OperationSpec(
                            name = "other",
                            path = "/other",
                            method = "GET",
                            parameters = emptyList(),
                            responses = listOf(ResponseEntry(statusCodes = listOf(200), bodyType = null, isSuccess = true)),
                        ),
                    ),
            )
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client1, client2),
                models = listOf(privateModel),
            )

        val usage = analyzeModelUsage(spec)

        assertEquals(setOf("Client1"), usage["PrivateModel"])
    }

    @Test
    fun `GIVEN spec with unused model WHEN analyzeModelUsage THEN model has empty client set`() {
        val unusedModel = ModelSpec.DataClassSpec(name = "UnusedModel", properties = emptyList())
        val client =
            ClientSpec(
                name = "Client1",
                operations =
                    listOf(
                        OperationSpec(
                            name = "ping",
                            path = "/ping",
                            method = "GET",
                            parameters = emptyList(),
                            responses = listOf(ResponseEntry(statusCodes = listOf(200), bodyType = null, isSuccess = true)),
                        ),
                    ),
            )
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client),
                models = listOf(unusedModel),
            )

        val usage = analyzeModelUsage(spec)

        assertTrue(usage["UnusedModel"]?.isEmpty() == true)
    }

    @Test
    fun `GIVEN spec with sealed class used by client WHEN analyzeModelUsage THEN subtypes are also attributed to that client`() {
        val sealedClass = ModelSpec.SealedClassSpec(name = "Status", discriminatorPropertyName = "type")
        val subtype1 =
            ModelSpec.DataClassSpec(
                name = "ActiveStatus",
                properties = emptyList(),
                sealedParentName = "Status",
            )
        val subtype2 =
            ModelSpec.DataClassSpec(
                name = "InactiveStatus",
                properties = emptyList(),
                sealedParentName = "Status",
            )
        val client =
            ClientSpec(
                name = "Client1",
                operations = listOf(operationWithResponse("Status")),
            )
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client),
                models = listOf(sealedClass, subtype1, subtype2),
            )

        val usage = analyzeModelUsage(spec)

        assertEquals(setOf("Client1"), usage["Status"])
        assertEquals(setOf("Client1"), usage["ActiveStatus"])
        assertEquals(setOf("Client1"), usage["InactiveStatus"])
    }

    @Test
    fun `GIVEN spec with model having transitive deps WHEN analyzeModelUsage THEN transitive deps are included`() {
        val leaf = ModelSpec.DataClassSpec(name = "LeafModel", properties = emptyList())
        val middle =
            ModelSpec.DataClassSpec(
                name = "MiddleModel",
                properties =
                    listOf(
                        ModelProperty(
                            originalName = "leaf",
                            camelCaseName = "leaf",
                            type = DomainType.ModelReference("LeafModel"),
                            needsSerialName = false,
                        ),
                    ),
            )
        val top =
            ModelSpec.DataClassSpec(
                name = "TopModel",
                properties =
                    listOf(
                        ModelProperty(
                            originalName = "middle",
                            camelCaseName = "middle",
                            type = DomainType.ModelReference("MiddleModel"),
                            needsSerialName = false,
                        ),
                    ),
            )
        val client =
            ClientSpec(
                name = "Client1",
                operations =
                    listOf(
                        OperationSpec(
                            name = "getTop",
                            path = "/top",
                            method = "GET",
                            parameters =
                                listOf(
                                    OperationParameter(
                                        originalName = "topParam",
                                        camelCaseName = "topParam",
                                        type = DomainType.ModelReference("TopModel"),
                                        location = ParameterLocation.QUERY,
                                        required = false,
                                    ),
                                ),
                            responses = listOf(ResponseEntry(statusCodes = listOf(200), bodyType = null, isSuccess = true)),
                        ),
                    ),
            )
        val spec =
            GenerationSpec(
                clientConfiguration = minimalConfig(),
                clients = listOf(client),
                models = listOf(top, middle, leaf),
            )

        val usage = analyzeModelUsage(spec)

        assertEquals(setOf("Client1"), usage["TopModel"])
        assertEquals(setOf("Client1"), usage["MiddleModel"])
        assertEquals(setOf("Client1"), usage["LeafModel"])
    }
}
