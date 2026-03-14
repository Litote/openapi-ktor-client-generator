package org.litote.openapi.ktor.client.generator.domain

import org.litote.openapi.ktor.client.generator.shared.sanitizeToIdentifier
import org.litote.openapi.ktor.client.generator.shared.toUpperSnakeCase

/**
 * Converts a raw string value (e.g. from an OpenAPI enum) to a valid Kotlin UPPER_SNAKE_CASE identifier.
 */
public val String.enumFieldName: String get() = sanitizeToIdentifier().toUpperSnakeCase()
