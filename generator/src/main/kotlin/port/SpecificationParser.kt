package org.litote.openapi.ktor.client.generator.port

import org.litote.openapi.ktor.client.generator.ApiGeneratorConfiguration
import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.OperationMeta

/** Outgoing port: parses an OpenAPI specification into the domain [GenerationSpec]. */
internal interface SpecificationParser {
    fun parse(
        configuration: ApiGeneratorConfiguration,
        operationFilter: (OperationMeta) -> Boolean,
    ): GenerationSpec
}
