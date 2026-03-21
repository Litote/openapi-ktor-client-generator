package org.litote.openapi.ktor.client.generator.domain

public fun analyzeModelUsage(spec: GenerationSpec): Map<String, Set<String>> {
    val allModels = spec.models.associateBy { it.name }
    val usage: MutableMap<String, MutableSet<String>> =
        spec.models
            .associate { it.name to mutableSetOf<String>() }
            .toMutableMap()

    spec.clients.forEach { client ->
        val directRefs = collectDirectRefs(client)
        val allRefs = resolveTransitiveDeps(directRefs, allModels)
        allRefs.forEach { modelName ->
            usage.getOrPut(modelName) { mutableSetOf() }.add(client.name)
        }
    }

    propagateSealedClassUsage(usage, allModels)

    // After propagateSealedClassUsage, sealed class subtypes may have gained new clients without
    // their own transitive dependencies being resolved. Run a fixpoint pass to propagate client
    // sets from every model to every model it references, until stable.
    propagateTransitiveDeps(usage, allModels)

    return usage
}

private fun collectModelRefs(type: DomainTypeSpec): Set<String> =
    when (type) {
        is DomainTypeSpec.ModelReferenceSpec -> setOf(type.name)

        is DomainTypeSpec.ListTypeSpec -> collectModelRefs(type.element)

        is DomainTypeSpec.SetTypeSpec -> collectModelRefs(type.element)

        is DomainTypeSpec.MapTypeSpec -> collectModelRefs(type.value)

        is DomainTypeSpec.PrimitiveSpec,
        is DomainTypeSpec.InlineTypeSpec,
        is DomainTypeSpec.JsonTypeSpec,
        -> emptySet()
    }

public fun collectModelRefs(model: ModelSpec): Set<String> =
    when (model) {
        is ModelSpec.DataClassSpec -> {
            val refs = mutableSetOf<String>()
            model.sealedParentName?.let { refs.add(it) }
            refs.addAll(model.interfaceParentNames)
            model.properties.forEach { prop ->
                refs.addAll(collectModelRefs(prop.type))
                prop.nestedModels.forEach { nested -> refs.addAll(collectModelRefs(nested)) }
            }
            refs
        }

        is ModelSpec.ObjectSpec -> {
            val refs = mutableSetOf<String>()
            model.sealedParentName?.let { refs.add(it) }
            refs
        }

        is ModelSpec.InterfaceSpec -> {
            val refs = mutableSetOf<String>()
            model.properties.forEach { prop ->
                refs.addAll(collectModelRefs(prop.type))
                prop.nestedModels.forEach { nested -> refs.addAll(collectModelRefs(nested)) }
            }
            refs
        }

        is ModelSpec.EnumSpec,
        is ModelSpec.SealedClassSpec,
        is ModelSpec.AliasSpec,
        -> {
            emptySet()
        }
    }

private fun collectDirectRefs(client: ClientSpec): Set<String> {
    val refs = mutableSetOf<String>()
    client.operations.forEach { op ->
        op.parameters.forEach { param -> refs.addAll(collectModelRefs(param.type)) }
        op.requestBody?.let { body -> refs.addAll(collectModelRefs(body.type)) }
        op.responses.forEach { response -> response.bodyType?.let { refs.addAll(collectModelRefs(it)) } }
    }
    return refs
}

private fun resolveTransitiveDeps(
    directRefs: Set<String>,
    allModels: Map<String, ModelSpec>,
): Set<String> {
    val resolved = mutableSetOf<String>()
    val frontier = ArrayDeque(directRefs.toList())
    while (frontier.isNotEmpty()) {
        val name = frontier.removeFirst()
        if (resolved.add(name)) {
            val model = allModels[name] ?: continue
            collectModelRefs(model).forEach { dep ->
                if (dep !in resolved) frontier.add(dep)
            }
        }
    }
    return resolved
}

private fun propagateSealedClassUsage(
    usage: MutableMap<String, MutableSet<String>>,
    allModels: Map<String, ModelSpec>,
) {
    allModels.values.filterIsInstance<ModelSpec.SealedClassSpec>().forEach { sealedClass ->
        val sealedClients: Set<String> = usage[sealedClass.name] ?: return@forEach
        allModels.values.forEach { model ->
            val parentName =
                when (model) {
                    is ModelSpec.DataClassSpec -> model.sealedParentName
                    is ModelSpec.ObjectSpec -> model.sealedParentName
                    else -> null
                }
            if (parentName == sealedClass.name) {
                usage.getOrPut(model.name) { mutableSetOf() }.addAll(sealedClients)
            }
        }
    }
}

/**
 * Propagates client sets transitively across model references until stable (fixpoint).
 *
 * This is needed because [propagateSealedClassUsage] can assign new clients to sealed-class
 * subtypes AFTER [resolveTransitiveDeps] has already run per client. Without this pass,
 * properties of newly-marked subtypes (e.g. `Tag.history: List<TagHistory>`) would not
 * propagate their clients to referenced models (e.g. `TagHistory`), leaving those models
 * incorrectly classified as orphans.
 */
private fun propagateTransitiveDeps(
    usage: MutableMap<String, MutableSet<String>>,
    allModels: Map<String, ModelSpec>,
) {
    var changed = true
    while (changed) {
        changed = false
        allModels.values.forEach { model ->
            val clients: Set<String> = usage[model.name] ?: return@forEach
            if (clients.isEmpty()) return@forEach
            collectModelRefs(model).forEach { dep ->
                val depClients = usage.getOrPut(dep) { mutableSetOf() }
                if (depClients.addAll(clients)) changed = true
            }
        }
    }
}

/**
 * Computes the direct Gradle compile dependencies between per-group shared subprojects.
 *
 * For each group in [sharedGroups], finds which OTHER groups contain models that are
 * directly referenced by models in this group. Groups form a DAG (no cycles) because
 * transitive analysis ensures a model referenced by a group's model will have a SUPERSET
 * client-group, never a strict subset (which would create a cycle).
 *
 * @return map from each SharedGroupSpec to the set of SharedGroupSpecs it depends on
 */
public fun computeGroupDeps(sharedGroups: List<SharedGroupSpec>): Map<SharedGroupSpec, Set<SharedGroupSpec>> {
    val modelToGroup: Map<String, SharedGroupSpec> =
        sharedGroups
            .flatMap { group -> group.spec.models.map { model -> model.name to group } }
            .toMap()

    return sharedGroups.associateWith { group ->
        group.spec.models
            .flatMap { model -> collectModelRefs(model) }
            .mapNotNull { refName ->
                val depGroup = modelToGroup[refName]
                if (depGroup != null && depGroup != group) depGroup else null
            }.toSet()
    }
}
