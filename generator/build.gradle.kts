plugins {
    id("kotlin-convention")
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(project(":shared"))

    api(libs.kotlin.poet)
    api(libs.serialization)
    api(libs.openapi.bindings)

    implementation(libs.ktor.core)
    implementation(libs.logback)

    testImplementation(libs.coroutines)
    testImplementation(libs.ktor.cio)
    testImplementation(libs.ktor.contentnegotiation)
    testImplementation(libs.ktor.json)
    testImplementation(libs.ktor.log)
}

kotlin {
    sourceSets["test"].kotlin.srcDirs("src/test/kotlin", "build/openapi/src/main/kotlin")
}

mavenPublishing {
    pom {
        description = "core openapi ktor generator"
    }
}
