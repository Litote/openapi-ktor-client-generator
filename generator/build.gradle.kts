plugins {
    id("kotlin-convention")
    alias(libs.plugins.serialization)
}

dependencies {
    api(project(":generator:config"))
    api(project(":generator:domain"))
    api(project(":generator:port"))
    implementation(project(":generator:application"))
    implementation(project(":generator:adapter-parser"))
    implementation(project(":generator:adapter-renderer"))
    implementation(project(":generator:adapter-writer"))
    implementation(project(":shared"))
    implementation(libs.logback)

    testImplementation(libs.coroutines)
    testImplementation(libs.ktor.cio)
    testImplementation(libs.ktor.contentnegotiation)
    testImplementation(libs.ktor.json)
    testImplementation(libs.ktor.log)
    testImplementation(libs.snakeyaml)
}

kotlin {
    sourceSets["test"].kotlin.srcDirs(
        "src/test/kotlin",
        "build/snapshot-test-output/sample-api/src/main/kotlin",
        "build/snapshot-test-output/simple-api/src/main/kotlin",
        "build/snapshot-test-output/mastodon-api/src/main/kotlin",
        "build/snapshot-test-output/inheritance-api/src/main/kotlin",
        "build/snapshot-test-output/yaml-api/src/main/kotlin",
    )
}

mavenPublishing {
    pom {
        description = "core openapi ktor generator"
    }
}
