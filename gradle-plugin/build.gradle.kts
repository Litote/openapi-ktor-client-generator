import com.vanniktech.maven.publish.GradlePublishPlugin
import org.gradle.plugin.compatibility.compatibility

plugins {
    alias(libs.plugins.gradle.publish)
    id("kotlin-convention")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val generateVersionConstants by tasks.registering {
    val version = project.version.toString()
    val kotlinVersion = catalog.findVersion("kotlin").get().requiredVersion
    val ktorVersion = catalog.findVersion("ktor").get().requiredVersion
    val coroutinesVersion = catalog.findVersion("coroutines").get().requiredVersion
    val serializationVersion = catalog.findVersion("kotlinx-serialization").get().requiredVersion
    val outputDir = layout.buildDirectory.dir("generated/kotlin")
    outputs.dir(outputDir)
    inputs.property("version", version)
    inputs.property("kotlinVersion", kotlinVersion)
    inputs.property("ktorVersion", ktorVersion)
    inputs.property("coroutinesVersion", coroutinesVersion)
    inputs.property("serializationVersion", serializationVersion)
    doLast {
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
    implementation(project(":module:logging-kotlin"))
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

mavenPublishing {
    configure(GradlePublishPlugin())
    pom {
        description = pluginDescription
    }
}
