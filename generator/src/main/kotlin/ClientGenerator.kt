package org.litote.openapi.ktor.client.generator

internal class ClientGenerator(val apiModel: ApiModel) {

    fun generate() {
        generateClientConfiguration()
        apiModel.pathsByTags.forEach { (name, schema) ->
            generateClient(name, schema)
        }
    }

    private fun generateClientConfiguration() {
        val generator = ApiClientConfigurationGenerator(apiModel)
        apiModel.configuration.modules.forEach { module -> module.process(generator) }

        val constructor = generator.buildConstructor()
        val companion = generator.buildCompanion()
        val clientConfiguration = generator.buildClientConfiguration(constructor, companion)
        generator.writeFile(clientConfiguration)
    }

    fun generateClient(name: String, operations: List<ApiOperation>) {
        val generator = ApiClientGenerator(apiModel)
        apiModel.configuration.modules.forEach { module -> module.process(generator) }

        val file = generator.buildClient(ClientGenerationContext(name, operations))
        generator.writeFile(file)
    }
}
