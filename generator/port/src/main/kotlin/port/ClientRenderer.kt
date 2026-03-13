package org.litote.openapi.ktor.client.generator.port

import org.litote.openapi.ktor.client.generator.domain.ClientSpec

/** Renders a single client class to disk. */
public fun interface ClientRenderer {
    public fun render(spec: ClientSpec)
}
