package org.litote.openapi.ktor.client.generator.adapter.renderer

import com.squareup.kotlinpoet.FunSpec
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
    val parameterHelpers: MutableSet<ParameterSerializationHelper> = mutableSetOf(),
)

/**
 * Context containing the generated client interface, factory function, implementation class and metadata.
 *
 * @property clientClass the default implementation of [clientInterface]
 */
public data class ClientFileContext(
    val name: String,
    val operations: List<OperationSpec>,
    val hasHeaders: Boolean,
    val hasPathComponents: Boolean,
    val hasSseOperations: Boolean,
    val clientClass: TypeSpec,
    val clientInterface: TypeSpec,
    val clientFactory: FunSpec,
) {
    internal constructor(
        generationContext: ClientGenerationContext,
        clientInterface: TypeSpec,
        clientFactory: FunSpec,
        clientClass: TypeSpec,
    ) : this(
        generationContext.name,
        generationContext.operations,
        generationContext.hasHeaders,
        generationContext.hasPathComponents,
        generationContext.hasSseOperations,
        clientClass,
        clientInterface,
        clientFactory,
    )
}
