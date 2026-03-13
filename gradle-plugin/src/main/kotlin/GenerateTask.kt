package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.litote.openapi.ktor.client.generator.ApiGeneratorConfiguration
import org.litote.openapi.ktor.client.generator.ApiGeneratorModule.Companion.getModule
import org.litote.openapi.ktor.client.generator.GenerationResult
import org.litote.openapi.ktor.client.generator.SharedModelGranularity
import org.litote.openapi.ktor.client.generator.SplitGranularity
import org.litote.openapi.ktor.client.generator.generate
import org.litote.openapi.ktor.client.generator.parseSharedClientGroups

@CacheableTask
public abstract class GenerateTask : DefaultTask() {
    /**
     * OpenAPI3 specification file (json).
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val openApiFile: RegularFileProperty

    /**
     * Where generated code will be written.
     */
    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    /**
     * Base package of generated classes.
     */
    @get:Input
    public abstract val basePackage: Property<String>

    /**
     * List of allowed paths to generate code for. If empty, all paths will be generated.
     */
    @get:Input
    public abstract val allowedPaths: SetProperty<String>

    /**
     * List of allowed additional modules used to generate code.
     */
    @get:Input
    public abstract val modulesIds: SetProperty<String>

    @get:Input
    public abstract val skip: Property<Boolean>

    @get:Input
    public abstract val splitByClient: Property<Boolean>

    @get:Input
    @get:Optional
    public abstract val targetClientName: Property<String>

    @get:Input
    @get:Optional
    public abstract val sharedBasePackage: Property<String>

    /** Accepted values: `BY_TAG` (default), `BY_TAG_AND_PATH`, `BY_TAG_AND_OPERATION`. */
    @get:Input
    public abstract val splitGranularity: Property<String>

    /** Accepted values: `SHARED_ALL` (default), `SHARED_PER_GROUP`. */
    @get:Input
    public abstract val sharedModelGranularity: Property<String>

    /**
     * Comma-separated sorted client names identifying a specific shared group to generate.
     * E.g. `"OrderClient,UserClient"`.
     */
    @get:Input
    @get:Optional
    public abstract val targetSharedGroup: Property<String>

    /**
     * Map of shared group identifier → base package of that group's subproject.
     * Group identifier = comma-separated sorted client names, e.g. `"OrderClient,UserClient"`.
     * Used to resolve [modelPackageOverrides] at build time.
     */
    @get:Input
    public abstract val additionalSharedGroupPackages: MapProperty<String, String>

    @TaskAction
    public fun generate() {
        val allowedPaths = allowedPaths.get()
        val openApiFilePath = openApiFile.get().asFile.absolutePath
        val splitGranularityValue = SplitGranularity.valueOf(splitGranularity.get())
        val sharedModelGranularityValue = SharedModelGranularity.valueOf(sharedModelGranularity.get())
        val targetSharedGroupValue = targetSharedGroup.orNull?.split(",")?.toSet()

        // Resolve modelPackageOverrides from additionalSharedGroupPackages if needed.
        val additionalGroups = additionalSharedGroupPackages.get()
        val modelPackageOverrides: Map<String, String> =
            if (additionalGroups.isEmpty()) {
                emptyMap()
            } else {
                val sharedGroups = parseSharedClientGroups(openApiFilePath, splitGranularityValue)
                additionalGroups
                    .flatMap { (groupKey, basePackage) ->
                        val clientGroup = groupKey.split(",").toSet()
                        val matchingGroup = sharedGroups.firstOrNull { it.clientGroup == clientGroup }
                        matchingGroup?.modelNames?.map { modelName -> modelName to "$basePackage.model" } ?: emptyList()
                    }.toMap()
            }

        val config =
            ApiGeneratorConfiguration(
                openApiFile = openApiFilePath,
                outputDirectory = outputDirectory.get().asFile.absolutePath,
                basePackage = basePackage.get(),
                operationFilter = { operation ->
                    val path = operation.path
                    allowedPaths.isEmpty() || allowedPaths.contains(path)
                },
                modules =
                    modulesIds
                        .get()
                        .map { moduleId -> checkNotNull(getModule(moduleId)) { "Module identifier $moduleId not found" } },
                splitByClient = splitByClient.get(),
                targetClientName = targetClientName.orNull,
                sharedBasePackage = sharedBasePackage.orNull,
                splitGranularity = splitGranularityValue,
                sharedModelGranularity = sharedModelGranularityValue,
                targetSharedGroup = targetSharedGroupValue,
                modelPackageOverrides = modelPackageOverrides,
            )

        if (skip.get() == true) {
            logger.info("skip generation for ${config.openApiFile}")
        } else {
            when (val result = generate(config)) {
                is GenerationResult.Success -> {
                    logger.info("Generated ${result.clientsGenerated} clients and ${result.modelsGenerated} models")
                }

                is GenerationResult.Failure -> {
                    throw org.gradle.api.GradleException(result.message, result.error)
                }
            }
        }
    }
}
