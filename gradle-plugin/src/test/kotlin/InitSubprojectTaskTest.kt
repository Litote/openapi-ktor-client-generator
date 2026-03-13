package org.litote.openapi.ktor.client.generator.plugin

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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
        splitByClient: Boolean? = null,
        basePackage: String? = null,
        buildScriptTemplate: String? = null,
        generatorConfigExtra: String? = null,
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
        splitByClient?.let { task.splitByClient.set(it) }
        basePackage?.let { task.basePackage.set(it) }
        buildScriptTemplate?.let { task.buildScriptTemplate.set(it) }
        generatorConfigExtra?.let { task.generatorConfigExtra.set(it) }
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

    @Test
    fun `GIVEN splitByClient=true with multi-tag spec WHEN initSubproject THEN creates shared and per-client modules`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
            )

        task.initSubproject()

        assertTrue(tempDir.resolve("shared/build.gradle.kts").exists())
        assertFalse(tempDir.resolve("settings.gradle.kts").exists(), "settings.gradle.kts should NOT be generated")
        assertFalse(tempDir.resolve("build.gradle.kts").exists(), "root build.gradle.kts should NOT exist")
        assertTrue(tempDir.resolve("src/main/openapi/multi-tag.json").exists())
    }

    @Test
    fun `GIVEN splitByClient=true with multi-tag spec WHEN initSubproject THEN client dirs are in kebab-case`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
            )

        task.initSubproject()

        // UserClient → user-client, OrderClient → order-client
        assertTrue(tempDir.resolve("user-client/build.gradle.kts").exists(), "userClient dir should exist")
        assertTrue(tempDir.resolve("order-client/build.gradle.kts").exists(), "orderClient dir should exist")
    }

    @Test
    fun `GIVEN splitByClient=true WHEN initSubproject THEN shared build gradle kts has splitByClient and basePackage`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
            )

        task.initSubproject()

        val sharedContent = tempDir.resolve("shared/build.gradle.kts").readText()
        assertContains(sharedContent, "splitByClient.set(true)")
        assertContains(sharedContent, """basePackage = "org.example.multitag"""")
    }

    @Test
    fun `GIVEN splitByClient=true WHEN initSubproject THEN client build gradle kts has targetClientName and api dependency on shared`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
            )

        task.initSubproject()

        val userClientContent = tempDir.resolve("user-client/build.gradle.kts").readText()
        assertContains(userClientContent, """targetClientName.set("UserClient")""")
        assertContains(userClientContent, """api(project(":shared"))""")
        assertContains(userClientContent, "splitByClient.set(true)")
        assertContains(userClientContent, """sharedBasePackage.set("org.example.multitag")""")
        assertContains(userClientContent, """basePackage = "org.example.multitag.user"""")
    }

    @Test
    fun `GIVEN splitByClient=true and explicit basePackage WHEN initSubproject THEN generated build files use provided basePackage`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
                basePackage = "com.acme.myapi",
            )

        task.initSubproject()

        val sharedContent = tempDir.resolve("shared/build.gradle.kts").readText()
        assertContains(sharedContent, """basePackage = "com.acme.myapi"""")

        val userClientContent = tempDir.resolve("user-client/build.gradle.kts").readText()
        assertContains(userClientContent, """basePackage = "com.acme.myapi.user"""")
    }

    @Test
    fun `GIVEN splitByClient=true and openApiFile already in src main openapi WHEN initSubproject THEN source file is preserved`() {
        val openApiDestDir = tempDir.resolve("src/main/openapi").also { it.mkdirs() }
        val openApiFile = openApiDestDir.resolve("multi-tag.json")
        val originalContent =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            ).readText()
        openApiFile.writeText(originalContent)

        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
            )

        task.initSubproject()

        assertTrue(openApiFile.exists(), "Source file should still exist")
        assertEquals(originalContent, openApiFile.readText(), "Source file content should be preserved")
    }

    @Test
    fun `GIVEN buildScriptTemplate WHEN initSubproject THEN build gradle kts uses provided template`() {
        val openApiFile = tempDir.resolve("petstore.yaml").also { it.writeText("openapi: 3.0.0") }
        val template =
            """
            plugins {
                alias(libs.plugins.kotlin.jvm)
            }
            dependencies {
                implementation(libs.ktor.client.core)
            }
            """.trimIndent()
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-client",
                buildScriptTemplate = template,
            )

        task.initSubproject()

        val content = tempDir.resolve("my-client/build.gradle.kts").readText()
        assertContains(content, "alias(libs.plugins.kotlin.jvm)")
        assertContains(content, "implementation(libs.ktor.client.core)")
        assertContains(content, "apiClientGenerator {")
        // auto-generated versions should NOT be present
        assertFalse(content.contains("""kotlin("jvm") version"""), "Should not contain generated plugins block")
    }

    @Test
    fun `GIVEN generatorConfigExtra WHEN initSubproject THEN build gradle kts contains extra config`() {
        val openApiFile = tempDir.resolve("petstore.yaml").also { it.writeText("openapi: 3.0.0") }
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-client",
                generatorConfigExtra = """modulesIds.add("UnknownEnumValueModule")""",
            )

        task.initSubproject()

        val content = tempDir.resolve("my-client/build.gradle.kts").readText()
        assertContains(content, """modulesIds.add("UnknownEnumValueModule")""")
        assertContains(content, """openApiFile = file("src/main/openapi/petstore.yaml")""")
    }

    @Test
    fun `GIVEN buildScriptTemplate WHEN splitByClient=true THEN shared build gradle kts uses template`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val template = "plugins { alias(libs.plugins.kotlin.jvm) }"
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                buildScriptTemplate = template,
            )

        task.initSubproject()

        val sharedContent = tempDir.resolve("shared/build.gradle.kts").readText()
        assertContains(sharedContent, "alias(libs.plugins.kotlin.jvm)")
        assertContains(sharedContent, "splitByClient.set(true)")
        assertFalse(sharedContent.contains("""kotlin("jvm") version"""), "Should not contain generated plugins block")
    }

    @Test
    fun `GIVEN buildScriptTemplate WHEN splitByClient=true THEN client build gradle kts uses template and keeps api project shared`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val template = "plugins { alias(libs.plugins.kotlin.jvm) }"
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                buildScriptTemplate = template,
            )

        task.initSubproject()

        val clientContent = tempDir.resolve("user-client/build.gradle.kts").readText()
        assertContains(clientContent, "alias(libs.plugins.kotlin.jvm)")
        assertContains(clientContent, """api(project(":shared"))""")
        assertContains(clientContent, """targetClientName.set("UserClient")""")
        assertFalse(clientContent.contains("""kotlin("jvm") version"""), "Should not contain generated plugins block")
    }

    @Test
    fun `GIVEN generatorConfigExtra WHEN splitByClient=true THEN shared and client builds contain extra config`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                generatorConfigExtra = """modulesIds.add("LoggingSl4jModule")""",
            )

        task.initSubproject()

        val sharedContent = tempDir.resolve("shared/build.gradle.kts").readText()
        assertContains(sharedContent, """modulesIds.add("LoggingSl4jModule")""")

        val clientContent = tempDir.resolve("user-client/build.gradle.kts").readText()
        assertContains(clientContent, """modulesIds.add("LoggingSl4jModule")""")
    }
}

