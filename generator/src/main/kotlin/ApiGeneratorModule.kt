package org.litote.openapi.ktor.client.generator

import java.util.ServiceLoader

public interface ApiGeneratorModule {

    public val id: String get() = this::class.simpleName ?: error("Module must have a simple class name")

    public fun process(generator: ApiClientConfigurationGenerator) {}

    public fun process(generator: ApiClientGenerator) {}

    public fun process(generator: ApiModelGenerator) {}

    public companion object {
        private val modules: Map<String, ApiGeneratorModule> by lazy {
            ServiceLoader
                .load(ApiGeneratorModule::class.java)
                .associateBy { it.id }
        }

        public fun getModule(id: String): ApiGeneratorModule? = modules[id]
    }
}