package org.litote.openapi.ktor.client.generator.application

import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.PartitionedGenerationSpec
import org.litote.openapi.ktor.client.generator.domain.PerClientGenerationSpec
import org.litote.openapi.ktor.client.generator.domain.SharedGroupSpec
import org.litote.openapi.ktor.client.generator.domain.analyzeModelUsage

public class GenerationSpecPartitioner {
    /**
     * Partitions [spec] into shared groups and per-client specs.
     *
     * Shared groups are keyed by the exact set of clients that use the models in that group.
     * Only models reachable from at least one operation are included — orphan models (not
     * transitively reachable from any operation) are silently excluded because they would
     * never be used by any generated client code.
     *
     * - A set of 2+ clients represents models shared by exactly those clients.
     *
     * Private models (used by exactly 1 client) are placed in the corresponding [PerClientGenerationSpec].
     */
    public fun partition(spec: GenerationSpec): PartitionedGenerationSpec {
        val usage = analyzeModelUsage(spec)

        val sharedGroups =
            spec.models
                .groupBy { model -> usage[model.name] ?: emptySet() }
                .filter { (clientGroup, _) -> clientGroup.size >= 2 }
                .map { (clientGroup, models) ->
                    SharedGroupSpec(
                        clientGroup = clientGroup,
                        spec =
                            GenerationSpec(
                                clientConfiguration = spec.clientConfiguration,
                                clients = emptyList(),
                                models = models,
                            ),
                    )
                }

        val perClientSpecs =
            spec.clients.map { client ->
                val privateModels =
                    spec.models.filter { model ->
                        usage[model.name] == setOf(client.name)
                    }
                PerClientGenerationSpec(
                    clientName = client.name,
                    spec =
                        GenerationSpec(
                            clientConfiguration = spec.clientConfiguration,
                            clients = listOf(client),
                            models = privateModels,
                        ),
                )
            }

        return PartitionedGenerationSpec(sharedGroups = sharedGroups, perClient = perClientSpecs)
    }
}
