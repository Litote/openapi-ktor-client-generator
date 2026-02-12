package org.litote.openapi.ktor.client.generator


public data class ApiGeneratorConfiguration(
    val openApiFile: String = "src/main/openapi/openapi.json",
    val outputDirectory: String = openApiFile.substring(openApiFile.lastIndexOf('/'), openApiFile.lastIndexOf('.')),
    val basePackage: String = "org.example",
    val operationFilter: (ApiOperation) -> Boolean = { true },
    val modelPackage: String = "$basePackage.model",
    val clientPackage: String = "$basePackage.client",
    val modules: List<ApiGeneratorModule> = emptyList()
)
