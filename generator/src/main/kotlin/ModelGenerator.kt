package org.litote.openapi.ktor.client.generator

import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SchemaOrReference

internal class ModelGenerator(
    val apiModel: ApiModel,
) {
    fun generate(): Int {
        var count = 0
        apiModel.schemas.forEach { (name, schema) ->
            if (generateSchema(name, schema)) {
                count++
            }
        }
        return count
    }

    fun generateSchema(
        name: String,
        schema: OpenAPIV3SchemaOrReference,
    ): Boolean =
        if (schema is OpenAPIV3Schema) {
            val generator = ApiModelGenerator(apiModel)
            apiModel.configuration.modules.forEach { module -> module.process(generator) }

            val typeSpec = generator.buildModel(name, schema)

            generator.writeFile(name, typeSpec)
            true
        } else {
            false
        }
}
