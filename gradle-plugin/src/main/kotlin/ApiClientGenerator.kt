package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import javax.inject.Inject

public abstract class ApiClientGenerator
    @Inject
    constructor(
        objects: ObjectFactory,
        private val name: String,
    ) : Named {
        override fun getName(): String = name

        /**
         * OpenAPI3 specification file (json).
         */
        public val openApiFile: RegularFileProperty = objects.fileProperty()

        /**
         * Where generated code will be written.
         */
        public val outputDirectory: DirectoryProperty = objects.directoryProperty()

        public val basePackage: Property<String> = objects.property(String::class.java)

        /**
         * List of allowed paths to generate code for. If empty, all paths will be generated.
         */
        public val allowedPaths: SetProperty<String> = objects.setProperty(String::class.java)

        /**
         * List of allowed additional modules used to generate code.
         */
        public val modulesIds: SetProperty<String> = objects.setProperty(String::class.java)

        /**
         * Custom module instances to use during generation.
         * Use this to pass inline module implementations defined directly in the build script.
         * Note: tasks using custom modules are excluded from the Gradle configuration cache.
         */
        public val customModules: MutableList<ApiGeneratorModule> = mutableListOf()

        public val skip: Property<Boolean> = objects.property(Boolean::class.java)

        public val splitByClient: Property<Boolean> = objects.property(Boolean::class.java)

        public val targetClientName: Property<String> = objects.property(String::class.java)

        public val sharedBasePackage: Property<String> = objects.property(String::class.java)

        /**
         * Granularity used to group operations into client classes.
         * Accepted values: `BY_TAG` (default), `BY_TAG_AND_PATH`, `BY_TAG_AND_OPERATION`.
         */
        public val splitGranularity: Property<String> = objects.property(String::class.java)

        /**
         * How shared models are distributed when [splitByClient] is true.
         * Accepted values: `SHARED_ALL` (default), `SHARED_PER_GROUP`.
         */
        public val sharedModelGranularity: Property<String> = objects.property(String::class.java)

        /**
         * Exact set of client names identifying a specific shared group to generate.
         * Only used when [sharedModelGranularity] is `SHARED_PER_GROUP` and [targetClientName] is not set.
         * Encoded as a comma-separated sorted list, e.g. `"OrderClient,UserClient"`.
         */
        public val targetSharedGroup: Property<String> = objects.property(String::class.java)

        /**
         * Mapping of shared group identifier → base package of that group's subproject.
         * The group identifier is a comma-separated sorted list of client names,
         * e.g. `"OrderClient,UserClient" to "org.example.sharedOrderUser"`.
         *
         * Used when generating a client subproject that depends on per-group shared subprojects.
         * The [GenerateTask] resolves the model-to-package mapping at build time.
         */
        public val additionalSharedGroupPackages: MapProperty<String, String> =
            objects.mapProperty(String::class.java, String::class.java)

        internal fun initConventions(project: Project) {
            openApiFile.convention(project.layout.projectDirectory.file("src/main/openapi/$name.json"))
            basePackage.convention("org.example")
            allowedPaths.convention(emptySet())
            modulesIds.convention(emptySet())
            outputDirectory.convention(project.layout.buildDirectory.dir("api-$name"))
            skip.convention(null as Boolean?)
            splitByClient.convention(false)
            targetClientName.convention(null as String?)
            sharedBasePackage.convention(null as String?)
            splitGranularity.convention("BY_TAG")
            sharedModelGranularity.convention("SHARED_ALL")
            targetSharedGroup.convention(null as String?)
            additionalSharedGroupPackages.convention(emptyMap())
        }
    }
