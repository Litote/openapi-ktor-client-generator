import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("project-convention")
    kotlin("jvm")
    id("signing")
}

plugin("vanniktech.maven.publish")

dependencies {
    constraints {
        implementation(lib("kotlin-reflect"))
    }
    implementation(lib("logging"))
    testImplementation(kotlin("test"))
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
}

signing {
    useGpgCmd()
}


