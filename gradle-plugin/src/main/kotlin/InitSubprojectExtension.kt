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
    }
