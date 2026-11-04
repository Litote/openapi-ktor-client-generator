import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.sonarqube.gradle.SonarExtension

plugins {
    id("project-convention")
    alias(libs.plugins.version.catalog.update)
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.sonarqube)
    id("jacoco")
}

val jacocoAggregatedReport by tasks.registering(JacocoReport::class) {
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("test") })

    executionData.setFrom(
        subprojects.flatMap { sub ->
            sub.fileTree("${sub.layout.buildDirectory.get()}/jacoco") { include("*.exec") }
        },
    )

    sourceDirectories.setFrom(
        subprojects.flatMap { sub ->
            sub.extensions.findByType(KotlinJvmProjectExtension::class)
                ?.sourceSets?.getByName("main")?.kotlin?.srcDirs
                ?: emptyList()
        },
    )

    classDirectories.setFrom(
        subprojects.flatMap { sub ->
            sub.fileTree("${sub.layout.buildDirectory.get()}/classes/kotlin/main") {
                exclude("**/*\$\$serializer.class")
            }
        },
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

sonar {
    properties {
        property("sonar.projectKey", "Litote_openapi-ktor-client-generator")
        property("sonar.organization", "litote")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.qualitygate.wait", true)
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/jacoco/jacocoAggregatedReport/jacocoAggregatedReport.xml").get().asFile.absolutePath,
        )
    }
}

val aggregatedReportPath: String = layout.buildDirectory
    .file("reports/jacoco/jacocoAggregatedReport/jacocoAggregatedReport.xml")
    .get().asFile.absolutePath

subprojects {
    afterEvaluate {
        extensions.findByType(SonarExtension::class)?.properties {
            property("sonar.coverage.jacoco.xmlReportPaths", aggregatedReportPath)
        }
    }
}

/**
 * Regenerates gradle/verification-metadata.xml after dependency upgrades.
 *
 * Run this after every `versionCatalogUpdate` or manual dependency change:
 *   ./gradlew updateVerificationMetadata
 */
tasks.register("updateVerificationMetadata") {
    group = "verification"
    description = "Regenerates gradle/verification-metadata.xml after dependency upgrades. Run after every versionCatalogUpdate."
    notCompatibleWithConfigurationCache("Spawns a Gradle subprocess that writes verification-metadata.xml")
    doLast {
        val result = ProcessBuilder(
            "${rootDir}/gradlew",
            "--write-verification-metadata", "sha256", "--refresh-dependencies",
            "dependencies", "check",
        )
            .directory(rootDir)
            .inheritIO()
            .start()
            .waitFor()
        check(result == 0) { "updateVerificationMetadata failed with exit code $result" }
    }
}
