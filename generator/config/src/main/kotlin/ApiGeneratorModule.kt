package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.port.ClientGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ConfigurationGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ModelGeneratorConfig
import java.util.ServiceLoader

public interface ApiGeneratorModule {
    public val id: String get() = this::class.simpleName ?: error("Module must have a simple class name")

    public fun process(generator: ConfigurationGeneratorConfig) {
        // Module hook — no-op by default.
    }

    public fun process(generator: ClientGeneratorConfig) {
        // Module hook — no-op by default.
    }

    public fun process(generator: ModelGeneratorConfig) {
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
