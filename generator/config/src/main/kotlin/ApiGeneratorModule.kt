package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.domain.ClientSpec
import org.litote.openapi.ktor.client.generator.domain.GeneratedFileSpec
import org.litote.openapi.ktor.client.generator.domain.ModelSpec
import org.litote.openapi.ktor.client.generator.port.ApiClientGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig
import org.litote.openapi.ktor.client.generator.port.ApiModelGeneratorConfig
import java.util.ServiceLoader

public interface ApiGeneratorModule {
    public val id: String get() = this::class.simpleName ?: error("Module must have a simple class name")

    public fun processConfiguration(generator: ApiConfigurationGeneratorConfig) {
        // Module hook — no-op by default.
    }

    public fun processClient(generator: ApiClientGeneratorConfig) {
        // Module hook — no-op by default.
    }

    public fun processModel(generator: ApiModelGeneratorConfig) {
        // Module hook — no-op by default.
    }

    public fun transformClientSpec(spec: ClientSpec): ClientSpec = spec

    public fun transformModelSpec(spec: ModelSpec): ModelSpec = spec

    public fun transformFile(file: GeneratedFileSpec): GeneratedFileSpec = file

    public companion object {
        private val modules: Map<String, ApiGeneratorModule> by lazy {
            ServiceLoader
                .load(ApiGeneratorModule::class.java)
                .associateBy { it.id }
        }

        public fun getModule(id: String): ApiGeneratorModule? = modules[id]
    }
}
