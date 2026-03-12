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
)
