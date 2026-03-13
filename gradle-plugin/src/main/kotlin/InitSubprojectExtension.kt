package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

public abstract class InitSubprojectExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        /** Override the Kotlin version used in the generated build.gradle.kts. */
        @get:Input
        @get:Optional
        public val kotlinVersion: Property<String> = objects.property(String::class.java)

        /** Override the Ktor version used in the generated build.gradle.kts. */
        @get:Input
        @get:Optional
        public val ktorVersion: Property<String> = objects.property(String::class.java)

        /** Override the kotlinx.coroutines version used in the generated build.gradle.kts. */
        @get:Input
        @get:Optional
        public val coroutinesVersion: Property<String> = objects.property(String::class.java)

        /** Override the kotlinx.serialization version used in the generated build.gradle.kts. */
        @get:Input
        @get:Optional
        public val serializationVersion: Property<String> = objects.property(String::class.java)

        /**
         * Custom template that replaces the auto-generated `plugins {}` and `dependencies {}` blocks
         * in all generated build.gradle.kts files.
         *
         * When set, the provided content is used verbatim before the `apiClientGenerator {}` block.
         * Useful for projects using a Gradle version catalog (libs.versions.toml).
         *
         * For multi-module client builds, `dependencies { api(project(":shared")) }` is still
         * appended automatically after the template.
         */
        @get:Input
        @get:Optional
        public val buildScriptTemplate: Property<String> = objects.property(String::class.java)

        /**
         * Extra configuration lines appended inside the `create("...") { }` generator block of
         * all generated build.gradle.kts files, after the last auto-generated property.
         *
         * Example: `modulesIds.add("UnknownEnumValueModule")`
         */
        @get:Input
        @get:Optional
        public val generatorConfigExtra: Property<String> = objects.property(String::class.java)
    }
