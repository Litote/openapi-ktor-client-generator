package org.litote.openapi.ktor.client.generator

import io.github.oshai.kotlinlogging.KotlinLogging
import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.adapter.renderer.ApiClientConfigurationGenerator
import org.litote.openapi.ktor.client.generator.adapter.renderer.ApiClientGenerator
import org.litote.openapi.ktor.client.generator.adapter.renderer.ApiModelGenerator
import org.litote.openapi.ktor.client.generator.application.GenerateCodeService
import org.litote.openapi.ktor.client.generator.application.GenerationSpecPartitioner
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.computeGroupDeps
import org.litote.openapi.ktor.client.generator.port.ApiClientRenderer
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationRenderer
import org.litote.openapi.ktor.client.generator.port.ApiModelRenderer
import org.litote.openapi.ktor.client.generator.shared.toSharedGroupDirName

/**
 * Main entry point for the API client generator.
 *
 * @param args Command-line arguments:
 *             - args[0]: path to the OpenAPI specification file
 *             - args[1]: output directory
 */
public fun main(vararg args: String) {
    generate(
        ApiGeneratorConfiguration(
            args.getOrNull(0)
                ?: error("please provide the path to the OpenAPI specification file as the first argument"),
            args.getOrNull(1) ?: error("please provide the path to the output directory as the second argument"),
        ),
    )
}

/**
 * A group of models shared by exactly the clients in [clientGroup].
 * Used by [SharedModelGranularity.SHARED_PER_GROUP] to identify per-group subprojects.
 *
 * @param clientGroup The exact set of client names that use these models.
 * @param modelNames The names of all model classes in this group.
 */
public data class SharedClientGroup(
    val clientGroup: Set<String>,
    val modelNames: Set<String>,
) {
    /** Directory name for the dedicated shared subproject: e.g. `{OrderClient, UserClient}` → `"shared-order-user"`. Long groups get a hash suffix. */
    val directoryName: String
        get() = clientGroup.toSharedGroupDirName()

    /** Package suffix derived from the directory name: e.g. `"shared-order-user"` → `"sharedOrderUser"`. */
    val packageSuffix: String
        get() =
            directoryName
                .split("-")
                .mapIndexed { i, part -> if (i == 0) part else part.replaceFirstChar { it.uppercase() } }
                .joinToString("")
}

/**
 * Parses an OpenAPI specification and returns the names of all generated clients.
 *
 * @param openApiFilePath Path to the OpenAPI specification file
 * @param splitGranularity Granularity used to group operations into clients
 * @return List of client names derived from the spec
 */
public fun parseClientNames(
    openApiFilePath: String,
    splitGranularity: SplitGranularity = SplitGranularity.BY_TAG,
): List<String> {
    val configuration =
        ApiGeneratorConfiguration(
            openApiFile = openApiFilePath,
            splitGranularity = splitGranularity,
        )
    val spec = OpenApiSpecificationParser(configuration).parse(configuration.operationFilter)
    return spec.clients.map { it.name }
}

/**
 * Parses an OpenAPI specification and returns all non-trivial shared model groups.
 * A group is non-trivial when it contains models shared by exactly 2 or more clients.
 *
 * Used by [SharedModelGranularity.SHARED_PER_GROUP] to generate dedicated subprojects.
 *
 * @param openApiFilePath Path to the OpenAPI specification file
 * @param splitGranularity Granularity used to group operations into clients
 * @return List of shared client groups (only groups with 2+ clients)
 */
public fun parseSharedClientGroups(
    openApiFilePath: String,
    splitGranularity: SplitGranularity = SplitGranularity.BY_TAG,
): List<SharedClientGroup> {
    val configuration =
        ApiGeneratorConfiguration(
            openApiFile = openApiFilePath,
            splitGranularity = splitGranularity,
        )
    val spec = OpenApiSpecificationParser(configuration).parse(configuration.operationFilter)
    val partitioned = GenerationSpecPartitioner().partition(spec)
    return partitioned.sharedGroups
        .filter { it.clientGroup.size >= 2 }
        .map { group ->
            SharedClientGroup(
                clientGroup = group.clientGroup,
                modelNames =
                    group.spec.models
                        .map { it.name }
                        .toSet(),
            )
        }
}

