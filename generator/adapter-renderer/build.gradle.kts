plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(project(":generator:domain"))
    implementation(project(":generator:port"))
    implementation(project(":generator:config"))
    implementation(project(":generator:adapter-writer"))
    implementation(project(":shared"))
    api(libs.kotlin.poet)
    implementation(libs.serialization)
    implementation(libs.ktor.core)
    implementation(libs.snakeyaml)
}

mavenPublishing {
    pom {
        description = "openapi ktor generator - Kotlin code renderer adapter"
    }
}
