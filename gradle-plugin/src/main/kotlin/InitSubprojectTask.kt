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
import org.litote.openapi.ktor.client.generator.computeSharedGroupDependencies
import org.litote.openapi.ktor.client.generator.parseClientNames
import org.litote.openapi.ktor.client.generator.parseSharedClientGroups
import org.litote.openapi.ktor.client.generator.shared.toSharedGroupDirName
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
            updateSettingsIncludes(rootDirectory.get().asFile, listOf(subprojectNameValue))
            logger.lifecycle("Subproject '$subprojectNameValue' created at ${subprojectDir.absolutePath}")
            logger.lifecycle("settings.gradle.kts updated with include(\"$subprojectNameValue\")")
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
        val allModulesSharedAll = listOf("shared") + subprojectDirNames
        updateSettingsIncludes(rootDirectory.get().asFile, allModulesSharedAll)
        logger.lifecycle("Multi-module project '$subprojectNameValue' created at ${projectDir.absolutePath}")
        logger.lifecycle("Generated modules: ${allModulesSharedAll.joinToString(", ")}")
        logger.lifecycle("settings.gradle.kts updated with include(${allModulesSharedAll.joinToString(", ") { "\"$it\"" }})")
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
        val groupPackageMap =
            sharedGroups.associate { g ->
                g.clientGroup.toTargetSharedGroupString() to "$basePackage.${g.clientGroup.toSharedGroupDirName().toCamelCase()}"
            }
        // Compute which groups each group directly depends on (for cross-group model references)
        val groupDepsMap =
            computeSharedGroupDependencies(openApiSourceFile.absolutePath, granularity)
                .entries
                .associate { (group, deps) ->
                    group.clientGroup.toTargetSharedGroupString() to
                        deps.map { it.clientGroup.toSharedGroupDirName() }
                }
        sharedGroups.forEach { group ->
            val groupDirName = group.clientGroup.toSharedGroupDirName()
            val groupBasePackage = "$basePackage.${groupDirName.toCamelCase()}"
            val groupDir = projectDir.resolve(groupDirName)
            groupDir.mkdirs()
            // All other groups are passed for import resolution in the generator
            val otherGroupPackages = groupPackageMap.filterKeys { it != group.clientGroup.toTargetSharedGroupString() }
            // Only the groups this group's models actually reference need a Gradle compile dependency
            val directDepDirNames =
                groupDepsMap[group.clientGroup.toTargetSharedGroupString()] ?: emptyList()
            groupDir.resolve("build.gradle.kts").writeText(
                buildSharedGroupGradleKtsContent(
                    specNameWithoutExt = specNameWithoutExt,
                    specRelativePath = specRelativePath,
                    basePackage = groupBasePackage,
                    topBasePackage = basePackage,
                    targetSharedGroup = group.clientGroup.toTargetSharedGroupString(),
                    additionalSharedGroupPackages = otherGroupPackages,
                    directGroupDeps = directDepDirNames,
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
        updateSettingsIncludes(rootDirectory.get().asFile, allModules)
        logger.lifecycle("Multi-module project (SHARED_PER_GROUP) '$subprojectNameValue' created at ${projectDir.absolutePath}")
        logger.lifecycle("Generated modules: ${allModules.joinToString(", ")}")
        logger.lifecycle("settings.gradle.kts updated with include(${allModules.joinToString(", ") { "\"$it\"" }})")
    }

    internal companion object {
        internal const val PLUGIN_ID = "org.litote.openapi.ktor.client.generator.gradle"

        internal const val SETTINGS_MARKER_START = "// <openapi-ktor-generated-includes>"
        internal const val SETTINGS_MARKER_END = "// </openapi-ktor-generated-includes>"

        internal fun updateSettingsIncludes(
            rootDir: File,
            modules: List<String>,
        ) {
            val settingsFile = rootDir.resolve("settings.gradle.kts")
            val includeStatement = "include(${modules.joinToString(", ") { "\"$it\"" }})"
            val newBlock = "$SETTINGS_MARKER_START\n$includeStatement\n$SETTINGS_MARKER_END"
            val existing = if (settingsFile.exists()) settingsFile.readText() else ""
            val updated =
                if (existing.contains(SETTINGS_MARKER_START)) {
                    existing.replace(
                        Regex(
                            "${Regex.escape(SETTINGS_MARKER_START)}.*?${Regex.escape(SETTINGS_MARKER_END)}",
                            RegexOption.DOT_MATCHES_ALL,
                        ),
                        newBlock,
                    )
                } else if (existing.isEmpty()) {
                    "$newBlock\n"
                } else {
                    "$existing\n\n$newBlock\n"
                }
            settingsFile.writeText(updated)
        }

        internal fun String.toKebabCase(): String =
            replace(Regex("([a-zA-Z0-9])([A-Z])")) { "${it.groupValues[1]}-${it.groupValues[2]}" }
                .lowercase()

        /** Converts e.g. "shared-order-user" to "sharedOrderUser" (camelCase). */
        private fun String.toCamelCase(): String =
            split("-")
                .joinToString("") { part -> part.replaceFirstChar { it.uppercase() } }
                .replaceFirstChar { it.lowercase() }

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
            topBasePackage: String,
            targetSharedGroup: String,
            additionalSharedGroupPackages: Map<String, String> = emptyMap(),
            /**
             * Direct Gradle compile dependencies on other per-group subprojects.
             * These are subproject directory names (e.g. "shared-alpha-beta") whose models
             * are directly referenced by models in this group.
             */
            directGroupDeps: List<String> = emptyList(),
            kotlinVersion: String,
            ktorVersion: String,
            coroutinesVersion: String,
            serializationVersion: String,
            buildScriptTemplate: String? = null,
            generatorConfigExtra: String? = null,
            splitGranularity: SplitGranularity = SplitGranularity.BY_TAG,
        ): String {
            // Per-group subprojects always depend on :shared. They also depend on specific other
            // per-group subprojects whose models they directly reference (directGroupDeps).
            val header =
                if (buildScriptTemplate != null) {
                    val sharedDep = """    api(project(":shared"))"""
                    val otherDeps = directGroupDeps.map { """    api(project(":$it"))""" }
                    val extraDeps = (listOf(sharedDep) + otherDeps).joinToString("\n")
                    "${buildScriptTemplate.trimEnd()}\n\ndependencies {\n$extraDeps\n}"
                } else {
                    defaultSharedGroupHeader(
                        kotlinVersion,
                        ktorVersion,
                        coroutinesVersion,
                        serializationVersion,
                        directGroupDeps,
                    )
                }
            val properties =
                buildList {
                    add("""openApiFile = file("$specRelativePath")""")
                    add("""basePackage = "$basePackage"""")
                    add("""sharedBasePackage.set("$topBasePackage")""")
                    add("splitByClient.set(true)")
                    add("""sharedModelGranularity.set("SHARED_PER_GROUP")""")
                    add("""targetSharedGroup.set("$targetSharedGroup")""")
                    if (additionalSharedGroupPackages.isNotEmpty()) {
                        val mapEntries =
                            additionalSharedGroupPackages.entries.joinToString(", ") { (k, v) ->
                                """"$k" to "$v""""
                            }
                        add("""additionalSharedGroupPackages.set(mapOf($mapEntries))""")
                    }
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

        private fun defaultSharedGroupHeader(
            kotlinVersion: String,
            ktorVersion: String,
            coroutinesVersion: String,
            serializationVersion: String,
            directGroupDeps: List<String> = emptyList(),
        ): String {
            val groupDeps = directGroupDeps.joinToString("\n") { ref -> """    api(project(":$ref"))""" }
            val allDeps =
                if (groupDeps.isBlank()) {
                    "    api(project(\":shared\"))"
                } else {
                    "    api(project(\":shared\"))\n$groupDeps"
                }
            return """
                plugins {
                    kotlin("jvm") version "$kotlinVersion"
                    kotlin("plugin.serialization") version "$kotlinVersion"
                    id("$PLUGIN_ID") version "$PLUGIN_VERSION"
                }

                dependencies {
                $allDeps
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
