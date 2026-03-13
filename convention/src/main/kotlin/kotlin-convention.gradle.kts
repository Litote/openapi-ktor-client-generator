import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("project-convention")
    kotlin("jvm")
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
    java {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
    compilerOptions.freeCompilerArgs = listOf(
        "-Xjdk-release=17","-Xconsistent-data-class-copy-visibility"
    )
}

tasks.test {
    useJUnitPlatform()
    failOnNoDiscoveredTests = false
    systemProperty("junit.jupiter.execution.parallel.enabled", "true")
    systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")

    maxParallelForks = Runtime.getRuntime().availableProcessors()
}

signing {
    useGpgCmd()
}


