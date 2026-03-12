package org.litote.openapi.ktor.client.generator.port

import org.litote.openapi.ktor.client.generator.domain.GeneratedFile

internal interface FileSystemWriter {
    fun write(
        file: GeneratedFile,
        outputDirectory: String,
    )
}
