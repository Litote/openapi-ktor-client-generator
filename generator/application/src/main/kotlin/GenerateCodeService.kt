package org.litote.openapi.ktor.client.generator.application

import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.port.ApiClientRenderer
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationRenderer
import org.litote.openapi.ktor.client.generator.port.ApiModelRenderer

public class GenerateCodeService(
    private val configurationRenderer: ApiConfigurationRenderer,
    private val clientRenderer: ApiClientRenderer,
    private val modelRenderer: ApiModelRenderer,
) {
    public fun generate(spec: GenerationSpec): Pair<Int, Int> {
        configurationRenderer.render()
        val clientsGenerated = spec.clients.onEach { clientRenderer.render(it) }.size
        val modelsGenerated = spec.models.onEach { modelRenderer.render(it) }.size
        return Pair(clientsGenerated, modelsGenerated)
    }
}
