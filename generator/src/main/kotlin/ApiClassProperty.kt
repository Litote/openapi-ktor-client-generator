package org.litote.openapi.ktor.client.generator

import com.squareup.kotlinpoet.TypeName
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Reference
import community.flock.kotlinx.openapi.bindings.OpenAPIV3Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV3SchemaOrReference
import org.litote.openapi.ktor.client.generator.shared.hasIllegalIdentifierChars
import org.litote.openapi.ktor.client.generator.shared.isSnakeCase
import org.litote.openapi.ktor.client.generator.shared.sanitizeToIdentifier
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase

public data class ApiClassProperty(
    val initialName: String,
    val type: TypeName,
    val apiModel: OpenAPIV3SchemaOrReference,
) {
    internal val isSnakeCase: Boolean = initialName.isSnakeCase()
    internal val hasIllegalChars: Boolean = initialName.hasIllegalIdentifierChars()
    internal val needsSerialName: Boolean = isSnakeCase || hasIllegalChars
    internal val camelCaseName: String = initialName.sanitizeToIdentifier().snakeToCamelCase()
    internal val asSchema: OpenAPIV3Schema? get() = apiModel as? OpenAPIV3Schema
    internal val asReference: String? get() = (apiModel as? OpenAPIV3Reference)?.ref?.value
}
