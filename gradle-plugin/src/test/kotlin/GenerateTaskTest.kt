package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.domain.GeneratedFileSpec
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

internal class GenerateTaskTest {
    @TempDir
    lateinit var tempDir: File

    private fun buildTask(skip: Boolean = true): GenerateTask {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val task = project.tasks.register("generateTest", GenerateTask::class.java).get()
        task.openApiFile.set(File("src/test/resources/openapi.json"))
        task.outputDirectory.set(tempDir)
        task.basePackage.set("com.example")
        task.skip.set(skip)
        task.splitByClient.set(false)
        task.splitGranularity.set("BY_TAG")
        task.sharedModelGranularity.set("SHARED_ALL")
        task.allowedPaths.set(emptySet<String>())
        task.additionalSharedGroupPackages.set(emptyMap<String, String>())
        return task
    }

    @Test
    fun `GIVEN unknown module id WHEN generating THEN warning is logged and task succeeds`() {
        val task = buildTask()
        task.modulesIds.add("NonExistentModule")

        // Should not throw — unknown modules are skipped with a warning
        task.generate()
    }

    @Test
    fun `GIVEN customModules WHEN generating THEN custom module transforms are applied`() {
        val specFile =
            tempDir.resolve("openapi.json").also {
                it.writeText(
                    """
                    {
                      "openapi": "3.0.0",
                      "info": { "title": "Test", "version": "1.0" },
                      "paths": {
                        "/items": {
                          "get": {
                            "tags": ["Items"],
                            "operationId": "listItems",
                            "responses": { "200": { "description": "ok" } }
                          }
                        }
                      }
                    }
                    """.trimIndent(),
                )
            }
        val outputDir = Files.createTempDirectory("generate-task-custom-module-test").toFile()
        try {
            val headerComment = "// custom module header"
            val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
            val task = project.tasks.register("generateCustom", GenerateTask::class.java).get()
            task.openApiFile.set(specFile)
            task.outputDirectory.set(outputDir)
            task.basePackage.set("com.example")
            task.skip.set(false)
            task.splitByClient.set(false)
            task.splitGranularity.set("BY_TAG")
            task.sharedModelGranularity.set("SHARED_ALL")
            task.allowedPaths.set(emptySet<String>())
            task.additionalSharedGroupPackages.set(emptyMap<String, String>())
            task.customModules.add(
                object : ApiGeneratorModule {
                    override fun transformFile(file: GeneratedFileSpec): GeneratedFileSpec =
                        file.copy(content = "$headerComment\n" + file.content)
                },
            )

            task.generate()

            val generatedFiles = outputDir.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
            assertTrue(generatedFiles.isNotEmpty(), "Should generate at least one file")
            assertTrue(
                generatedFiles.all { it.readText().startsWith(headerComment) },
                "All generated files should start with the custom module header",
            )
        } finally {
            outputDir.deleteRecursively()
        }
    }
}
