package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.domain.OperationMeta

public data class ApiGeneratorConfiguration(
    val openApiFile: String = "src/main/openapi/openapi.json",
    val outputDirectory: String = openApiFile.substring(openApiFile.lastIndexOf('/'), openApiFile.lastIndexOf('.')),
    val basePackage: String = "org.example",
    val operationFilter: (OperationMeta) -> Boolean = { true },
    val modelPackage: String = "$basePackage.model",
    val clientPackage: String = "$basePackage.client",
    val modules: List<ApiGeneratorModule> = emptyList(),
    val splitByClient: Boolean = false,
    val targetClientName: String? = null,
    /**
     * Base package of the shared module. When set, `ClientConfiguration` is imported from
     * `sharedBasePackage.client` instead of `basePackage.client`. Use this when a client submodule
     * has a distinct [basePackage] (e.g. `com.example.api.user`) but `ClientConfiguration` lives in
     * the shared module's package (e.g. `com.example.api`).
     */
    val sharedBasePackage: String? = null,
) {
    /** Package used to reference `ClientConfiguration` — the shared module's client package when set. */
    val configPackage: String = "${sharedBasePackage ?: basePackage}.client"

    /**
     * Package used for all model type references and model file generation.
     * When [sharedBasePackage] is set, all models (shared and private) use the shared model package
     * so that cross-module references resolve correctly.
     */
    val resolvedModelPackage: String = "${sharedBasePackage ?: basePackage}.model"
}
