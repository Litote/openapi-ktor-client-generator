package org.litote.openapi.ktor.client.generator.port

import org.litote.openapi.ktor.client.generator.domain.GeneratedFileSpec

public fun interface ApiFileSystemWriter {
    public fun write(
        file: GeneratedFileSpec,
        outputDirectory: String,
    )
}
