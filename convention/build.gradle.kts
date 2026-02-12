import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.vanniktech.maven.publish)
}

kotlin {
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
        "-Xjdk-release=17",
    )
}