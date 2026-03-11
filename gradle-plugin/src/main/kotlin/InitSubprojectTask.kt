package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Scaffolding task — generates project structure on demand")
public abstract class InitSubprojectTask : DefaultTask() {
    /** Path to the OpenAPI spec file (passed via -PopenApiFile=...). */
    @get:Input
    @get:Optional
    public abstract val openApiFilePath: Property<String>

    /** Name of the subproject directory to create (passed via -PsubprojectName=...). */
    @get:Input
    @get:Optional
    public abstract val subprojectName: Property<String>

    @get:Internal
    public abstract val rootDirectory: DirectoryProperty

    /** Kotlin version used in the generated build.gradle.kts. Defaults to [DEFAULT_KOTLIN_VERSION]. */
    @get:Input
    public abstract val kotlinVersion: Property<String>

    /** Ktor version used in the generated build.gradle.kts. Defaults to [DEFAULT_KTOR_VERSION]. */
    @get:Input
    public abstract val ktorVersion: Property<String>

    /** kotlinx.coroutines version used in the generated build.gradle.kts. Defaults to [DEFAULT_COROUTINES_VERSION]. */
    @get:Input
    public abstract val coroutinesVersion: Property<String>

    /** kotlinx.serialization version used in the generated build.gradle.kts. Defaults to [DEFAULT_SERIALIZATION_VERSION]. */
    @get:Input
    public abstract val serializationVersion: Property<String>

    @TaskAction
    public fun initSubproject() {
        val openApiFilePathValue =
            openApiFilePath.orNull
                ?: throw GradleException("Missing required parameter: -PopenApiFile=<path to OpenAPI spec file>")

        val openApiSourceFile = resolveOpenApiFile(openApiFilePathValue)
        val subprojectNameValue = subprojectName.orNull ?: openApiSourceFile.nameWithoutExtension
        val subprojectDir = rootDirectory.get().asFile.resolve(subprojectNameValue)

        scaffoldSubproject(subprojectDir, openApiSourceFile, subprojectNameValue)

        logger.lifecycle("Subproject '$subprojectNameValue' created at ${subprojectDir.absolutePath}")
        logger.lifecycle("Don't forget to add 'include(\"$subprojectNameValue\")' to your settings.gradle.kts")
    }

    private fun resolveOpenApiFile(path: String): File {
        val file = File(path).let { if (it.isAbsolute) it else rootDirectory.get().asFile.resolve(path) }
        if (!file.exists()) throw GradleException("OpenAPI file not found: ${file.absolutePath}")
        return file
    }

    private fun scaffoldSubproject(
        subprojectDir: File,
        openApiSourceFile: File,
        subprojectNameValue: String,
    ) {
        val openApiDestDir = subprojectDir.resolve("src/main/openapi")
        openApiDestDir.mkdirs()

        val openApiFileName = openApiSourceFile.name
        openApiSourceFile.copyTo(openApiDestDir.resolve(openApiFileName), overwrite = true)

        val generatorName = openApiSourceFile.nameWithoutExtension
        subprojectDir.resolve("build.gradle.kts").writeText(
            buildGradleKtsContent(
                generatorName = generatorName,
                openApiFileName = openApiFileName,
                kotlinVersion = kotlinVersion.get(),
                ktorVersion = ktorVersion.get(),
                coroutinesVersion = coroutinesVersion.get(),
                serializationVersion = serializationVersion.get(),
            ),
        )

        logger.info("Scaffolded subproject '$subprojectNameValue'")
    }

    internal companion object {
        internal const val PLUGIN_ID = "org.litote.openapi.ktor.client.generator.gradle"

        internal fun buildGradleKtsContent(
            generatorName: String,
            openApiFileName: String,
            kotlinVersion: String,
            ktorVersion: String,
            coroutinesVersion: String,
            serializationVersion: String,
        ): String =
            """
            plugins {
                kotlin("jvm") version "$kotlinVersion"
                kotlin("plugin.serialization") version "$kotlinVersion"
                id("$PLUGIN_ID") version "$PLUGIN_VERSION"
            }

            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
            }

            apiClientGenerator {
                generators {
                    create("$generatorName") {
                        openApiFile = file("src/main/openapi/$openApiFileName")
                    }
                }
            }
            """.trimIndent()
    }
}
