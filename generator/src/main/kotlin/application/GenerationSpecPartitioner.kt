package org.litote.openapi.ktor.client.generator.application

import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.PartitionedGenerationSpec
import org.litote.openapi.ktor.client.generator.domain.PerClientGenerationSpec
import org.litote.openapi.ktor.client.generator.domain.analyzeModelUsage

internal class GenerationSpecPartitioner {
    fun partition(spec: GenerationSpec): PartitionedGenerationSpec {
        val usage = analyzeModelUsage(spec)

        val sharedModels =
            spec.models.filter { model ->
                val clientsUsing = usage[model.name] ?: emptySet()
                clientsUsing.size != 1
            }

        val sharedSpec =
            GenerationSpec(
                clientConfiguration = spec.clientConfiguration,
                clients = emptyList(),
                models = sharedModels,
            )

        val perClientSpecs =
            spec.clients.map { client ->
                val privateModels =
                    spec.models.filter { model ->
                        val clientsUsing = usage[model.name] ?: emptySet()
                        clientsUsing == setOf(client.name)
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

        return PartitionedGenerationSpec(shared = sharedSpec, perClient = perClientSpecs)
    }
}
