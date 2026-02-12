package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

public abstract class ApiClientGeneratorsExtension @Inject constructor(objects: ObjectFactory) {
    public val generators: NamedDomainObjectContainer<ApiClientGenerator> =   objects.domainObjectContainer(ApiClientGenerator::class.java) {
            name -> objects.newInstance(ApiClientGenerator::class.java, objects, name)
    }
}