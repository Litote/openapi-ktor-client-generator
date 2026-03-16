package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.TypeSpec
import org.litote.openapi.ktor.client.generator.domain.OperationSpec

/**
 * Context for client generation, tracking state during the build process.
 */
internal data class ClientGenerationContext(
    val name: String,
    val operations: List<OperationSpec>,
    var hasHeaders: Boolean = false,
    var hasPathComponents: Boolean = false,
    var hasSseOperations: Boolean = false,
)

/**
 * Context containing the generated client class and metadata.
 */
public data class ClientFileContext(
    val name: String,
    val operations: List<OperationSpec>,
    val hasHeaders: Boolean,
    val hasPathComponents: Boolean,
    val hasSseOperations: Boolean,
    val clientClass: TypeSpec,
) {
    internal constructor(generationContext: ClientGenerationContext, clientClass: TypeSpec) : this(
        generationContext.name,
        generationContext.operations,
        generationContext.hasHeaders,
        generationContext.hasPathComponents,
        generationContext.hasSseOperations,
        clientClass,
    )
}
