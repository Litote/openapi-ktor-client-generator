import com.vanniktech.maven.publish.GradlePublishPlugin
import org.gradle.plugin.compatibility.compatibility

plugins {
    alias(libs.plugins.gradle.publish)
    id("kotlin-convention")
}

val generateVersionConstants by tasks.registering {
    val version = project.version.toString()
    val tomlFile = rootProject.file("gradle/libs.versions.toml")
    val outputDir = layout.buildDirectory.dir("generated/kotlin")
    outputs.dir(outputDir)
    inputs.property("version", version)
    inputs.file(tomlFile)
    doLast {
        fun readTomlVersion(key: String): String =
            tomlFile.readLines()
                .first { it.trimStart().startsWith("$key =") }
                .substringAfter("\"")
                .substringBefore("\"")

        val kotlinVersion = readTomlVersion("kotlin")
        val ktorVersion = readTomlVersion("ktor")
        val coroutinesVersion = readTomlVersion("coroutines")
        val serializationVersion = readTomlVersion("kotlinx-serialization")

        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("PluginVersion.kt").writeText(
            """
            package org.litote.openapi.ktor.client.generator.plugin

            internal const val PLUGIN_VERSION = "$version"
            internal const val DEFAULT_KOTLIN_VERSION = "$kotlinVersion"
            internal const val DEFAULT_KTOR_VERSION = "$ktorVersion"
            internal const val DEFAULT_COROUTINES_VERSION = "$coroutinesVersion"
            internal const val DEFAULT_SERIALIZATION_VERSION = "$serializationVersion"
            """.trimIndent(),
        )
    }
}

sourceSets.main {
    kotlin.srcDir(generateVersionConstants)
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)

    implementation(project(":shared"))
    implementation(project(":generator"))

    implementation(project(":module:unknown-enum-value"))
    implementation(project(":module:logging-sl4j"))
}

val pluginDescription = "Gradle plugin to generate OpenApi client with ktor/kotlinx.serialization"
gradlePlugin {
    plugins {
        create("ktorClientGenerator") {
            id = "org.litote.openapi.ktor.client.generator.gradle"
            implementationClass = "org.litote.openapi.ktor.client.generator.plugin.GeneratorPlugin"
            displayName = "Gradle OpenAPI ktor client generator plugin"
            description = pluginDescription
            website = "https://github.com/Litote/openapi-ktor-client-generator"
            vcsUrl = "https://github.com/Litote/openapi-ktor-client-generator.git"
            tags.set(listOf("openapi", "ktor", "client", "generator"))
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
}

tasks.register("printPlugins") {
    doLast {
        project.plugins.forEach {
            val pkg = it.javaClass.`package`
            val version = pkg?.implementationVersion ?: "unknown"
            println("${it.javaClass.name} -> $version")
        }
    }
}

mavenPublishing {
    configure(GradlePublishPlugin())
    description = pluginDescription
    pom {
        description = pluginDescription
    }
}
