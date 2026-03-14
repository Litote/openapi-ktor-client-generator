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
    /** Groups the four Gradle dependency version strings used across all generated build files. */
    internal data class DependencyVersions(
        val kotlinVersion: String,
        val ktorVersion: String,
        val coroutinesVersion: String,
        val serializationVersion: String,
    )

    /** Groups optional generator configuration switches passed to the content-builder functions. */
    internal data class GeneratorOptions(
        val buildScriptTemplate: String? = null,
        val generatorConfigExtra: String? = null,
        val splitGranularity: SplitGranularity = SplitGranularity.BY_TAG,
        val multiplatform: Boolean = false,
    )

    /** Groups spec identification fields shared across multi-module build-file generators. */
    internal data class SpecConfig(
        val nameWithoutExt: String,
        val relativePath: String,
        val basePackage: String,
    )

    /** Groups shared-group-specific configuration for [buildSharedGroupGradleKtsContent]. */
    internal data class SharedGroupConfig(
        val topBasePackage: String,
        val targetSharedGroup: String,
        val additionalSharedGroupPackages: Map<String, String> = emptyMap(),
        val directGroupDeps: List<String> = emptyList(),
    )

    internal class MultiModuleContext(
        val projectDir: File,
        val spec: SpecConfig,
        val versions: DependencyVersions,
        val options: GeneratorOptions,
        private val subprojectRootDirectory: String?,
    ) {
        fun resolveModuleDir(name: String): File =
            if (subprojectRootDirectory != null) {
                projectDir.resolve("$subprojectRootDirectory/$name")
            } else {
                projectDir.resolve(name)
            }

        fun moduleIncludeName(name: String): String = if (subprojectRootDirectory != null) ":$subprojectRootDirectory:$name" else name
    }

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

    /**
     * Optional intermediate directory name used to group all generated multi-module subprojects
     * under a common subdirectory. When set to e.g. `"clients"`, modules `shared` and `user-client`
     * are created at `clients/shared` and `clients/user-client`, and the settings include becomes
     * `include("clients/shared", "clients/user-client")`.
     * Passed via -PsubprojectRootDirectory=...
     */
    @get:Input
    @get:Optional
    public abstract val subprojectRootDirectory: Property<String>

    /**
     * When true, generated `build.gradle.kts` files use `kotlin("multiplatform")` instead of
     * `kotlin("jvm")`, and dependencies are placed inside a `kotlin { sourceSets { commonMain.dependencies { } } }`
     * block. A single `jvm()` target is declared by default; add other targets manually.
     * Passed via -PmultiplatformTargets=true.
     */
    @get:Input
    @get:Optional
    public abstract val multiplatform: Property<Boolean>

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
        val subprojectRootDirectoryValue = subprojectRootDirectory.orNull

        if (splitByClient.getOrElse(false)) {
            if (sharedModelGranularityValue == "SHARED_PER_GROUP") {
                initMultiModuleSubprojectPerGroup(openApiSourceFile, subprojectNameValue, granularity, subprojectRootDirectoryValue)
            } else {
                initMultiModuleSubproject(openApiSourceFile, subprojectNameValue, granularity, subprojectRootDirectoryValue)
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
        val openApiDestDir = subprojectDir.resolve(OPENAPI_SRC_DIR)
        openApiDestDir.mkdirs()

        val openApiFileName = openApiSourceFile.name
        val openApiDestFile = openApiDestDir.resolve(openApiFileName)
        if (openApiSourceFile.canonicalPath != openApiDestFile.canonicalPath) {
            openApiSourceFile.copyTo(openApiDestFile, overwrite = true)
        }

        val generatorName = openApiSourceFile.nameWithoutExtension
        subprojectDir.resolve(BUILD_GRADLE_KTS).writeText(
            buildGradleKtsContent(
                generatorName = generatorName,
                openApiFileName = openApiFileName,
                versions =
                    DependencyVersions(
                        kotlinVersion = kotlinVersion.get(),
                        ktorVersion = ktorVersion.get(),
                        coroutinesVersion = coroutinesVersion.get(),
                        serializationVersion = serializationVersion.get(),
                    ),
                options =
                    GeneratorOptions(
                        buildScriptTemplate = buildScriptTemplate.orNull,
                        generatorConfigExtra = generatorConfigExtra.orNull,
                        multiplatform = multiplatform.getOrElse(false),
                    ),
            ),
        )

        logger.info("Generated subproject '$subprojectNameValue'")
    }

    private fun buildMultiModuleContext(
        openApiSourceFile: File,
        granularity: SplitGranularity,
        subprojectRootDirectoryValue: String?,
    ): MultiModuleContext {
        val projectDir = rootDirectory.get().asFile
        val openApiDestDir = projectDir.resolve(OPENAPI_SRC_DIR)
        openApiDestDir.mkdirs()
        val openApiFileName = openApiSourceFile.name
        val openApiDestFile = openApiDestDir.resolve(openApiFileName)
        if (openApiSourceFile.canonicalPath != openApiDestFile.canonicalPath) {
            openApiSourceFile.copyTo(openApiDestFile, overwrite = true)
        }
        val specRelativePath =
            if (subprojectRootDirectoryValue != null) {
                "../../$OPENAPI_SRC_DIR/$openApiFileName"
            } else {
                "../$OPENAPI_SRC_DIR/$openApiFileName"
            }
        val specNameWithoutExt = openApiSourceFile.nameWithoutExtension
        val basePackage =
            basePackage.orNull
                ?: "org.example.${specNameWithoutExt.lowercase().filter { it.isLetterOrDigit() }}"
        return MultiModuleContext(
            projectDir = projectDir,
            spec = SpecConfig(nameWithoutExt = specNameWithoutExt, relativePath = specRelativePath, basePackage = basePackage),
            versions =
                DependencyVersions(
                    kotlinVersion = kotlinVersion.get(),
                    ktorVersion = ktorVersion.get(),
                    coroutinesVersion = coroutinesVersion.get(),
                    serializationVersion = serializationVersion.get(),
                ),
            options =
                GeneratorOptions(
                    buildScriptTemplate = buildScriptTemplate.orNull,
                    generatorConfigExtra = generatorConfigExtra.orNull,
                    splitGranularity = granularity,
                    multiplatform = multiplatform.getOrElse(false),
                ),
            subprojectRootDirectory = subprojectRootDirectoryValue,
        )
    }

    private fun initMultiModuleSubproject(
        openApiSourceFile: File,
        subprojectNameValue: String,
        granularity: SplitGranularity,
        subprojectRootDirectoryValue: String? = null,
    ) {
        val clientNames = parseClientNames(openApiSourceFile.absolutePath, granularity)
        val ctx = buildMultiModuleContext(openApiSourceFile, granularity, subprojectRootDirectoryValue)

        val sharedDir = ctx.resolveModuleDir("shared")
        sharedDir.mkdirs()
        sharedDir.resolve(BUILD_GRADLE_KTS).writeText(buildSharedGradleKtsContent(ctx.spec, ctx.versions, ctx.options))

        clientNames.forEach { clientName ->
            val subprojectDirName = clientName.toKebabCase()
            val clientDir = ctx.resolveModuleDir(subprojectDirName)
            clientDir.mkdirs()
            clientDir.resolve(BUILD_GRADLE_KTS).writeText(
                buildClientGradleKtsContent(
                    clientName = clientName,
                    subprojectDirName = subprojectDirName,
                    spec = ctx.spec,
                    versions = ctx.versions,
                    options = ctx.options,
                    subprojectRootDirectory = subprojectRootDirectoryValue,
                ),
            )
        }

        val subprojectDirNames = clientNames.map { it.toKebabCase() }
        val allModulesSharedAll = listOf("shared") + subprojectDirNames
        updateSettingsIncludes(rootDirectory.get().asFile, allModulesSharedAll.map { ctx.moduleIncludeName(it) })
        logger.lifecycle("Multi-module project '$subprojectNameValue' created at ${ctx.projectDir.absolutePath}")
        logger.lifecycle("Generated modules: ${allModulesSharedAll.joinToString(", ")}")
        logger.lifecycle(
            "settings.gradle.kts updated with include(${allModulesSharedAll.map {
                ctx.moduleIncludeName(
                    it,
                )
            }.joinToString(", ") { "\"$it\"" }})",
        )
    }

    private fun initMultiModuleSubprojectPerGroup(
        openApiSourceFile: File,
        subprojectNameValue: String,
        granularity: SplitGranularity,
        subprojectRootDirectoryValue: String? = null,
    ) {
        val clientNames = parseClientNames(openApiSourceFile.absolutePath, granularity)
        val sharedGroups = parseSharedClientGroups(openApiSourceFile.absolutePath, granularity)
        val ctx = buildMultiModuleContext(openApiSourceFile, granularity, subprojectRootDirectoryValue)
        val basePackage = ctx.spec.basePackage

        // Generate global shared project (ClientConfiguration only in SHARED_PER_GROUP mode)
        val sharedDir = ctx.resolveModuleDir("shared")
        sharedDir.mkdirs()
        sharedDir.resolve(BUILD_GRADLE_KTS).writeText(
            buildSharedGradleKtsContent(ctx.spec, ctx.versions, ctx.options, sharedModelGranularity = "SHARED_PER_GROUP"),
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
            val groupDir = ctx.resolveModuleDir(groupDirName)
            groupDir.mkdirs()
            // All other groups are passed for import resolution in the generator
            val otherGroupPackages = groupPackageMap.filterKeys { it != group.clientGroup.toTargetSharedGroupString() }
            // Only the groups this group's models actually reference need a Gradle compile dependency
            val directDepDirNames =
                groupDepsMap[group.clientGroup.toTargetSharedGroupString()] ?: emptyList()
            groupDir.resolve(BUILD_GRADLE_KTS).writeText(
                buildSharedGroupGradleKtsContent(
                    spec = ctx.spec.copy(basePackage = groupBasePackage),
                    groupConfig =
                        SharedGroupConfig(
                            topBasePackage = basePackage,
                            targetSharedGroup = group.clientGroup.toTargetSharedGroupString(),
                            additionalSharedGroupPackages = otherGroupPackages,
                            directGroupDeps = directDepDirNames,
                        ),
                    versions = ctx.versions,
                    options = ctx.options,
                    subprojectRootDirectory = subprojectRootDirectoryValue,
                ),
            )
        }

        // Generate client projects with references to their shared group dependencies
        clientNames.forEach { clientName ->
            val subprojectDirName = clientName.toKebabCase()
            val clientDir = ctx.resolveModuleDir(subprojectDirName)
            clientDir.mkdirs()
            // Find groups this client belongs to
            val clientGroups = sharedGroups.filter { it.clientGroup.contains(clientName) }
            clientDir.resolve(BUILD_GRADLE_KTS).writeText(
                buildClientPerGroupGradleKtsContent(
                    clientName = clientName,
                    subprojectDirName = subprojectDirName,
                    spec = ctx.spec,
                    clientGroups =
                        clientGroups.associate { group ->
                            group.clientGroup.toTargetSharedGroupString() to
                                "$basePackage.${group.clientGroup.toSharedGroupDirName().toCamelCase()}"
                        },
                    versions = ctx.versions,
                    options = ctx.options,
                    subprojectRootDirectory = subprojectRootDirectoryValue,
                ),
            )
        }

        val groupDirNames = sharedGroups.map { it.clientGroup.toSharedGroupDirName() }
        val clientDirNames = clientNames.map { it.toKebabCase() }
        val allModules = listOf("shared") + groupDirNames + clientDirNames
        updateSettingsIncludes(rootDirectory.get().asFile, allModules.map { ctx.moduleIncludeName(it) })
        logger.lifecycle("Multi-module project (SHARED_PER_GROUP) '$subprojectNameValue' created at ${ctx.projectDir.absolutePath}")
        logger.lifecycle("Generated modules: ${allModules.joinToString(", ")}")
        logger.lifecycle(
            "settings.gradle.kts updated with include(${allModules.map { ctx.moduleIncludeName(it) }.joinToString(", ") { "\"$it\"" }})",
        )
    }

    internal companion object {
        internal const val PLUGIN_ID = "org.litote.openapi.ktor.client.generator.gradle"

        internal const val SETTINGS_MARKER_START = "// <openapi-ktor-generated-includes>"
        internal const val SETTINGS_MARKER_END = "// </openapi-ktor-generated-includes>"

        private const val OPENAPI_SRC_DIR = "src/main/openapi"
        private const val BUILD_GRADLE_KTS = "build.gradle.kts"
        private const val SHARED_PROJECT_REF = ":shared"
        private const val SPLIT_BY_CLIENT = "splitByClient.set(true)"

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
            versions: DependencyVersions,
            options: GeneratorOptions = GeneratorOptions(),
        ): String {
            val header =
                options.buildScriptTemplate?.trimEnd()
                    ?: defaultHeader(versions, options.multiplatform)
            val generatorBlock =
                buildGeneratorContent(
                    generatorName = generatorName,
                    properties = listOf("""openApiFile = file("$OPENAPI_SRC_DIR/$openApiFileName")"""),
                    generatorConfigExtra = options.generatorConfigExtra,
                )
            return "$header\n\n$generatorBlock"
        }

        internal fun buildSharedGradleKtsContent(
            spec: SpecConfig,
            versions: DependencyVersions,
            options: GeneratorOptions = GeneratorOptions(),
            sharedModelGranularity: String = "SHARED_ALL",
        ): String {
            val header =
                options.buildScriptTemplate?.trimEnd()
                    ?: defaultHeader(versions, options.multiplatform)
            val properties =
                buildList {
                    add("""openApiFile = file("${spec.relativePath}")""")
                    add("""basePackage = "${spec.basePackage}"""")
                    add(SPLIT_BY_CLIENT)
                    if (options.splitGranularity != SplitGranularity.BY_TAG) {
                        add("""splitGranularity.set("${options.splitGranularity}")""")
                    }
                    if (sharedModelGranularity != "SHARED_ALL") {
                        add("""sharedModelGranularity.set("$sharedModelGranularity")""")
                    }
                }
            val generatorBlock =
                buildGeneratorContent(
                    generatorName = spec.nameWithoutExt,
                    properties = properties,
                    generatorConfigExtra = options.generatorConfigExtra,
                )
            return "$header\n\n$generatorBlock"
        }

        internal fun buildSharedGroupGradleKtsContent(
            spec: SpecConfig,
            groupConfig: SharedGroupConfig,
            versions: DependencyVersions,
            options: GeneratorOptions = GeneratorOptions(),
            subprojectRootDirectory: String? = null,
        ): String {
            val sharedProjectRef = if (subprojectRootDirectory != null) ":$subprojectRootDirectory:shared" else SHARED_PROJECT_REF
            val groupProjectRef: (String) -> String = { dirName ->
                if (subprojectRootDirectory != null) ":$subprojectRootDirectory:$dirName" else ":$dirName"
            }
            val header =
                if (options.buildScriptTemplate != null) {
                    val sharedDep = """    api(project("$sharedProjectRef"))"""
                    val otherDeps = groupConfig.directGroupDeps.map { """    api(project("${groupProjectRef(it)}"))""" }
                    val depsBlock = buildProjectDepsBlock(listOf(sharedDep) + otherDeps, options.multiplatform)
                    "${options.buildScriptTemplate.trimEnd()}\n\n$depsBlock"
                } else {
                    defaultSharedGroupHeader(
                        versions,
                        groupConfig.directGroupDeps,
                        sharedProjectRef,
                        groupProjectRef,
                        options.multiplatform,
                    )
                }
            val properties =
                buildList {
                    add("""openApiFile = file("${spec.relativePath}")""")
                    add("""basePackage = "${spec.basePackage}"""")
                    add("""sharedBasePackage.set("${groupConfig.topBasePackage}")""")
                    add(SPLIT_BY_CLIENT)
                    add("""sharedModelGranularity.set("SHARED_PER_GROUP")""")
                    add("""targetSharedGroup.set("${groupConfig.targetSharedGroup}")""")
                    if (groupConfig.additionalSharedGroupPackages.isNotEmpty()) {
                        val mapEntries =
                            groupConfig.additionalSharedGroupPackages.entries.joinToString(", ") { (k, v) ->
                                """"$k" to "$v""""
                            }
                        add("""additionalSharedGroupPackages.set(mapOf($mapEntries))""")
                    }
                    if (options.splitGranularity != SplitGranularity.BY_TAG) {
                        add("""splitGranularity.set("${options.splitGranularity}")""")
                    }
                }
            val generatorBlock =
                buildGeneratorContent(
                    generatorName = spec.nameWithoutExt,
                    properties = properties,
                    generatorConfigExtra = options.generatorConfigExtra,
                )
            return "$header\n\n$generatorBlock"
        }

        internal fun buildClientGradleKtsContent(
            clientName: String,
            subprojectDirName: String,
            spec: SpecConfig,
            versions: DependencyVersions,
            options: GeneratorOptions = GeneratorOptions(),
            subprojectRootDirectory: String? = null,
        ): String {
            val sharedProjectRef = if (subprojectRootDirectory != null) ":$subprojectRootDirectory:shared" else SHARED_PROJECT_REF
            val header =
                if (options.buildScriptTemplate != null) {
                    val depsBlock = buildProjectDepsBlock(listOf("""    api(project("$sharedProjectRef"))"""), options.multiplatform)
                    "${options.buildScriptTemplate.trimEnd()}\n\n$depsBlock"
                } else {
                    defaultClientHeader(versions, sharedProjectRef, options.multiplatform)
                }
            val properties =
                buildList {
                    add("""openApiFile = file("${spec.relativePath}")""")
                    add("""basePackage = "${spec.basePackage}.${clientName.removeSuffix("Client").replaceFirstChar { it.lowercase() }}"""")
                    add("""sharedBasePackage.set("${spec.basePackage}")""")
                    add(SPLIT_BY_CLIENT)
                    add("""targetClientName.set("$clientName")""")
                    if (options.splitGranularity != SplitGranularity.BY_TAG) {
                        add("""splitGranularity.set("${options.splitGranularity}")""")
                    }
                }
            val generatorBlock =
                buildGeneratorContent(
                    generatorName = spec.nameWithoutExt,
                    properties = properties,
                    generatorConfigExtra = options.generatorConfigExtra,
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
            spec: SpecConfig,
            clientGroups: Map<String, String>,
            versions: DependencyVersions,
            options: GeneratorOptions = GeneratorOptions(),
            subprojectRootDirectory: String? = null,
        ): String {
            val sharedProjectRef = if (subprojectRootDirectory != null) ":$subprojectRootDirectory:shared" else SHARED_PROJECT_REF
            val groupProjectRef: (String) -> String = { dirName ->
                if (subprojectRootDirectory != null) ":$subprojectRootDirectory:$dirName" else ":$dirName"
            }
            val groupDeps =
                clientGroups.keys.map { groupKey ->
                    val clientGroup = groupKey.split(",").toSet()
                    "    api(project(\"${groupProjectRef(clientGroup.toSharedGroupDirName())}\"))"
                }
            val allDeps = listOf("    api(project(\"$sharedProjectRef\"))") + groupDeps
            val header =
                if (options.buildScriptTemplate != null) {
                    val depsBlock = buildProjectDepsBlock(allDeps, options.multiplatform)
                    "${options.buildScriptTemplate.trimEnd()}\n\n$depsBlock"
                } else {
                    defaultClientPerGroupHeader(
                        versions,
                        sharedProjectRef = sharedProjectRef,
                        groupProjectRefs =
                            clientGroups.keys.map { groupKey ->
                                clientGroups.keys
                                    .map { it.split(",").toSet() }
                                    .firstOrNull { it == groupKey.split(",").toSet() }
                                    ?.toSharedGroupDirName()
                                    ?.let { groupProjectRef(it) } ?: groupKey
                            },
                        multiplatform = options.multiplatform,
                    )
                }
            val additionalGroupPackages =
                clientGroups.entries.joinToString(",\n") { (groupKey, pkg) ->
                    "                \"$groupKey\" to \"$pkg\""
                }
            val properties =
                buildList {
                    add("""openApiFile = file("${spec.relativePath}")""")
                    add("""basePackage = "${spec.basePackage}.${clientName.removeSuffix("Client").replaceFirstChar { it.lowercase() }}"""")
                    add("""sharedBasePackage.set("${spec.basePackage}")""")
                    add(SPLIT_BY_CLIENT)
                    add("""sharedModelGranularity.set("SHARED_PER_GROUP")""")
                    add("""targetClientName.set("$clientName")""")
                    if (options.splitGranularity != SplitGranularity.BY_TAG) {
                        add("""splitGranularity.set("${options.splitGranularity}")""")
                    }
                    if (clientGroups.isNotEmpty()) {
                        add(
                            "additionalSharedGroupPackages.set(mapOf(\n$additionalGroupPackages\n            ))",
                        )
                    }
                }
            val generatorBlock =
                buildGeneratorContent(
                    generatorName = spec.nameWithoutExt,
                    properties = properties,
                    generatorConfigExtra = options.generatorConfigExtra,
                )
            return "$header\n\n$generatorBlock"
        }

        private fun defaultHeader(
            versions: DependencyVersions,
            multiplatform: Boolean = false,
        ): String {
            val (kotlinVersion, ktorVersion, coroutinesVersion, serializationVersion) = versions
            return if (multiplatform) {
                """
                plugins {
                    kotlin("multiplatform") version "$kotlinVersion"
                    kotlin("plugin.serialization") version "$kotlinVersion"
                    id("$PLUGIN_ID") version "$PLUGIN_VERSION"
                }

                kotlin {
                    jvm()
                    // Add your targets: iosArm64(), js(IR) { browser() }, linuxX64(), etc.

                    sourceSets {
                        commonMain.dependencies {
                            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                            implementation("io.ktor:ktor-client-cio:$ktorVersion")
                            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                            implementation("io.ktor:ktor-client-core:$ktorVersion")
                            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                            implementation("io.ktor:ktor-client-logging:$ktorVersion")
                        }
                    }
                }
                """.trimIndent()
            } else {
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
            }
        }

        private fun defaultSharedGroupHeader(
            versions: DependencyVersions,
            directGroupDeps: List<String> = emptyList(),
            sharedProjectRef: String = SHARED_PROJECT_REF,
            groupProjectRef: (String) -> String = { ":$it" },
            multiplatform: Boolean = false,
        ): String {
            val (kotlinVersion, ktorVersion, coroutinesVersion, serializationVersion) = versions
            val groupDeps = directGroupDeps.joinToString("\n") { ref -> """    api(project("${groupProjectRef(ref)}"))""" }
            val allDeps =
                if (groupDeps.isBlank()) {
                    """    api(project("$sharedProjectRef"))"""
                } else {
                    """    api(project("$sharedProjectRef"))""" + "\n$groupDeps"
                }
            return if (multiplatform) {
                val indentedDeps = allDeps.lines().joinToString("\n") { "        $it" }
                """
                plugins {
                    kotlin("multiplatform") version "$kotlinVersion"
                    kotlin("plugin.serialization") version "$kotlinVersion"
                    id("$PLUGIN_ID") version "$PLUGIN_VERSION"
                }

                kotlin {
                    jvm()
                    // Add your targets: iosArm64(), js(IR) { browser() }, linuxX64(), etc.

                    sourceSets {
                        commonMain.dependencies {
                $indentedDeps
                            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                            implementation("io.ktor:ktor-client-cio:$ktorVersion")
                            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                            implementation("io.ktor:ktor-client-core:$ktorVersion")
                            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                            implementation("io.ktor:ktor-client-logging:$ktorVersion")
                        }
                    }
                }
                """.trimIndent()
            } else {
                """
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
        }

        private fun defaultClientHeader(
            versions: DependencyVersions,
            sharedProjectRef: String = SHARED_PROJECT_REF,
            multiplatform: Boolean = false,
        ): String {
            val (kotlinVersion, ktorVersion, coroutinesVersion, serializationVersion) = versions
            return if (multiplatform) {
                """
                plugins {
                    kotlin("multiplatform") version "$kotlinVersion"
                    kotlin("plugin.serialization") version "$kotlinVersion"
                    id("$PLUGIN_ID") version "$PLUGIN_VERSION"
                }

                kotlin {
                    jvm()
                    // Add your targets: iosArm64(), js(IR) { browser() }, linuxX64(), etc.

                    sourceSets {
                        commonMain.dependencies {
                            api(project("$sharedProjectRef"))
                            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                            implementation("io.ktor:ktor-client-cio:$ktorVersion")
                            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                            implementation("io.ktor:ktor-client-core:$ktorVersion")
                            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                            implementation("io.ktor:ktor-client-logging:$ktorVersion")
                        }
                    }
                }
                """.trimIndent()
            } else {
                """
                plugins {
                    kotlin("jvm") version "$kotlinVersion"
                    kotlin("plugin.serialization") version "$kotlinVersion"
                    id("$PLUGIN_ID") version "$PLUGIN_VERSION"
                }

                dependencies {
                    api(project("$sharedProjectRef"))
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
        }

        private fun defaultClientPerGroupHeader(
            versions: DependencyVersions,
            sharedProjectRef: String = SHARED_PROJECT_REF,
            groupProjectRefs: List<String>,
            multiplatform: Boolean = false,
        ): String {
            val (kotlinVersion, ktorVersion, coroutinesVersion, serializationVersion) = versions
            val groupDeps =
                groupProjectRefs.joinToString("\n") { ref ->
                    """    api(project("$ref"))"""
                }
            return if (multiplatform) {
                val indentedGroupDeps = groupDeps.lines().joinToString("\n") { "        $it" }
                """
                plugins {
                    kotlin("multiplatform") version "$kotlinVersion"
                    kotlin("plugin.serialization") version "$kotlinVersion"
                    id("$PLUGIN_ID") version "$PLUGIN_VERSION"
                }

                kotlin {
                    jvm()
                    // Add your targets: iosArm64(), js(IR) { browser() }, linuxX64(), etc.

                    sourceSets {
                        commonMain.dependencies {
                            api(project("$sharedProjectRef"))
                $indentedGroupDeps
                            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                            implementation("io.ktor:ktor-client-cio:$ktorVersion")
                            implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                            implementation("io.ktor:ktor-client-core:$ktorVersion")
                            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                            implementation("io.ktor:ktor-client-logging:$ktorVersion")
                        }
                    }
                }
                """.trimIndent()
            } else {
                """
                plugins {
                    kotlin("jvm") version "$kotlinVersion"
                    kotlin("plugin.serialization") version "$kotlinVersion"
                    id("$PLUGIN_ID") version "$PLUGIN_VERSION"
                }

                dependencies {
                    api(project("$sharedProjectRef"))
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
        }

        /**
         * Builds a `dependencies {}` or `sourceSets { commonMain.dependencies {} }` block
         * from a list of dependency lines (each already 4-space-indented, e.g. `    api(project(...))`).
         */
        private fun buildProjectDepsBlock(
            deps: List<String>,
            multiplatform: Boolean,
        ): String =
            if (multiplatform) {
                val indented = deps.joinToString("\n") { "        $it" }
                "kotlin {\n    sourceSets {\n        commonMain.dependencies {\n$indented\n        }\n    }\n}"
            } else {
                "dependencies {\n${deps.joinToString("\n")}\n}"
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
