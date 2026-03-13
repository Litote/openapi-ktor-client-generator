package org.litote.openapi.ktor.client.generator.domain

internal data class PartitionedGenerationSpec(
    val shared: GenerationSpec,
    val perClient: List<PerClientGenerationSpec>,
)

internal data class PerClientGenerationSpec(
    val clientName: String,
    val spec: GenerationSpec,
)
