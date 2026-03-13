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
import org.litote.openapi.ktor.client.generator.SplitGranularity
import org.litote.openapi.ktor.client.generator.parseClientNames
import org.litote.openapi.ktor.client.generator.parseSharedClientGroups
import java.io.File

@DisableCachingByDefault(because = "Project generation task — generates project structure on demand")
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

    /** When true, generate a multi-module subproject with one module per client (passed via -PsplitByClient=true). */
    @get:Input
    @get:Optional
    public abstract val splitByClient: Property<Boolean>

    /** Base package for generated code (passed via -PbasePackage=...). Falls back to a value derived from the spec name. */
    @get:Input
    @get:Optional
    public abstract val basePackage: Property<String>

    /**
     * Custom template that replaces the auto-generated `plugins {}` and `dependencies {}` blocks
     * in all generated build.gradle.kts files. See [InitSubprojectExtension.buildScriptTemplate].
     */
    @get:Input
    @get:Optional
    public abstract val buildScriptTemplate: Property<String>

    /**
     * Extra configuration lines appended inside the `create("...") { }` generator block of all
     * generated build.gradle.kts files. See [InitSubprojectExtension.generatorConfigExtra].
     */
    @get:Input
    @get:Optional
    public abstract val generatorConfigExtra: Property<String>

    /**
     * Granularity used to group operations into client classes.
     * Accepted values: `BY_TAG` (default), `BY_TAG_AND_PATH`, `BY_TAG_AND_OPERATION`.
     * Passed via -PsplitGranularity=...
     */
    @get:Input
    @get:Optional
    public abstract val splitGranularity: Property<String>

    /**
     * How shared models are distributed into subprojects.
     * Accepted values: `SHARED_ALL` (default), `SHARED_PER_GROUP`.
     * Passed via -PsharedModelGranularity=...
     */
    @get:Input
    @get:Optional
    public abstract val sharedModelGranularity: Property<String>

    @TaskAction
    public fun initSubproject() {
        val openApiFilePathValue =
            openApiFilePath.orNull
                ?: throw GradleException("Missing required parameter: -PopenApiFile=<path to OpenAPI spec file>")

        val openApiSourceFile = resolveOpenApiFile(openApiFilePathValue)
        val subprojectNameValue = subprojectName.orNull ?: openApiSourceFile.nameWithoutExtension
        val subprojectDir = rootDirectory.get().asFile.resolve(subprojectNameValue)
        val granularity = SplitGranularity.valueOf(splitGranularity.getOrElse("BY_TAG"))
        val sharedModelGranularityValue = sharedModelGranularity.getOrElse("SHARED_ALL")

        if (splitByClient.getOrElse(false)) {
            if (sharedModelGranularityValue == "SHARED_PER_GROUP") {
                initMultiModuleSubprojectPerGroup(openApiSourceFile, subprojectNameValue, granularity)
            } else {
                initMultiModuleSubproject(openApiSourceFile, subprojectNameValue, granularity)
            }
        } else {
            generateSubproject(subprojectDir, openApiSourceFile, subprojectNameValue)
            logger.lifecycle("Subproject '$subprojectNameValue' created at ${subprojectDir.absolutePath}")
            logger.lifecycle("Don't forget to add 'include(\"$subprojectNameValue\")' to your settings.gradle.kts")
        }
    }

    private fun resolveOpenApiFile(path: String): File {
        val file = File(path).let { if (it.isAbsolute) it else rootDirectory.get().asFile.resolve(path) }
        if (!file.exists()) throw GradleException("OpenAPI file not found: ${file.absolutePath}")
        return file
    }

    private fun generateSubproject(
        subprojectDir: File,
        openApiSourceFile: File,
        subprojectNameValue: String,
    ) {
        val openApiDestDir = subprojectDir.resolve("src/main/openapi")
        openApiDestDir.mkdirs()

        val openApiFileName = openApiSourceFile.name
        val openApiDestFile = openApiDestDir.resolve(openApiFileName)
        if (openApiSourceFile.canonicalPath != openApiDestFile.canonicalPath) {
            openApiSourceFile.copyTo(openApiDestFile, overwrite = true)
        }

        val generatorName = openApiSourceFile.nameWithoutExtension
        subprojectDir.resolve("build.gradle.kts").writeText(
            buildGradleKtsContent(
                generatorName = generatorName,
                openApiFileName = openApiFileName,
                kotlinVersion = kotlinVersion.get(),
                ktorVersion = ktorVersion.get(),
                coroutinesVersion = coroutinesVersion.get(),
                serializationVersion = serializationVersion.get(),
                buildScriptTemplate = buildScriptTemplate.orNull,
                generatorConfigExtra = generatorConfigExtra.orNull,
            ),
        )

        logger.info("Generated subproject '$subprojectNameValue'")
    }

    private fun initMultiModuleSubproject(
        openApiSourceFile: File,
        subprojectNameValue: String,
        granularity: SplitGranularity,
    ) {
        val clientNames = parseClientNames(openApiSourceFile.absolutePath, granularity)
        val projectDir = rootDirectory.get().asFile

        val openApiDestDir = projectDir.resolve("src/main/openapi")
        openApiDestDir.mkdirs()
        val openApiFileName = openApiSourceFile.name
        val openApiDestFile = openApiDestDir.resolve(openApiFileName)
        if (openApiSourceFile.canonicalPath != openApiDestFile.canonicalPath) {
            openApiSourceFile.copyTo(openApiDestFile, overwrite = true)
        }

        val specRelativePath = "../src/main/openapi/$openApiFileName"
        val specNameWithoutExt = openApiSourceFile.nameWithoutExtension
        val basePackage =
            basePackage.orNull
                ?: "org.example.${specNameWithoutExt.lowercase().filter { it.isLetterOrDigit() }}"

        val sharedDir = projectDir.resolve("shared")
        sharedDir.mkdirs()
        sharedDir.resolve("build.gradle.kts").writeText(
            buildSharedGradleKtsContent(
                specNameWithoutExt = specNameWithoutExt,
                specRelativePath = specRelativePath,
                basePackage = basePackage,
                kotlinVersion = kotlinVersion.get(),
                ktorVersion = ktorVersion.get(),
                coroutinesVersion = coroutinesVersion.get(),
                serializationVersion = serializationVersion.get(),
                buildScriptTemplate = buildScriptTemplate.orNull,
                generatorConfigExtra = generatorConfigExtra.orNull,
                splitGranularity = granularity,
            ),
        )

        clientNames.forEach { clientName ->
            val subprojectDirName = clientName.toKebabCase()
            val clientDir = projectDir.resolve(subprojectDirName)
            clientDir.mkdirs()
            clientDir.resolve("build.gradle.kts").writeText(
                buildClientGradleKtsContent(
                    clientName = clientName,
                    subprojectDirName = subprojectDirName,
                    specNameWithoutExt = specNameWithoutExt,
                    specRelativePath = specRelativePath,
                    basePackage = basePackage,
                    kotlinVersion = kotlinVersion.get(),
                    ktorVersion = ktorVersion.get(),
                    coroutinesVersion = coroutinesVersion.get(),
                    serializationVersion = serializationVersion.get(),
                    buildScriptTemplate = buildScriptTemplate.orNull,
                    generatorConfigExtra = generatorConfigExtra.orNull,
                    splitGranularity = granularity,
                ),
            )
        }

        val subprojectDirNames = clientNames.map { it.toKebabCase() }
        logger.lifecycle("Multi-module project '$subprojectNameValue' created at ${projectDir.absolutePath}")
        logger.lifecycle("Generated modules: shared, ${subprojectDirNames.joinToString(", ")}")
        logger.lifecycle(
            "Don't forget to add 'include(\"shared\", ${subprojectDirNames.joinToString(", ") { "\"$it\"" }})' to your settings.gradle.kts",
        )
    }

    private fun initMultiModuleSubprojectPerGroup(
        openApiSourceFile: File,
        subprojectNameValue: String,
        granularity: SplitGranularity,
    ) {
        val clientNames = parseClientNames(openApiSourceFile.absolutePath, granularity)
        val sharedGroups = parseSharedClientGroups(openApiSourceFile.absolutePath, granularity)
        val projectDir = rootDirectory.get().asFile

        val openApiDestDir = projectDir.resolve("src/main/openapi")
        openApiDestDir.mkdirs()
        val openApiFileName = openApiSourceFile.name
        val openApiDestFile = openApiDestDir.resolve(openApiFileName)
        if (openApiSourceFile.canonicalPath != openApiDestFile.canonicalPath) {
            openApiSourceFile.copyTo(openApiDestFile, overwrite = true)
        }

        val specRelativePath = "../src/main/openapi/$openApiFileName"
        val specNameWithoutExt = openApiSourceFile.nameWithoutExtension
        val basePackage =
            basePackage.orNull
                ?: "org.example.${specNameWithoutExt.lowercase().filter { it.isLetterOrDigit() }}"

        // Generate global shared project (ClientConfiguration + orphan models)
        val sharedDir = projectDir.resolve("shared")
        sharedDir.mkdirs()
        sharedDir.resolve("build.gradle.kts").writeText(
            buildSharedGradleKtsContent(
                specNameWithoutExt = specNameWithoutExt,
                specRelativePath = specRelativePath,
                basePackage = basePackage,
                kotlinVersion = kotlinVersion.get(),
                ktorVersion = ktorVersion.get(),
                coroutinesVersion = coroutinesVersion.get(),
                serializationVersion = serializationVersion.get(),
                buildScriptTemplate = buildScriptTemplate.orNull,
                generatorConfigExtra = generatorConfigExtra.orNull,
                splitGranularity = granularity,
                sharedModelGranularity = "SHARED_PER_GROUP",
            ),
        )

        // Generate one subproject per non-trivial shared group (2+ clients)
        sharedGroups.forEach { group ->
            val groupDirName = group.clientGroup.toSharedGroupDirName()
            val groupBasePackage = "$basePackage.${groupDirName.toCamelCase()}"
            val groupDir = projectDir.resolve(groupDirName)
            groupDir.mkdirs()
            groupDir.resolve("build.gradle.kts").writeText(
                buildSharedGroupGradleKtsContent(
                    specNameWithoutExt = specNameWithoutExt,
                    specRelativePath = specRelativePath,
                    basePackage = groupBasePackage,
                    targetSharedGroup = group.clientGroup.toTargetSharedGroupString(),
                    kotlinVersion = kotlinVersion.get(),
                    ktorVersion = ktorVersion.get(),
                    coroutinesVersion = coroutinesVersion.get(),
                    serializationVersion = serializationVersion.get(),
                    buildScriptTemplate = buildScriptTemplate.orNull,
                    generatorConfigExtra = generatorConfigExtra.orNull,
                    splitGranularity = granularity,
                ),
            )
        }

        // Generate client projects with references to their shared group dependencies
        clientNames.forEach { clientName ->
            val subprojectDirName = clientName.toKebabCase()
            val clientDir = projectDir.resolve(subprojectDirName)
            clientDir.mkdirs()
            // Find groups this client belongs to
            val clientGroups = sharedGroups.filter { it.clientGroup.contains(clientName) }
            clientDir.resolve("build.gradle.kts").writeText(
                buildClientPerGroupGradleKtsContent(
                    clientName = clientName,
                    subprojectDirName = subprojectDirName,
                    specNameWithoutExt = specNameWithoutExt,
                    specRelativePath = specRelativePath,
                    basePackage = basePackage,
                    clientGroups =
                        clientGroups.associate { group ->
                            group.clientGroup.toTargetSharedGroupString() to
                                "$basePackage.${group.clientGroup.toSharedGroupDirName().toCamelCase()}"
                        },
                    kotlinVersion = kotlinVersion.get(),
                    ktorVersion = ktorVersion.get(),
                    coroutinesVersion = coroutinesVersion.get(),
                    serializationVersion = serializationVersion.get(),
                    buildScriptTemplate = buildScriptTemplate.orNull,
                    generatorConfigExtra = generatorConfigExtra.orNull,
                    splitGranularity = granularity,
                ),
            )
        }

        val groupDirNames = sharedGroups.map { it.clientGroup.toSharedGroupDirName() }
        val clientDirNames = clientNames.map { it.toKebabCase() }
        val allModules = listOf("shared") + groupDirNames + clientDirNames
        logger.lifecycle("Multi-module project (SHARED_PER_GROUP) '$subprojectNameValue' created at ${projectDir.absolutePath}")
        logger.lifecycle("Generated modules: ${allModules.joinToString(", ")}")
        logger.lifecycle(
            "Don't forget to add 'include(${allModules.joinToString(", ") { "\"$it\"" }})' to your settings.gradle.kts",
        )
    }

    internal companion object {
        internal const val PLUGIN_ID = "org.litote.openapi.ktor.client.generator.gradle"

        internal fun String.toKebabCase(): String =
            replace(Regex("([a-zA-Z0-9])([A-Z])")) { "${it.groupValues[1]}-${it.groupValues[2]}" }
                .lowercase()

        /** Converts e.g. "shared-order-user" to "sharedOrderUser" (camelCase). */
        private fun String.toCamelCase(): String =
            split("-")
                .joinToString("") { part -> part.replaceFirstChar { it.uppercase() } }
                .replaceFirstChar { it.lowercase() }

        /** Converts a client group to a directory name: `{OrderClient, UserClient}` → `"shared-order-user"`. */
        internal fun Set<String>.toSharedGroupDirName(): String =
            "shared-${sorted().joinToString("-") { it.removeSuffix("Client").lowercase() }}"

        /** Converts a client group to the comma-separated sorted string used by [GenerateTask.targetSharedGroup]. */
        internal fun Set<String>.toTargetSharedGroupString(): String = sorted().joinToString(",")

        internal fun buildGradleKtsContent(
            generatorName: String,
            openApiFileName: String,
            kotlinVersion: String,
            ktorVersion: String,
            coroutinesVersion: String,
            serializationVersion: String,
            buildScriptTemplate: String? = null,
            generatorConfigExtra: String? = null,
        ): String {
            val header =
                buildScriptTemplate?.trimEnd()
                    ?: defaultHeader(kotlinVersion, ktorVersion, coroutinesVersion, serializationVersion)
            val generatorBlock =
                buildGeneratorContent(
                    generatorName = generatorName,
                    properties = listOf("""openApiFile = file("src/main/openapi/$openApiFileName")"""),
                    generatorConfigExtra = generatorConfigExtra,
                )
            return "$header\n\n$generatorBlock"
        }

        internal fun buildSharedGradleKtsContent(
            specNameWithoutExt: String,
            specRelativePath: String,
            basePackage: String,
            kotlinVersion: String,
            ktorVersion: String,
            coroutinesVersion: String,
            serializationVersion: String,
            buildScriptTemplate: String? = null,
            generatorConfigExtra: String? = null,
            splitGranularity: SplitGranularity = SplitGranularity.BY_TAG,
            sharedModelGranularity: String = "SHARED_ALL",
        ): String {
            val header =
                buildScriptTemplate?.trimEnd()
                    ?: defaultHeader(kotlinVersion, ktorVersion, coroutinesVersion, serializationVersion)
            val properties =
                buildList {
                    add("""openApiFile = file("$specRelativePath")""")
                    add("""basePackage = "$basePackage"""")
                    add("splitByClient.set(true)")
                    if (splitGranularity != SplitGranularity.BY_TAG) {
                        add("""splitGranularity.set("$splitGranularity")""")
                    }
                    if (sharedModelGranularity != "SHARED_ALL") {
                        add("""sharedModelGranularity.set("$sharedModelGranularity")""")
                    }
                }
            val generatorBlock =
                buildGeneratorContent(
                    generatorName = specNameWithoutExt,
                    properties = properties,
                    generatorConfigExtra = generatorConfigExtra,
                )
            return "$header\n\n$generatorBlock"
        }

        internal fun buildSharedGroupGradleKtsContent(
            specNameWithoutExt: String,
            specRelativePath: String,
            basePackage: String,
            targetSharedGroup: String,
            kotlinVersion: String,
            ktorVersion: String,
            coroutinesVersion: String,
            serializationVersion: String,
            buildScriptTemplate: String? = null,
            generatorConfigExtra: String? = null,
            splitGranularity: SplitGranularity = SplitGranularity.BY_TAG,
        ): String {
            val header =
                buildScriptTemplate?.trimEnd()
                    ?: defaultHeader(kotlinVersion, ktorVersion, coroutinesVersion, serializationVersion)
            val properties =
                buildList {
                    add("""openApiFile = file("$specRelativePath")""")
                    add("""basePackage = "$basePackage"""")
                    add("splitByClient.set(true)")
                    add("""sharedModelGranularity.set("SHARED_PER_GROUP")""")
                    add("""targetSharedGroup.set("$targetSharedGroup")""")
                    if (splitGranularity != SplitGranularity.BY_TAG) {
                        add("""splitGranularity.set("$splitGranularity")""")
                    }
                }
            val generatorBlock =
                buildGeneratorContent(
                    generatorName = specNameWithoutExt,
                    properties = properties,
                    generatorConfigExtra = generatorConfigExtra,
                )
            return "$header\n\n$generatorBlock"
        }

        internal fun buildClientGradleKtsContent(
            clientName: String,
            subprojectDirName: String,
            specNameWithoutExt: String,
            specRelativePath: String,
            basePackage: String,
            kotlinVersion: String,
            ktorVersion: String,
            coroutinesVersion: String,
            serializationVersion: String,
            buildScriptTemplate: String? = null,
            generatorConfigExtra: String? = null,
            splitGranularity: SplitGranularity = SplitGranularity.BY_TAG,
        ): String {
            val header =
                if (buildScriptTemplate != null) {
                    "${buildScriptTemplate.trimEnd()}\n\ndependencies {\n    api(project(\":shared\"))\n}"
                } else {
                    defaultClientHeader(kotlinVersion, ktorVersion, coroutinesVersion, serializationVersion)
                }
            val properties =
                buildList {
                    add("""openApiFile = file("$specRelativePath")""")
                    add("""basePackage = "$basePackage.${clientName.removeSuffix("Client").replaceFirstChar { it.lowercase() }}"""")
                    add("""sharedBasePackage.set("$basePackage")""")
                    add("splitByClient.set(true)")
                    add("""targetClientName.set("$clientName")""")
                    if (splitGranularity != SplitGranularity.BY_TAG) {
                        add("""splitGranularity.set("$splitGranularity")""")
                    }
                }
            val generatorBlock =
                buildGeneratorContent(
                    generatorName = specNameWithoutExt,
                    properties = properties,
                    generatorConfigExtra = generatorConfigExtra,
                )
            return "$header\n\n$generatorBlock"
        }

        /**
         * Builds a client build.gradle.kts for SHARED_PER_GROUP mode.
         * The client depends on `shared` (for ClientConfiguration) and on each per-group shared subproject.
         *
         * @param clientGroups map of group identifier string → group base package
         */
        internal fun buildClientPerGroupGradleKtsContent(
            clientName: String,
            subprojectDirName: String,
            specNameWithoutExt: String,
            specRelativePath: String,
            basePackage: String,
            clientGroups: Map<String, String>,
            kotlinVersion: String,
            ktorVersion: String,
            coroutinesVersion: String,
            serializationVersion: String,
            buildScriptTemplate: String? = null,
            generatorConfigExtra: String? = null,
            splitGranularity: SplitGranularity = SplitGranularity.BY_TAG,
        ): String {
            val groupDeps =
                clientGroups.keys.map { groupKey ->
                    val clientGroup = groupKey.split(",").toSet()
                    "    api(project(\":${clientGroup.toSharedGroupDirName()}\"))"
                }
            val allDeps = listOf("    api(project(\":shared\"))") + groupDeps
            val header =
                if (buildScriptTemplate != null) {
                    "${buildScriptTemplate.trimEnd()}\n\ndependencies {\n${allDeps.joinToString("\n")}\n}"
                } else {
                    defaultClientPerGroupHeader(
                        kotlinVersion,
                        ktorVersion,
                        coroutinesVersion,
                        serializationVersion,
                        groupProjectRefs =
                            clientGroups.keys.map { groupKey ->
                                clientGroups.keys
                                    .map { it.split(",").toSet() }
                                    .firstOrNull { it == groupKey.split(",").toSet() }
                                    ?.toSharedGroupDirName() ?: groupKey
                            },
                    )
                }
            val additionalGroupPackages =
                clientGroups.entries.joinToString(",\n") { (groupKey, pkg) ->
                    "                \"$groupKey\" to \"$pkg\""
                }
            val properties =
                buildList {
                    add("""openApiFile = file("$specRelativePath")""")
                    add("""basePackage = "$basePackage.${clientName.removeSuffix("Client").replaceFirstChar { it.lowercase() }}"""")
                    add("""sharedBasePackage.set("$basePackage")""")
                    add("splitByClient.set(true)")
                    add("""sharedModelGranularity.set("SHARED_PER_GROUP")""")
                    add("""targetClientName.set("$clientName")""")
                    if (splitGranularity != SplitGranularity.BY_TAG) {
                        add("""splitGranularity.set("$splitGranularity")""")
                    }
                    if (clientGroups.isNotEmpty()) {
                        add(
                            "additionalSharedGroupPackages.set(mapOf(\n$additionalGroupPackages\n            ))",
                        )
                    }
                }
            val generatorBlock =
                buildGeneratorContent(
                    generatorName = specNameWithoutExt,
                    properties = properties,
                    generatorConfigExtra = generatorConfigExtra,
                )
            return "$header\n\n$generatorBlock"
        }

        private fun defaultHeader(
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
            """.trimIndent()

        private fun defaultClientHeader(
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
                api(project(":shared"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
            }
            """.trimIndent()

        private fun defaultClientPerGroupHeader(
            kotlinVersion: String,
            ktorVersion: String,
            coroutinesVersion: String,
            serializationVersion: String,
            groupProjectRefs: List<String>,
        ): String {
            val groupDeps =
                groupProjectRefs.joinToString("\n") { ref ->
                    """    api(project(":$ref"))"""
                }
            return """
                plugins {
                    kotlin("jvm") version "$kotlinVersion"
                    kotlin("plugin.serialization") version "$kotlinVersion"
                    id("$PLUGIN_ID") version "$PLUGIN_VERSION"
                }

                dependencies {
                    api(project(":shared"))
                $groupDeps
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                    implementation("io.ktor:ktor-client-cio:$ktorVersion")
                    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                    implementation("io.ktor:ktor-client-core:$ktorVersion")
                    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                    implementation("io.ktor:ktor-client-logging:$ktorVersion")
                }
                """.trimIndent()
        }

        private fun buildGeneratorContent(
            generatorName: String,
            properties: List<String>,
            generatorConfigExtra: String?,
        ): String =
            buildString {
                appendLine("apiClientGenerator {")
                appendLine("    generators {")
                appendLine("        create(\"$generatorName\") {")
                properties.forEach { prop -> appendLine("            $prop") }
                generatorConfigExtra?.trimIndent()?.lines()?.forEach { line ->
                    if (line.isBlank()) appendLine() else appendLine("            $line")
                }
                appendLine("        }")
                appendLine("    }")
                append("}")
            }
    }
}
