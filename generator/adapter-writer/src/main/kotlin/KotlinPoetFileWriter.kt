package org.litote.openapi.ktor.client.generator.adapter.writer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.litote.openapi.ktor.client.generator.domain.GeneratedFileSpec
import org.litote.openapi.ktor.client.generator.port.ApiFileSystemWriter
import java.io.File

public class KotlinPoetFileWriter : ApiFileSystemWriter {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    public override fun write(
        file: GeneratedFileSpec,
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
