package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class InitSubprojectTaskTest {
    @TempDir
    lateinit var tempDir: File

    private fun buildTask(
        openApiFile: String? = null,
        subprojectName: String? = null,
        kotlinVersion: String = DEFAULT_KOTLIN_VERSION,
        ktorVersion: String = DEFAULT_KTOR_VERSION,
        coroutinesVersion: String = DEFAULT_COROUTINES_VERSION,
        serializationVersion: String = DEFAULT_SERIALIZATION_VERSION,
    ): InitSubprojectTask {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val task =
            project.tasks
                .register("initApiClientSubproject", InitSubprojectTask::class.java)
                .get()
        openApiFile?.let { task.openApiFilePath.set(it) }
        subprojectName?.let { task.subprojectName.set(it) }
        task.rootDirectory.set(tempDir)
        task.kotlinVersion.set(kotlinVersion)
        task.ktorVersion.set(ktorVersion)
        task.coroutinesVersion.set(coroutinesVersion)
        task.serializationVersion.set(serializationVersion)
        return task
    }

    @Test
    fun `GIVEN missing openApiFile property WHEN initSubproject THEN throws GradleException`() {
        val task = buildTask(subprojectName = "my-client")

        assertFailsWith<GradleException> { task.initSubproject() }
    }

    @Test
    fun `GIVEN missing subprojectName property WHEN initSubproject THEN uses openapi filename as subproject name`() {
        val openApiFile = tempDir.resolve("petstore.yaml").also { it.writeText("openapi: 3.0.0") }
        val task = buildTask(openApiFile = openApiFile.absolutePath)

        task.initSubproject()

        assertTrue(tempDir.resolve("petstore/build.gradle.kts").exists())
        assertTrue(tempDir.resolve("petstore/src/main/openapi/petstore.yaml").exists())
    }

    @Test
    fun `GIVEN non-existent openApiFile WHEN initSubproject THEN throws GradleException`() {
        val task =
            buildTask(
                openApiFile = tempDir.resolve("missing.yaml").absolutePath,
                subprojectName = "my-client",
            )

        assertFailsWith<GradleException> { task.initSubproject() }
    }

    @Test
    fun `GIVEN valid inputs WHEN initSubproject THEN creates build gradle kts`() {
        val openApiFile = tempDir.resolve("petstore.yaml").also { it.writeText("openapi: 3.0.0") }
        val task = buildTask(openApiFile = openApiFile.absolutePath, subprojectName = "my-client")

        task.initSubproject()

        val buildFile = tempDir.resolve("my-client/build.gradle.kts")
        assertTrue(buildFile.exists(), "build.gradle.kts should exist")
    }

    @Test
    fun `GIVEN valid inputs WHEN initSubproject THEN copies openapi file to src main openapi`() {
        val openApiFile = tempDir.resolve("petstore.yaml").also { it.writeText("openapi: 3.0.0") }
        val task = buildTask(openApiFile = openApiFile.absolutePath, subprojectName = "my-client")

        task.initSubproject()

        val copiedFile = tempDir.resolve("my-client/src/main/openapi/petstore.yaml")
        assertTrue(copiedFile.exists(), "OpenAPI file should be copied to src/main/openapi/")
        assertEquals("openapi: 3.0.0", copiedFile.readText())
    }

    @Test
    fun `GIVEN valid inputs WHEN initSubproject THEN build gradle kts contains correct plugin id and versions`() {
        val openApiFile = tempDir.resolve("myapi.json").also { it.writeText("{}") }
        val task = buildTask(openApiFile = openApiFile.absolutePath, subprojectName = "my-client")

        task.initSubproject()

        val content = tempDir.resolve("my-client/build.gradle.kts").readText()
        assertContains(content, """id("${InitSubprojectTask.PLUGIN_ID}") version "$PLUGIN_VERSION"""")
        assertContains(content, """kotlin("jvm") version "${DEFAULT_KOTLIN_VERSION}"""")
        assertContains(content, """kotlin("plugin.serialization") version "${DEFAULT_KOTLIN_VERSION}"""")
    }

    @Test
    fun `GIVEN valid inputs WHEN initSubproject THEN build gradle kts contains all ktor dependencies`() {
        val openApiFile = tempDir.resolve("myapi.json").also { it.writeText("{}") }
        val task = buildTask(openApiFile = openApiFile.absolutePath, subprojectName = "my-client")

        task.initSubproject()

        val content = tempDir.resolve("my-client/build.gradle.kts").readText()
        assertContains(content, "io.ktor:ktor-client-cio:${DEFAULT_KTOR_VERSION}")
        assertContains(content, "io.ktor:ktor-client-content-negotiation:${DEFAULT_KTOR_VERSION}")
        assertContains(content, "io.ktor:ktor-client-core:${DEFAULT_KTOR_VERSION}")
        assertContains(content, "io.ktor:ktor-serialization-kotlinx-json:${DEFAULT_KTOR_VERSION}")
        assertContains(content, "io.ktor:ktor-client-logging:${DEFAULT_KTOR_VERSION}")
        assertContains(content, "org.jetbrains.kotlinx:kotlinx-serialization-json:${DEFAULT_SERIALIZATION_VERSION}")
        assertContains(content, "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DEFAULT_COROUTINES_VERSION}")
    }

    @Test
    fun `GIVEN custom versions WHEN initSubproject THEN build gradle kts uses provided versions`() {
        val openApiFile = tempDir.resolve("myapi.json").also { it.writeText("{}") }
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-client",
                kotlinVersion = "2.0.0",
                ktorVersion = "2.3.12",
                coroutinesVersion = "1.7.3",
                serializationVersion = "1.6.3",
            )

        task.initSubproject()

        val content = tempDir.resolve("my-client/build.gradle.kts").readText()
        assertContains(content, """kotlin("jvm") version "2.0.0"""")
        assertContains(content, "io.ktor:ktor-client-core:2.3.12")
        assertContains(content, "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
        assertContains(content, "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    }

    @Test
    fun `GIVEN valid inputs WHEN initSubproject THEN build gradle kts references generator name from filename`() {
        val openApiFile = tempDir.resolve("petstore.yaml").also { it.writeText("openapi: 3.0.0") }
        val task = buildTask(openApiFile = openApiFile.absolutePath, subprojectName = "my-client")

        task.initSubproject()

        val content = tempDir.resolve("my-client/build.gradle.kts").readText()
        assertContains(content, """create("petstore")""")
        assertContains(content, """openApiFile = file("src/main/openapi/petstore.yaml")""")
    }

    @Test
    fun `GIVEN relative openApiFile path WHEN initSubproject THEN resolves relative to project root`() {
        tempDir.resolve("specs/myapi.yaml").also {
            it.parentFile.mkdirs()
            it.writeText("openapi: 3.0.0")
        }
        val task = buildTask(openApiFile = "specs/myapi.yaml", subprojectName = "client")

        task.initSubproject()

        assertTrue(tempDir.resolve("client/src/main/openapi/myapi.yaml").exists())
    }
}
