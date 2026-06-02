package org.litote.openapi.ktor.client.generator.adapter.parser

import com.squareup.kotlinpoet.TypeName
import community.flock.kotlinx.openapi.bindings.Reference
import community.flock.kotlinx.openapi.bindings.Schema
import community.flock.kotlinx.openapi.bindings.SchemaOrReference
import org.litote.openapi.ktor.client.generator.shared.hasIllegalIdentifierChars
import org.litote.openapi.ktor.client.generator.shared.isSnakeCase
import org.litote.openapi.ktor.client.generator.shared.sanitizeToIdentifier
import org.litote.openapi.ktor.client.generator.shared.snakeToCamelCase

internal data class ApiClassProperty(
    val initialName: String,
    val type: TypeName,
    val apiModel: SchemaOrReference,
) {
    internal val isSnakeCase: Boolean = initialName.isSnakeCase()
    internal val hasIllegalChars: Boolean = initialName.hasIllegalIdentifierChars()
    internal val needsSerialName: Boolean = isSnakeCase || hasIllegalChars
    internal val camelCaseName: String = initialName.sanitizeToIdentifier().snakeToCamelCase()
    internal val asSchema: Schema? get() = apiModel as? Schema
    internal val asReference: String? get() = (apiModel as? Reference)?.refClassName
}
