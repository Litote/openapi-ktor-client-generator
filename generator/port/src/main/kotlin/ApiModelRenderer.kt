package org.litote.openapi.ktor.client.generator.port

import org.litote.openapi.ktor.client.generator.domain.ModelSpec

/** Renders a single model class to disk. */
public fun interface ApiModelRenderer {
    public fun render(spec: ModelSpec)
}
