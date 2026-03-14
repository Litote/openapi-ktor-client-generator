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
    runtimeOnly(libs.logback)

    testImplementation(libs.coroutines)
    testImplementation(libs.bundles.ktor)
    testImplementation(libs.snakeyaml)
}

kotlin {
    sourceSets["test"].kotlin.srcDirs(
        "src/test/kotlin",
        // Snapshot .kt files are committed in src/test/resources/snapshots/ and used
        // as test source roots so that tests like PayloadTest can import generated classes.
        // SnapshotTest also regenerates them into build/snapshot-test-output/ at runtime
        // for comparison, but compilation always resolves against the committed snapshots.
        "src/test/resources/snapshots/sample-api/src/main/kotlin",
        "src/test/resources/snapshots/simple-api/src/main/kotlin",
        "src/test/resources/snapshots/mastodon-api/src/main/kotlin",
        "src/test/resources/snapshots/inheritance-api/src/main/kotlin",
        "src/test/resources/snapshots/yaml-api/src/main/kotlin",
        "src/test/resources/snapshots/karto-api/src/main/kotlin",
    )
}

mavenPublishing {
    pom {
        description = "core openapi ktor generator"
    }
}