/**
 * Computes the direct Gradle compile-time dependency graph between per-group shared subprojects.
 *
 * Each per-group subproject may reference models from other per-group subprojects.
 * For example, if model `Foo` (in group {A,B}) has a property of type `Bar` (in group {A,B,C}),
 * then the {A,B} subproject must declare `api(project(":shared-a-b-c"))`.
 *
 * The returned map only contains groups with 2+ clients. Groups always form a DAG (no cycles).
 *
 * @param openApiFilePath Path to the OpenAPI specification file
 * @param splitGranularity Granularity used to group operations into clients
 * @return map from each [SharedClientGroup] to the set of [SharedClientGroup]s it directly depends on
 */
public fun computeSharedGroupDependencies(
    openApiFilePath: String,
    splitGranularity: SplitGranularity = SplitGranularity.BY_TAG,
): Map<SharedClientGroup, Set<SharedClientGroup>> {
    val configuration =
        ApiGeneratorConfiguration(
            openApiFile = openApiFilePath,
            splitGranularity = splitGranularity,
        )
    val spec = OpenApiSpecificationParser(configuration).parse(configuration.operationFilter)
    val partitioned = GenerationSpecPartitioner().partition(spec)
    val groups = partitioned.sharedGroups.filter { it.clientGroup.size >= 2 }

    val rawDeps = computeGroupDeps(groups)

    return rawDeps.entries.associate { (groupSpec, depSpecs) ->
        val groupModelNames =
            groupSpec.spec.models
                .map { it.name }
                .toSet()
        val group = SharedClientGroup(groupSpec.clientGroup, groupModelNames)
        val deps =
            depSpecs
                .map { dep ->
                    val depModelNames =
                        dep.spec.models
                            .map { it.name }
                            .toSet()
                    SharedClientGroup(dep.clientGroup, depModelNames)
                }.toSet()
        group to deps
    }
}

/**
 * Executes the generation of API client and data models based on an OpenAPI specification.
 *
 * Loads the OpenAPI file, generates the HTTP client using Ktor, and creates data model classes.
 *
 * @param configuration Custom generator configuration settings
 * @return [GenerationResult] indicating success with statistics or failure with error details
 */
