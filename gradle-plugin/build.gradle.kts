import com.vanniktech.maven.publish.GradlePublishPlugin

plugins {
    alias(libs.plugins.gradle.publish)
    id("kotlin-convention")
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
