package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.port.ApiClientGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ApiModelGeneratorConfig
import java.util.ServiceLoader

public interface ApiGeneratorModule {
    public val id: String get() = this::class.simpleName ?: error("Module must have a simple class name")

    public fun process(generator: ApiConfigurationGeneratorConfig) {
        // Module hook — no-op by default.
    }

    public fun process(generator: ApiClientGeneratorConfig) {
        // Module hook — no-op by default.
    }

    public fun process(generator: ApiModelGeneratorConfig) {
        // Module hook — no-op by default.
    }

    public companion object {
        private val modules: Map<String, ApiGeneratorModule> by lazy {
            ServiceLoader
                .load(ApiGeneratorModule::class.java)
                .associateBy { it.id }
        }

        public fun getModule(id: String): ApiGeneratorModule? = modules[id]
    }
}
