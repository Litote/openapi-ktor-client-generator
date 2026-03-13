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
        splitGranularity: String? = null,
        sharedModelGranularity: String? = null,
        subprojectRootDirectory: String? = null,
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
        splitGranularity?.let { task.splitGranularity.set(it) }
        sharedModelGranularity?.let { task.sharedModelGranularity.set(it) }
        subprojectRootDirectory?.let { task.subprojectRootDirectory.set(it) }
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
        assertTrue(tempDir.resolve("settings.gradle.kts").exists(), "settings.gradle.kts should be generated")
        assertContains(
            tempDir.resolve("settings.gradle.kts").readText(),
            "include(\"shared\",",
            message = "settings.gradle.kts should contain include for shared module",
        )
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

    @Test
    fun `GIVEN splitGranularity=BY_TAG_AND_PATH WHEN initSubproject THEN client build references splitGranularity`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                splitGranularity = "BY_TAG_AND_PATH",
            )

        task.initSubproject()

        val sharedContent = tempDir.resolve("shared/build.gradle.kts").readText()
        assertContains(sharedContent, """splitGranularity.set("BY_TAG_AND_PATH")""")

        // BY_TAG_AND_PATH: User tag + /users path → UserUsersClient → user-users-client dir
        val userClientDir = tempDir.resolve("user-users-client")
        assertTrue(userClientDir.exists(), "Expected user-users-client dir, found: ${tempDir.listFiles()?.map { it.name }}")
        val clientContent = userClientDir.resolve("build.gradle.kts").readText()
        assertContains(clientContent, """splitGranularity.set("BY_TAG_AND_PATH")""")
    }

    @Test
    fun `GIVEN splitGranularity=BY_TAG_AND_OPERATION WHEN initSubproject THEN client build references splitGranularity`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                splitGranularity = "BY_TAG_AND_OPERATION",
            )

        task.initSubproject()

        val sharedContent = tempDir.resolve("shared/build.gradle.kts").readText()
        assertContains(sharedContent, """splitGranularity.set("BY_TAG_AND_OPERATION")""")
    }

    @Test
    fun `GIVEN BY_TAG splitGranularity (default) WHEN initSubproject THEN build files do NOT contain splitGranularity`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                splitGranularity = "BY_TAG",
            )

        task.initSubproject()

        val sharedContent = tempDir.resolve("shared/build.gradle.kts").readText()
        assertFalse(sharedContent.contains("splitGranularity"), "BY_TAG is default, should not appear in build file")
    }

    @Test
    fun `GIVEN sharedModelGranularity=SHARED_PER_GROUP WHEN initSubproject THEN creates per-group shared subprojects`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("shared-model-granularity.json"),
                ) { "shared-model-granularity.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                sharedModelGranularity = "SHARED_PER_GROUP",
            )

        task.initSubproject()

        assertTrue(tempDir.resolve("shared/build.gradle.kts").exists(), "Global shared should exist")
        // Address shared by User+Order → shared-order-user
        assertTrue(tempDir.resolve("shared-order-user/build.gradle.kts").exists(), "shared-order-user should exist")
        // Category shared by Order+Product → shared-order-product
        assertTrue(tempDir.resolve("shared-order-product/build.gradle.kts").exists(), "shared-order-product should exist")
    }

    @Test
    fun `GIVEN sharedModelGranularity=SHARED_PER_GROUP WHEN initSubproject THEN per-group build has targetSharedGroup`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("shared-model-granularity.json"),
                ) { "shared-model-granularity.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                sharedModelGranularity = "SHARED_PER_GROUP",
            )

        task.initSubproject()

        val sharedOrderUserContent = tempDir.resolve("shared-order-user/build.gradle.kts").readText()
        assertContains(sharedOrderUserContent, "targetSharedGroup")
        assertContains(sharedOrderUserContent, "OrderClient")
        assertContains(sharedOrderUserContent, "UserClient")
        assertContains(sharedOrderUserContent, "sharedModelGranularity.set(\"SHARED_PER_GROUP\")")
    }

    @Test
    fun `GIVEN sharedModelGranularity=SHARED_PER_GROUP WHEN initSubproject THEN client build declares per-group shared dependencies`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("shared-model-granularity.json"),
                ) { "shared-model-granularity.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                sharedModelGranularity = "SHARED_PER_GROUP",
            )

        task.initSubproject()

        // UserClient uses Address (shared-order-user) but not Category (shared-order-product)
        val userClientContent = tempDir.resolve("user-client/build.gradle.kts").readText()
        assertContains(userClientContent, """api(project(":shared"))""")
        assertContains(userClientContent, """api(project(":shared-order-user"))""")
        assertFalse(
            userClientContent.contains("""api(project(":shared-order-product"))"""),
            "UserClient should not depend on shared-order-product",
        )

        // OrderClient uses both Address and Category
        val orderClientContent = tempDir.resolve("order-client/build.gradle.kts").readText()
        assertContains(orderClientContent, """api(project(":shared-order-user"))""")
        assertContains(orderClientContent, """api(project(":shared-order-product"))""")
    }

    @Test
    fun `GIVEN sharedModelGranularity=SHARED_ALL (default) WHEN initSubproject THEN no per-group shared subprojects created`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("shared-model-granularity.json"),
                ) { "shared-model-granularity.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                sharedModelGranularity = "SHARED_ALL",
            )

        task.initSubproject()

        assertTrue(tempDir.resolve("shared/build.gradle.kts").exists(), "Global shared should exist")
        assertFalse(tempDir.resolve("shared-order-user/build.gradle.kts").exists(), "No per-group shared should be created")
    }

    @Test
    fun `GIVEN sharedModelGranularity=SHARED_PER_GROUP WHEN initSubproject THEN per-group build declares api dependency on shared`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("shared-model-granularity.json"),
                ) { "shared-model-granularity.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                sharedModelGranularity = "SHARED_PER_GROUP",
            )

        task.initSubproject()

        // Per-group subprojects reference global shared models, so they must depend on ":shared"
        val sharedOrderUserContent = tempDir.resolve("shared-order-user/build.gradle.kts").readText()
        assertTrue(
            sharedOrderUserContent.contains("""api(project(":shared"))"""),
            "shared-order-user must declare api(project(\":shared\")) to resolve global model types",
        )
        val sharedOrderProductContent = tempDir.resolve("shared-order-product/build.gradle.kts").readText()
        assertTrue(
            sharedOrderProductContent.contains("""api(project(":shared"))"""),
            "shared-order-product must declare api(project(\":shared\")) to resolve global model types",
        )
    }

    @Test
    fun `GIVEN buildSharedGroupGradleKtsContent without otherGroups THEN contains api shared dependency`() {
        val content =
            InitSubprojectTask.buildSharedGroupGradleKtsContent(
                specNameWithoutExt = "myapi",
                specRelativePath = "../myapi.json",
                basePackage = "com.example.sharedOrderUser",
                topBasePackage = "com.example",
                targetSharedGroup = "OrderClient,UserClient",
                kotlinVersion = "2.0.0",
                ktorVersion = "3.0.0",
                coroutinesVersion = "1.8.0",
                serializationVersion = "1.6.0",
            )
        assertContains(content, """api(project(":shared"))""")
    }

    @Test
    fun `GIVEN buildSharedGroupGradleKtsContent with otherGroups THEN does NOT add cross-group project dependencies`() {
        // Per-group subprojects must NOT declare api(project(":other-group")) dependencies —
        // that would create circular Gradle task dependencies between groups that share a client.
        // additionalSharedGroupPackages is for generator import resolution only, not for Gradle deps.
        val content =
            InitSubprojectTask.buildSharedGroupGradleKtsContent(
                specNameWithoutExt = "myapi",
                specRelativePath = "../myapi.json",
                basePackage = "com.example.sharedOrderUser",
                topBasePackage = "com.example",
                targetSharedGroup = "OrderClient,UserClient",
                additionalSharedGroupPackages =
                    mapOf("OrderClient,ProductClient" to "com.example.sharedOrderProduct"),
                kotlinVersion = "2.0.0",
                ktorVersion = "3.0.0",
                coroutinesVersion = "1.8.0",
                serializationVersion = "1.6.0",
            )
        assertContains(content, """api(project(":shared"))""")
        assertFalse(
            content.contains("""api(project(":shared-order-product"))"""),
            "Per-group subproject must NOT depend on other per-group subprojects (circular dependency risk)",
        )
    }

    @Test
    fun `GIVEN SHARED_PER_GROUP with multiple groups WHEN initSubproject THEN no cross-group project dependencies`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("shared-model-granularity.json"),
                ) { "shared-model-granularity.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                sharedModelGranularity = "SHARED_PER_GROUP",
            )

        task.initSubproject()

        // shared-order-user must NOT depend on shared-order-product and vice-versa —
        // both have OrderClient in common, so cross-deps would be circular.
        val sharedOrderUserContent = tempDir.resolve("shared-order-user/build.gradle.kts").readText()
        assertFalse(
            sharedOrderUserContent.contains("""api(project(":shared-order-product"))"""),
            "shared-order-user must not depend on shared-order-product (circular dependency)",
        )
        val sharedOrderProductContent = tempDir.resolve("shared-order-product/build.gradle.kts").readText()
        assertFalse(
            sharedOrderProductContent.contains("""api(project(":shared-order-user"))"""),
            "shared-order-product must not depend on shared-order-user (circular dependency)",
        )
    }

    @Test
    fun `GIVEN SHARED_PER_GROUP where group A models reference group B models WHEN initSubproject THEN group A depends on group B`() {
        // cross-group-dep.json: SharedAB (group {Alpha,Beta}) has property of type SharedABG (group {Alpha,Beta,Gamma})
        // → shared-alpha-client-beta-client must depend on shared-alpha-client-beta-client-gamma-client
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("cross-group-dep.json"),
                ) { "cross-group-dep.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                splitByClient = true,
                sharedModelGranularity = "SHARED_PER_GROUP",
            )

        task.initSubproject()

        val groupAB = tempDir.resolve("shared-alpha-beta/build.gradle.kts").readText()
        val groupABG = tempDir.resolve("shared-alpha-beta-gamma/build.gradle.kts").readText()

        // SharedAB references SharedABG → group {Alpha,Beta} needs compile dep on group {Alpha,Beta,Gamma}
        assertTrue(
            groupAB.contains("""api(project(":shared-alpha-beta-gamma"))"""),
            "shared-alpha-beta must depend on shared-alpha-beta-gamma",
        )
        // SharedABG does NOT reference SharedAB → no reverse dependency (no cycle)
        assertFalse(
            groupABG.contains("""api(project(":shared-alpha-beta"))"""),
            "shared-alpha-beta-gamma must NOT depend on shared-alpha-beta",
        )
    }

    @Test
    fun `GIVEN buildSharedGroupGradleKtsContent with directGroupDeps THEN deps are included in build file`() {
        val content =
            InitSubprojectTask.buildSharedGroupGradleKtsContent(
                specNameWithoutExt = "myapi",
                specRelativePath = "../myapi.json",
                basePackage = "com.example.sharedAB",
                topBasePackage = "com.example",
                targetSharedGroup = "AlphaClient,BetaClient",
                directGroupDeps = listOf("shared-alpha-client-beta-client-gamma-client"),
                kotlinVersion = "2.0.0",
                ktorVersion = "3.0.0",
                coroutinesVersion = "1.8.0",
                serializationVersion = "1.6.0",
            )
        assertContains(content, """api(project(":shared"))""")
        assertContains(content, """api(project(":shared-alpha-client-beta-client-gamma-client"))""")
    }

    @Test
    fun `GIVEN splitByClient=true and subprojectRootDirectory WHEN initSubproject THEN module dirs are under subprojectRootDirectory`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
                subprojectRootDirectory = "clients",
            )

        task.initSubproject()

        assertTrue(tempDir.resolve("clients/shared/build.gradle.kts").exists(), "shared dir should be under clients/")
        assertTrue(tempDir.resolve("clients/user-client/build.gradle.kts").exists(), "user-client should be under clients/")
        assertTrue(tempDir.resolve("clients/order-client/build.gradle.kts").exists(), "order-client should be under clients/")
        assertFalse(tempDir.resolve("shared/build.gradle.kts").exists(), "shared should NOT be at root level")
        assertFalse(tempDir.resolve("user-client/build.gradle.kts").exists(), "user-client should NOT be at root level")
    }

    @Test
    fun `GIVEN splitByClient=true and subprojectRootDirectory WHEN initSubproject THEN settings include uses it as prefix`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
                subprojectRootDirectory = "clients",
            )

        task.initSubproject()

        val settingsContent = tempDir.resolve("settings.gradle.kts").readText()
        assertContains(settingsContent, """":clients:shared"""")
        assertContains(settingsContent, """":clients:user-client"""")
        assertContains(settingsContent, """":clients:order-client"""")
    }

    @Test
    fun `GIVEN splitByClient=true and subprojectRootDirectory WHEN initSubproject THEN client build uses prefixed project ref`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
                subprojectRootDirectory = "clients",
            )

        task.initSubproject()

        val userClientContent = tempDir.resolve("clients/user-client/build.gradle.kts").readText()
        assertContains(userClientContent, """api(project(":clients:shared"))""")
    }

    @Test
    fun `GIVEN splitByClient=true and subprojectRootDirectory WHEN initSubproject THEN specRelativePath uses two levels up`() {
        val openApiFile =
            File(
                checkNotNull(javaClass.classLoader.getResource("multi-tag.json")) { "multi-tag.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
                subprojectRootDirectory = "clients",
            )

        task.initSubproject()

        val sharedContent = tempDir.resolve("clients/shared/build.gradle.kts").readText()
        assertContains(sharedContent, "../../src/main/openapi/")
        val userClientContent = tempDir.resolve("clients/user-client/build.gradle.kts").readText()
        assertContains(userClientContent, "../../src/main/openapi/")
    }

    @Test
    fun `GIVEN splitByClient=true and subprojectRootDirectory and SHARED_PER_GROUP WHEN initSubproject THEN modules are nested`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("shared-model-granularity.json"),
                ) { "shared-model-granularity.json not found" }.toURI(),
            )
        val task =
            buildTask(
                openApiFile = openApiFile.absolutePath,
                subprojectName = "my-api",
                splitByClient = true,
                sharedModelGranularity = "SHARED_PER_GROUP",
                subprojectRootDirectory = "modules",
            )

        task.initSubproject()

        assertTrue(tempDir.resolve("modules/shared/build.gradle.kts").exists(), "shared should be under modules/")
        assertFalse(tempDir.resolve("shared/build.gradle.kts").exists(), "shared should NOT be at root level")

        val settingsContent = tempDir.resolve("settings.gradle.kts").readText()
        assertContains(settingsContent, """":modules:shared"""")
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

/**
 * Unit tests for [InitSubprojectTask] internal naming helpers.
 *
 * Validates the two fixes:
 * 1. `toKebabCase()` handles digit→uppercase transitions (e.g. `V1Media` → `v1-media`)
 * 2. Client package names use camelCase (`apiV1Media`) instead of all-lowercase (`apiv1media`)
 */
internal class NamingTest {
    // ─────────────────────────────────────────────────────────────
    // toKebabCase — digit→uppercase transition
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN single-word client WHEN toKebabCase THEN lowercased`() {
        with(InitSubprojectTask) { assertEquals("user-client", "UserClient".toKebabCase()) }
    }

    @Test
    fun `GIVEN multi-word client WHEN toKebabCase THEN kebab-cased`() {
        with(InitSubprojectTask) { assertEquals("media-client", "MediaClient".toKebabCase()) }
    }

    @Test
    fun `GIVEN client with digit-uppercase transition WHEN toKebabCase THEN digit and uppercase are separated`() {
        // Bug: previously V1Media → v1media (no separator between 1 and M)
        with(InitSubprojectTask) { assertEquals("api-v1-media-client", "ApiV1MediaClient".toKebabCase()) }
    }

    @Test
    fun `GIVEN client with multiple digit segments WHEN toKebabCase THEN all transitions are separated`() {
        with(InitSubprojectTask) { assertEquals("api-v1-users-client", "ApiV1UsersClient".toKebabCase()) }
    }

    @Test
    fun `GIVEN client with BY_TAG_AND_PATH name WHEN toKebabCase THEN path segment is properly separated`() {
        with(InitSubprojectTask) { assertEquals("media-api-v1-media-client", "MediaApiV1MediaClient".toKebabCase()) }
    }

    // ─────────────────────────────────────────────────────────────
    // Client package — camelCase (Kotlin convention: org.example.myProject)
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN single-word client WHEN package derived THEN first char lowercased`() {
        val clientName = "UserClient"
        val pkg = clientName.removeSuffix("Client").replaceFirstChar { it.lowercase() }
        assertEquals("user", pkg)
    }

    @Test
    fun `GIVEN multi-segment client with digits WHEN package derived THEN camelCase preserved`() {
        // Bug: previously ApiV1Media → apiv1media (all lowercase)
        val clientName = "ApiV1MediaClient"
        val pkg = clientName.removeSuffix("Client").replaceFirstChar { it.lowercase() }
        assertEquals("apiV1Media", pkg)
    }

    @Test
    fun `GIVEN BY_TAG_AND_PATH client name WHEN package derived THEN camelCase preserved`() {
        val clientName = "MediaApiV1MediaClient"
        val pkg = clientName.removeSuffix("Client").replaceFirstChar { it.lowercase() }
        assertEquals("mediaApiV1Media", pkg)
    }

    // ─────────────────────────────────────────────────────────────
    // Integration: initSubproject with /api/v1/... paths
    // ─────────────────────────────────────────────────────────────

    @TempDir
    lateinit var tempDir: File

    private fun buildTask(
        openApiFile: String,
        splitByClient: Boolean = true,
        splitGranularity: String? = null,
    ): InitSubprojectTask {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val task =
            project.tasks
                .register("initApiClientSubproject", InitSubprojectTask::class.java)
                .get()
        task.openApiFilePath.set(openApiFile)
        task.rootDirectory.set(tempDir)
        task.kotlinVersion.set(DEFAULT_KOTLIN_VERSION)
        task.ktorVersion.set(DEFAULT_KTOR_VERSION)
        task.coroutinesVersion.set(DEFAULT_COROUTINES_VERSION)
        task.serializationVersion.set(DEFAULT_SERIALIZATION_VERSION)
        task.splitByClient.set(splitByClient)
        splitGranularity?.let { task.splitGranularity.set(it) }
        return task
    }

    @Test
    fun `GIVEN spec with v1 paths and BY_TAG_AND_PATH WHEN initSubproject THEN directory uses kebab with digit separator`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("v1-path.json"),
                ) { "v1-path.json not found" }.toURI(),
            )
        val task = buildTask(openApiFile = openApiFile.absolutePath, splitGranularity = "BY_TAG_AND_PATH")

        task.initSubproject()

        // Media tag + /api/v1/media path → MediaApiV1MediaClient → media-api-v1-media-client
        val mediaDir = tempDir.resolve("media-api-v1-media-client")
        assertTrue(
            mediaDir.exists(),
            "Expected media-api-v1-media-client dir. Found: ${tempDir.listFiles()?.map { it.name }}",
        )
        // User tag + /api/v1/users path → UserApiV1UsersClient → user-api-v1-users-client
        val userDir = tempDir.resolve("user-api-v1-users-client")
        assertTrue(
            userDir.exists(),
            "Expected user-api-v1-users-client dir. Found: ${tempDir.listFiles()?.map { it.name }}",
        )
    }

    @Test
    fun `GIVEN spec with v1 paths and BY_TAG_AND_PATH WHEN initSubproject THEN client package uses camelCase`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("v1-path.json"),
                ) { "v1-path.json not found" }.toURI(),
            )
        val task = buildTask(openApiFile = openApiFile.absolutePath, splitGranularity = "BY_TAG_AND_PATH")

        task.initSubproject()

        val mediaClientContent = tempDir.resolve("media-api-v1-media-client/build.gradle.kts").readText()
        // Package should be camelCase: basePackage.mediaApiV1Media (NOT mediaapiV1Media or mediaapiV1media)
        assertContains(mediaClientContent, ".mediaApiV1Media\"", message = "Package should use camelCase: got\n$mediaClientContent")
    }

    @Test
    fun `GIVEN spec with v1 paths and BY_TAG WHEN initSubproject THEN single-word client dirs are correct`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("v1-path.json"),
                ) { "v1-path.json not found" }.toURI(),
            )
        val task = buildTask(openApiFile = openApiFile.absolutePath)

        task.initSubproject()

        // BY_TAG: Media → media-client, User → user-client
        assertTrue(tempDir.resolve("media-client/build.gradle.kts").exists(), "Expected media-client dir")
        assertTrue(tempDir.resolve("user-client/build.gradle.kts").exists(), "Expected user-client dir")
    }

    @Test
    fun `GIVEN spec with v1 paths and BY_TAG WHEN initSubproject THEN client package uses camelCase`() {
        val openApiFile =
            File(
                checkNotNull(
                    javaClass.classLoader.getResource("v1-path.json"),
                ) { "v1-path.json not found" }.toURI(),
            )
        val task = buildTask(openApiFile = openApiFile.absolutePath)

        task.initSubproject()

        val mediaClientContent = tempDir.resolve("media-client/build.gradle.kts").readText()
        // MediaClient → media (single word, lowercase = decapitalized = same)
        assertContains(mediaClientContent, ".media\"", message = "Package should end with .media")
        // Should NOT be all-uppercase or mixed wrong
        assertFalse(mediaClientContent.contains(".Media\""), "Package segment must start with lowercase")
    }
}

