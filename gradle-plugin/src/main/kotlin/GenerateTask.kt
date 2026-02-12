package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.litote.openapi.ktor.client.generator.ApiGeneratorConfiguration
import org.litote.openapi.ktor.client.generator.ApiGeneratorModule.Companion.getModule
import org.litote.openapi.ktor.client.generator.generate

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

    @TaskAction
    public fun generate() {
        val allowedPaths = allowedPaths.get()
        val config = ApiGeneratorConfiguration(
            openApiFile = openApiFile.get().asFile.absolutePath,
            outputDirectory = outputDirectory.get().asFile.absolutePath,
            basePackage = basePackage.get(),
            operationFilter = { operation ->
                val path = operation.path
                allowedPaths.isEmpty() || allowedPaths.contains(path)
            },
            modules = modulesIds.get()
                .map { moduleId -> checkNotNull(getModule(moduleId)) { "Module identifier $moduleId not found" } }
        )

        generate(config)
    }
}