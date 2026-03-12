package org.litote.openapi.ktor.client.generator.application

import org.litote.openapi.ktor.client.generator.GenerationResult
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.port.ClientRenderer
import org.litote.openapi.ktor.client.generator.port.ConfigurationRenderer
import org.litote.openapi.ktor.client.generator.port.ModelRenderer

internal class GenerateCodeService(
    private val configurationRenderer: ConfigurationRenderer,
    private val clientRenderer: ClientRenderer,
    private val modelRenderer: ModelRenderer,
) {
    fun generate(spec: GenerationSpec): GenerationResult {
        configurationRenderer.render()
        val clientsGenerated = spec.clients.onEach { clientRenderer.render(it) }.size
        val modelsGenerated = spec.models.onEach { modelRenderer.render(it) }.size
        return GenerationResult.Success(clientsGenerated, modelsGenerated)
    }
}
