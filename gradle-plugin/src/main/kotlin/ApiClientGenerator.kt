package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.Named
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
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

        public val skip: Property<Boolean> = objects.property(Boolean::class.java)

        public val splitByClient: Property<Boolean> = objects.property(Boolean::class.java)

        public val targetClientName: Property<String> = objects.property(String::class.java)

        public val sharedBasePackage: Property<String> = objects.property(String::class.java)

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
        }
    }
