plugins {
    id("kotlin-convention")
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(project(":generator:domain"))
    implementation(project(":generator:port"))
    implementation(project(":generator:config"))
    implementation(project(":generator:adapter-writer"))
    implementation(project(":shared"))
    implementation(project(":generator:adapter-parser"))
    api(libs.kotlin.poet)
    api(libs.serialization)
    implementation(libs.ktor.core)
    implementation(libs.snakeyaml)
}

mavenPublishing {
    pom {
        description = "openapi ktor generator - Kotlin code renderer adapter"
    }
}
