package org.litote.openapi.ktor.client.generator

import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SchemaOrReference

internal class ModelGenerator(
    val apiModel: ApiModel,
) {
    fun generate() {
        apiModel.schemas.forEach { (name, schema) ->
            generateSchema(name, schema)
        }
    }

    fun generateSchema(
        name: String,
        schema: OpenAPIV3SchemaOrReference,
    ) {
        if (schema is OpenAPIV3Schema) {
            val generator = ApiModelGenerator(apiModel)
            apiModel.configuration.modules.forEach { module -> module.process(generator) }

            val typeSpec = generator.buildModel(name, schema)

            generator.writeFile(name, typeSpec)
        }
    }
}
