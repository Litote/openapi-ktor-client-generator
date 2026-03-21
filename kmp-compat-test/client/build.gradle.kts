plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    iosArm64()
    iosX64()
    iosSimulatorArm64()
    macosArm64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosArm64()
    watchosSimulatorArm64()

    js {
        browser()
        nodejs()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    linuxX64()
    linuxArm64()

    mingwX64()

    sourceSets {
        commonMain.dependencies {
            // Mirrors generated client subproject: api(shared) + kotlin-convention deps.
            api(project(":shared"))
            api(libs.ktor.core)
            api(libs.ktor.cio)
            implementation(libs.ktor.contentnegotiation)
            api(libs.ktor.json)
            api(libs.ktor.log)
            api(libs.coroutines)
            api(libs.serialization)
        }
    }
}
