package org.litote.openapi.ktor.client.generator.domain

/**
 * A single HTTP operation to be generated as a suspend method in a client class.
 *
 * @param inlineModels Additional inline models (e.g. for inline parameter types) to be
 *   generated as nested classes within the client class.
 */
public data class OperationSpec(
    /** The method name (camelCase). */
    val name: String,
    val path: String,
    val method: String,
    val parameters: List<OperationParameter>,
    val requestBody: RequestBodySpec? = null,
    val responses: List<ResponseEntry>,
    val isSse: Boolean = false,
    val summary: String? = null,
    val inlineModels: List<ModelSpec> = emptyList(),
)
