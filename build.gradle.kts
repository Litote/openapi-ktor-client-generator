import groovy.json.JsonSlurper
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.sonarqube.gradle.SonarExtension
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

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
                ?: emptyList<File>()
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
    doLast {
        val result = ProcessBuilder(
            "${rootDir}/gradlew",
            "--write-verification-metadata", "sha256",
            "dependencies", "check",
        )
            .directory(rootDir)
            .inheritIO()
            .start()
            .waitFor()
        check(result == 0) { "updateVerificationMetadata failed with exit code $result" }
    }
}

/**
 * Fetches quality gate status and open issues from SonarCloud.
 * Fails the build if the gate is not OK or any issue/hotspot remains.
 *
 * Waits for SonarCloud to finish processing the analysis (async CE task) before checking.
 *
 * Prerequisites: run `./gradlew check jacocoAggregatedReport sonar` first to push the analysis.
 * Token: set `systemProp.sonar.token=<token>` in ~/.gradle/gradle.properties
 *
 * Full quality loop:
 *   ./gradlew check jacocoAggregatedReport sonar sonarCheck
 */
tasks.register("sonarCheck") {
    group = "verification"
    description = "Verifies SonarCloud quality gate: fails if gate != OK, issues > 0, or hotspots > 0."
    mustRunAfter("sonar")
    notCompatibleWithConfigurationCache("Reads sonar/report-task.txt generated at execution time")
    doLast {
        val token = project.providers.systemProperty("sonar.token")
            .orElse(project.providers.environmentVariable("SONAR_TOKEN"))
            .orNull
            ?: error("sonar.token not set. Add systemProp.sonar.token=<token> to ~/.gradle/gradle.properties")

        val projectKey = "Litote_openapi-ktor-client-generator"
        val client = HttpClient.newHttpClient()
        val slurper = JsonSlurper()

        fun fetch(url: String): Any {
            val req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer $token")
                .GET()
                .build()
            val resp = client.send(req, HttpResponse.BodyHandlers.ofString())
            check(resp.statusCode() == 200) {
                "SonarCloud API error ${resp.statusCode()}: ${resp.body()}"
            }
            return slurper.parseText(resp.body())
        }

        val reportTaskFile = layout.buildDirectory.file("sonar/report-task.txt").get().asFile
        if (reportTaskFile.exists()) {
            val props = java.util.Properties().also { it.load(reportTaskFile.inputStream()) }
            val ceTaskId = props.getProperty("ceTaskId")
            if (ceTaskId != null) {
                logger.lifecycle("⏳ Waiting for SonarCloud analysis (task $ceTaskId) to complete…")
                val maxWaitMs = 120_000L
                val pollIntervalMs = 5_000L
                val start = System.currentTimeMillis()
                while (true) {
                    @Suppress("UNCHECKED_CAST")
                    val taskData = fetch("https://sonarcloud.io/api/ce/task?id=$ceTaskId") as Map<String, Any>
                    @Suppress("UNCHECKED_CAST")
                    val taskStatus = (taskData["task"] as Map<String, Any>)["status"] as String
                    when (taskStatus) {
                        "SUCCESS" -> {
                            logger.lifecycle("✅ Analysis processing complete.")
                            break
                        }
                        "FAILED", "CANCELLED" -> error("SonarCloud analysis task $ceTaskId ended with status: $taskStatus")
                        else -> {
                            check(System.currentTimeMillis() - start < maxWaitMs) {
                                "Timed out waiting for SonarCloud analysis task $ceTaskId (status: $taskStatus)"
                            }
                            Thread.sleep(pollIntervalMs)
                        }
                    }
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        val qgData = fetch("https://sonarcloud.io/api/qualitygates/project_status?projectKey=$projectKey") as Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val gateStatus = (qgData["projectStatus"] as Map<String, Any>)["status"] as String

        @Suppress("UNCHECKED_CAST")
        val issuesData = fetch("https://sonarcloud.io/api/issues/search?projectKeys=$projectKey&resolved=false&ps=1") as Map<String, Any>
        val issueCount = (issuesData["total"] as Number).toInt()

        @Suppress("UNCHECKED_CAST")
        val hotspotsData = fetch("https://sonarcloud.io/api/hotspots/search?projectKey=$projectKey&status=TO_REVIEW&ps=1") as Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val hotspotCount = ((hotspotsData["paging"] as Map<String, Any>)["total"] as Number).toInt()

        logger.lifecycle("")
        logger.lifecycle("╔════════════════════════════════════╗")
        logger.lifecycle("║     SonarCloud Quality Gate        ║")
        logger.lifecycle("╠════════════════════════════════════╣")
        logger.lifecycle("║  Gate    : ${gateStatus.padEnd(24)}║")
        logger.lifecycle("║  Issues  : ${issueCount.toString().padEnd(24)}║")
        logger.lifecycle("║  Hotspots: ${hotspotCount.toString().padEnd(24)}║")
        logger.lifecycle("╚════════════════════════════════════╝")
        logger.lifecycle("")

        val failures = buildList {
            if (gateStatus != "OK") add("quality gate status is '$gateStatus' (expected OK)")
            if (issueCount > 0) add("$issueCount unresolved issue(s)")
            if (hotspotCount > 0) add("$hotspotCount unreviewed security hotspot(s)")
        }

        check(failures.isEmpty()) {
            "❌ SonarCloud check FAILED:\n${failures.joinToString("\n") { "  • $it" }}"
        }

        logger.lifecycle("✅ SonarCloud quality gate passed — 0 issues, 0 hotspots.")
    }
}