public fun generate(configuration: ApiGeneratorConfiguration): GenerationResult =
    try {
        logger.debug { "Generating API for $configuration" }
        val parser = OpenApiSpecificationParser(configuration)
        val spec = parser.parse(configuration.operationFilter)

        val clientGen =
            ApiClientGenerator(configuration)
                .apply { configuration.modules.forEach { it.process(this) } }
        val clientRenderer =
            ApiClientRenderer { clientSpec ->
                val context = clientGen.buildClient(clientSpec)
                clientGen.writeFile(context)
            }

        val modelGen =
            ApiModelGenerator(
                configuration.generationModelPackage,
                configuration.outputDirectory,
                modelPackageOverrides = configuration.modelPackageOverrides,
                fallbackModelPackage = configuration.resolvedModelPackage,
            ).apply { configuration.modules.forEach { it.process(this) } }
        val modelRenderer =
            ApiModelRenderer { modelSpec ->
                val typeSpec = modelGen.buildModel(modelSpec)
                modelGen.writeFile(modelSpec.name, typeSpec)
            }

        val (activeSpec, activeConfigRenderer, activeClientRenderer) =
            if (!configuration.splitByClient) {
                val configRenderer =
                    ApiClientConfigurationGenerator(spec.clientConfiguration, configuration)
                        .apply { configuration.modules.forEach { it.process(this) } }
                Triple(spec, configRenderer as ApiConfigurationRenderer, clientRenderer)
            } else {
                val partitioned = GenerationSpecPartitioner().partition(spec)
                val targetName = configuration.targetClientName
                val targetSharedGroup = configuration.targetSharedGroup

                when {
                    targetName != null -> {
                        // Per-client mode: generate one client + its private models
                        val perClientSpec =
                            partitioned.perClient.find { it.clientName == targetName }
                                ?: return GenerationResult.Failure(
                                    IllegalArgumentException("Client '$targetName' not found"),
                                    "Client '$targetName' not found in spec ${configuration.openApiFile}",
                                )
                        val noopConfigRenderer = ApiConfigurationRenderer { }
                        Triple(perClientSpec.spec, noopConfigRenderer, clientRenderer)
                    }

                    targetSharedGroup != null -> {
                        // Specific shared group mode (SHARED_PER_GROUP): generate models for that group only
                        val groupSpec =
                            partitioned.sharedGroups.find { it.clientGroup == targetSharedGroup }
                                ?: return GenerationResult.Failure(
                                    IllegalArgumentException("Shared group '$targetSharedGroup' not found"),
                                    "Shared group '$targetSharedGroup' not found in spec ${configuration.openApiFile}",
                                )
                        val noopConfigRenderer = ApiConfigurationRenderer { }
                        val noopClientRenderer = ApiClientRenderer { }
                        Triple(groupSpec.spec, noopConfigRenderer, noopClientRenderer)
                    }

                    else -> {
                        // Shared mode: generate ClientConfiguration + shared models
                        val sharedSpec = resolveSharedSpec(spec, partitioned, configuration)
                        val configRenderer =
                            ApiClientConfigurationGenerator(spec.clientConfiguration, configuration)
                                .apply { configuration.modules.forEach { it.process(this) } }
                        val noopClientRenderer = ApiClientRenderer { }
                        Triple(sharedSpec, configRenderer as ApiConfigurationRenderer, noopClientRenderer)
                    }
                }
            }

        val (clientsGenerated, modelsGenerated) =
            GenerateCodeService(
                activeConfigRenderer,
                activeClientRenderer,
                modelRenderer,
            ).generate(activeSpec)
        val result = GenerationResult.Success(clientsGenerated, modelsGenerated)
        logger.info { "Generation completed: ${result.clientsGenerated} clients, ${result.modelsGenerated} models" }
        result
    } catch (e: Throwable) {
        logger.error(e) { "Error while generating API for $configuration" }
        GenerationResult.Failure(e, "Failed to generate API for ${configuration.openApiFile}: ${e.message}")
    }

/**
 * Resolves the shared [GenerationSpec] based on [ApiGeneratorConfiguration.sharedModelGranularity].
 *
 * - [SharedModelGranularity.SHARED_ALL]: merges all shared groups into one spec (backward-compatible).
 * - [SharedModelGranularity.SHARED_PER_GROUP]: only includes orphan models (used by 0 clients).
 */
private fun resolveSharedSpec(
    fullSpec: GenerationSpec,
    partitioned: org.litote.openapi.ktor.client.generator.domain.PartitionedGenerationSpec,
    configuration: ApiGeneratorConfiguration,
): GenerationSpec =
    when (configuration.sharedModelGranularity) {
        SharedModelGranularity.SHARED_ALL -> {
            partitioned.shared
        }

        SharedModelGranularity.SHARED_PER_GROUP -> {
            val orphanModels =
                partitioned.sharedGroups
                    .firstOrNull { it.clientGroup.isEmpty() }
                    ?.spec
                    ?.models
                    ?: emptyList()
            GenerationSpec(
                clientConfiguration = fullSpec.clientConfiguration,
                clients = emptyList(),
                models = orphanModels,
            )
        }
    }

private val logger = KotlinLogging.logger {}
