package org.litote.openapi.ktor.client.generator.port

import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.OperationMeta

/** Outgoing port: parses an OpenAPI specification into the domain [GenerationSpec]. */
public fun interface SpecificationParser {
    public fun parse(operationFilter: (OperationMeta) -> Boolean): GenerationSpec
}
