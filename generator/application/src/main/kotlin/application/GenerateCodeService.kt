package org.litote.openapi.ktor.client.generator.application

import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.port.ClientRenderer
import org.litote.openapi.ktor.client.generator.port.ConfigurationRenderer
import org.litote.openapi.ktor.client.generator.port.ModelRenderer

public class GenerateCodeService(
    private val configurationRenderer: ConfigurationRenderer,
    private val clientRenderer: ClientRenderer,
    private val modelRenderer: ModelRenderer,
) {
    public fun generate(spec: GenerationSpec): Pair<Int, Int> {
        configurationRenderer.render()
        val clientsGenerated = spec.clients.onEach { clientRenderer.render(it) }.size
        val modelsGenerated = spec.models.onEach { modelRenderer.render(it) }.size
        return Pair(clientsGenerated, modelsGenerated)
    }
}
