package org.litote.openapi.ktor.client.generator.adapter.parser

import com.squareup.kotlinpoet.TypeName
import community.flock.kotlinx.openapi.bindings.OpenAPIV30Reference
import community.flock.kotlinx.openapi.bindings.OpenAPIV30Schema
import community.flock.kotlinx.openapi.bindings.OpenAPIV30SchemaOrReference
import org.litote.openapi.ktor.client.generator.shared.hasIllegalIdentifierChars
import org.litote.openapi.ktor.client.generator.shared.isSnakeCase
import org.litote.openapi.ktor.client.generator.shared.sanitizeToIdentifier
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase

internal data class ApiClassProperty(
    val initialName: String,
    val type: TypeName,
    val apiModel: OpenAPIV30SchemaOrReference,
) {
    internal val isSnakeCase: Boolean = initialName.isSnakeCase()
    internal val hasIllegalChars: Boolean = initialName.hasIllegalIdentifierChars()
    internal val needsSerialName: Boolean = isSnakeCase || hasIllegalChars
    internal val camelCaseName: String = initialName.sanitizeToIdentifier().snakeToCamelCase()
    internal val asSchema: OpenAPIV30Schema? get() = apiModel as? OpenAPIV30Schema
    internal val asReference: String? get() = (apiModel as? OpenAPIV30Reference)?.ref?.value
}