internal class GeneratorPluginTest {
    @TempDir
    lateinit var rootDir: File

    @Test
    fun `GIVEN plugin applied to root project WHEN tasks listed THEN initApiClientSubproject is registered`() {
        val rootProject =
            ProjectBuilder
                .builder()
                .withProjectDir(rootDir)
                .build()
        rootProject.plugins.apply(GeneratorPlugin::class.java)

        assertTrue(
            rootProject.tasks.names.contains("initApiClientSubproject"),
            "initApiClientSubproject should be registered on the root project",
        )
    }

    @Test
    fun `GIVEN plugin applied to subproject WHEN tasks listed THEN initApiClientSubproject is NOT registered`() {
        val rootProject =
            ProjectBuilder
                .builder()
                .withProjectDir(rootDir)
                .build()
        val subprojectDir = rootDir.resolve("shared").also { it.mkdirs() }
        val subproject =
            ProjectBuilder
                .builder()
                .withParent(rootProject)
                .withName("shared")
                .withProjectDir(subprojectDir)
                .build()
        subproject.plugins.apply(GeneratorPlugin::class.java)

        assertFalse(
            subproject.tasks.names.contains("initApiClientSubproject"),
            "initApiClientSubproject must NOT be registered on a subproject — it would overwrite generated files without templates",
        )
    }
}
