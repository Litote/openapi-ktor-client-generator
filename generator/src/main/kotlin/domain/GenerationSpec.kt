package org.litote.openapi.ktor.client.generator.domain

/**
 * Top-level specification of everything to generate from an OpenAPI spec.
 *
 * This is the central domain object: it contains a complete description of the clients and models
 * that the generator will produce, expressed in pure Kotlin domain types with no dependency on
 * OpenAPI bindings or KotlinPoet.
 */
internal data class GenerationSpec(
    val clientConfiguration: ClientConfigurationSpec,
    val clients: List<ClientSpec>,
    val models: List<ModelSpec>,
)
