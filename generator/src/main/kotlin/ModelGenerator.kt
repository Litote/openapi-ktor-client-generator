package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.TypeSpec
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

    fun buildModel(
        name: String,
        schema: OpenAPIV3Schema,
        generator: ApiModelGenerator = ApiModelGenerator(apiModel),
    ): TypeSpec? {
        apiModel.configuration.modules.forEach { module -> module.process(generator) }

        return generator.buildModel(name, schema)
    }

    private fun generateSchema(
        name: String,
        schema: OpenAPIV3SchemaOrReference,
    ): Boolean =
        if (schema is OpenAPIV3Schema) {
            val generator = ApiModelGenerator(apiModel)
            val typeSpec = buildModel(name, schema, generator)

            generator.writeFile(name, typeSpec)
            true
        } else {
            false
        }
}
