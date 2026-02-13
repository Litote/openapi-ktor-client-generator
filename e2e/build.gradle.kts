plugins {
    `kotlin-dsl`
    alias(e2e.plugins.kotlin.jvm)
    id("org.litote.openapi.ktor.client.generator.gradle") version "+"
    alias(e2e.plugins.serialization)
}

dependencies {
    implementation(e2e.serialization)
    implementation(e2e.coroutines)
    implementation(e2e.bundles.ktor)
}

group = providers.gradleProperty("GROUP").orNull ?: error("Missing gradle.properties 'group'")
version = providers.gradleProperty("VERSION_NAME").orNull ?: error("Missing gradle.properties 'version'")

apiClientGenerator {
    generators {
        create("openapi") {
            outputDirectory = file("build/generated")
            allowedPaths = setOf("/test-status")
            modulesIds = setOf("UnknownEnumValueModule", "LoggingSl4jModule")
        }
    }
}
