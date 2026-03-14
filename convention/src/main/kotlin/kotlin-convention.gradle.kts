import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("project-convention")
    kotlin("jvm")
    id("jacoco")
    id("signing")
}

plugin("vanniktech.maven.publish")
plugin("ktlint")

dependencies {
    constraints {
        implementation(lib("kotlin-reflect"))
    }
    implementation(lib("logging"))
    testImplementation(kotlin("test"))
    testImplementation(lib("mockk"))
}

kotlin {
    explicitApi()
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll("-Xjdk-release=17", "-Xconsistent-data-class-copy-visibility")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    useJUnitPlatform()
    failOnNoDiscoveredTests = false
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")

    maxParallelForks = Runtime.getRuntime().availableProcessors()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(false)
    }
}

signing {
    useGpgCmd()
}
