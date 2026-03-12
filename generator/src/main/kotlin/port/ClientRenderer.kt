package org.litote.openapi.ktor.client.generator.port

import org.litote.openapi.ktor.client.generator.domain.ClientSpec

/** Renders a single client class to disk. */
internal fun interface ClientRenderer {
    fun render(spec: ClientSpec)
}
