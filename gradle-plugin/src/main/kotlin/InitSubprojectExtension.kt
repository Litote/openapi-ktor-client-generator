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

        /**
         * Optional intermediate directory name used to group all generated multi-module subprojects
         * under a common subdirectory. When set to e.g. `"clients"`, modules `shared` and `user-client`
         * are created at `clients/shared` and `clients/user-client`, and the settings include becomes
         * `include("clients/shared", "clients/user-client")`.
         */
        @get:Input
        @get:Optional
        public val subprojectRootDirectory: Property<String> = objects.property(String::class.java)

        /**
         * When true, generated `build.gradle.kts` files use `kotlin("multiplatform")` instead of
         * `kotlin("jvm")`, and dependencies are placed inside a `kotlin { sourceSets { commonMain.dependencies { } } }`
         * block. A single `jvm()` target is declared by default; add other targets manually.
         *
         * Can also be set via `-PmultiplatformTargets=true` on the command line.
         */
        @get:Input
        @get:Optional
        public val multiplatform: Property<Boolean> = objects.property(Boolean::class.java)
    }
