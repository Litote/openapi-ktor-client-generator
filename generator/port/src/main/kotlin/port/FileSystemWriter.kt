package org.litote.openapi.ktor.client.generator.port

import org.litote.openapi.ktor.client.generator.domain.GeneratedFile

public interface FileSystemWriter {
    public fun write(
        file: GeneratedFile,
        outputDirectory: String,
    )
}