/**
 * Unit tests for [InitSubprojectTask.updateSettingsIncludes].
 *
 * Verifies the marker-based include injection: creates a new settings file,
 * appends to an existing one without a marker, and replaces the block when
 * the marker is already present.
 */
internal class SettingsUpdaterTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `GIVEN no settings file WHEN updateSettingsIncludes THEN creates file with marker block`() {
        with(InitSubprojectTask) {
            updateSettingsIncludes(tempDir, listOf("my-client"))
        }

        val content = tempDir.resolve("settings.gradle.kts").readText()
        assertContains(content, InitSubprojectTask.SETTINGS_MARKER_START)
        assertContains(content, InitSubprojectTask.SETTINGS_MARKER_END)
        assertContains(content, """include("my-client")""")
    }

    @Test
    fun `GIVEN existing settings without marker WHEN updateSettingsIncludes THEN appends marker block`() {
        val settingsFile = tempDir.resolve("settings.gradle.kts")
        settingsFile.writeText("""rootProject.name = "my-project"""")

        with(InitSubprojectTask) {
            updateSettingsIncludes(tempDir, listOf("api-client"))
        }

        val content = settingsFile.readText()
        assertContains(content, """rootProject.name = "my-project"""")
        assertContains(content, InitSubprojectTask.SETTINGS_MARKER_START)
        assertContains(content, """include("api-client")""")
    }

    @Test
    fun `GIVEN existing settings with marker WHEN updateSettingsIncludes THEN replaces marker block`() {
        val settingsFile = tempDir.resolve("settings.gradle.kts")
        settingsFile.writeText(
            """
            rootProject.name = "my-project"

            ${InitSubprojectTask.SETTINGS_MARKER_START}
            include("old-client")
            ${InitSubprojectTask.SETTINGS_MARKER_END}
            """.trimIndent(),
        )

        with(InitSubprojectTask) {
            updateSettingsIncludes(tempDir, listOf("new-client", "shared"))
        }

        val content = settingsFile.readText()
        assertContains(content, """rootProject.name = "my-project"""")
        assertContains(content, """include("new-client", "shared")""")
        assertFalse(content.contains("""include("old-client")"""), "Old include should be replaced")
        assertEquals(1, content.split(InitSubprojectTask.SETTINGS_MARKER_START).size - 1, "Marker should appear exactly once")
    }

    @Test
    fun `GIVEN multi-module project WHEN updateSettingsIncludes THEN includes all modules`() {
        with(InitSubprojectTask) {
            updateSettingsIncludes(tempDir, listOf("shared", "user-client", "order-client"))
        }

        val content = tempDir.resolve("settings.gradle.kts").readText()
        assertContains(content, """include("shared", "user-client", "order-client")""")
    }

    @Test
    fun `GIVEN single subproject WHEN initSubproject THEN settings file is created with include`() {
        val openApiFile = tempDir.resolve("petstore.yaml").also { it.writeText("openapi: 3.0.0") }
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        val task =
            project.tasks
                .register("initApiClientSubproject", InitSubprojectTask::class.java)
                .get()
        task.openApiFilePath.set(openApiFile.absolutePath)
        task.subprojectName.set("my-client")
        task.rootDirectory.set(tempDir)
        task.kotlinVersion.set(DEFAULT_KOTLIN_VERSION)
        task.ktorVersion.set(DEFAULT_KTOR_VERSION)
        task.coroutinesVersion.set(DEFAULT_COROUTINES_VERSION)
        task.serializationVersion.set(DEFAULT_SERIALIZATION_VERSION)

        task.initSubproject()

        val settingsContent = tempDir.resolve("settings.gradle.kts").readText()
        assertContains(settingsContent, """include("my-client")""")
        assertContains(settingsContent, InitSubprojectTask.SETTINGS_MARKER_START)
    }
}
