package org.litote.openapi.ktor.client.generator.port

import org.litote.openapi.ktor.client.generator.domain.GenerationSpec
import org.litote.openapi.ktor.client.generator.domain.OperationMetaSpec

/** Outgoing port: parses an OpenAPI specification into the domain [GenerationSpec]. */
public fun interface ApiSpecificationParser {
    public fun parse(operationFilter: (OperationMetaSpec) -> Boolean): GenerationSpec
}
