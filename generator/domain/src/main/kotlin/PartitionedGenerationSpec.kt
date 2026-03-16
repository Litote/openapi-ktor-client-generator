package org.litote.openapi.ktor.client.generator.domain

public data class PartitionedGenerationSpec(
    /**
     * Shared model groups, keyed by the exact set of clients that use them (2+ clients per group).
     * Orphan models (used by 0 clients) are excluded from generation entirely.
     */
    val sharedGroups: List<SharedGroupSpec>,
    val perClient: List<PerClientGenerationSpec>,
) {
    /**
     * Backward-compatible view: union of all shared groups' models into a single [GenerationSpec].
     * Used by `SharedModelGranularity.SHARED_ALL`.
     */
    public val shared: GenerationSpec
        get() {
            val allModels = sharedGroups.flatMap { it.spec.models }
            val config =
                sharedGroups.firstOrNull()?.spec?.clientConfiguration
                    ?: GenerationSpec(
                        clientConfiguration = ClientConfigurationSpec("", emptyList(), emptyList()),
                        clients = emptyList(),
                        models = emptyList(),
                    ).clientConfiguration
            return GenerationSpec(clientConfiguration = config, clients = emptyList(), models = allModels)
        }
}

/**
 * A group of models shared by exactly the clients in [clientGroup] (always 2+ clients).
 */
public data class SharedGroupSpec(
    val clientGroup: Set<String>,
    val spec: GenerationSpec,
)

public data class PerClientGenerationSpec(
    val clientName: String,
    val spec: GenerationSpec,
)
