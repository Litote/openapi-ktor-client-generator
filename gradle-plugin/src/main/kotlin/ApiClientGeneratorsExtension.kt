package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import javax.inject.Inject

public abstract class ApiClientGeneratorsExtension
    @Inject
    constructor(
        objects: ObjectFactory,
    ) {
        public val generators: NamedDomainObjectContainer<ApiClientGenerator> =
            objects.domainObjectContainer(ApiClientGenerator::class.java) { name ->
                objects.newInstance(ApiClientGenerator::class.java, objects, name)
            }

        @get:Input
        public val skip: Property<Boolean> = objects.property(Boolean::class.java)

        init {
            skip.convention(false)
        }
    }
