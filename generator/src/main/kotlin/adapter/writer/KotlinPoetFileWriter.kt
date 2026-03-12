package org.litote.openapi.ktor.client.generator.adapter.writer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.litote.openapi.ktor.client.generator.domain.GeneratedFile
import org.litote.openapi.ktor.client.generator.port.FileSystemWriter
import java.io.File

internal class KotlinPoetFileWriter : FileSystemWriter {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun write(
        file: GeneratedFile,
        outputDirectory: String,
    ) {
        val packagePath = file.packageName.replace('.', File.separatorChar)
        val dir = File(outputDirectory).resolve("src/main/kotlin").resolve(packagePath)
        dir.mkdirs()
        val target = File(dir, "${file.fileName}.kt")
        logger.debug { "Writing ${file.fileName} to ${dir.path}" }
        target.writeText(file.content)
    }
}
