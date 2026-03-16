package org.litote.openapi.ktor.client.generator.domain

/** Specification for the generated `ClientConfiguration` class. */
public data class ClientConfigurationSpec(
    val serverUrl: String,
    val apiKeySchemes: List<SecuritySchemeSpec>,
    val componentParameters: List<ComponentParameterSpec>,
    val hasYamlContentType: Boolean = false,
)
