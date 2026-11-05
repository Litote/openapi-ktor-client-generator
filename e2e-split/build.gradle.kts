import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnPlugin
import org.jetbrains.kotlin.gradle.targets.wasm.yarn.WasmYarnRootExtension

plugins {
    // Declare Kotlin plugin versions so subprojects can use them without classpath conflict.
    // These versions must match DEFAULT_KOTLIN_VERSION baked into the generator plugin.
    alias(e2e.plugins.kotlin.jvm) apply false
    alias(e2e.plugins.kotlin.multiplatform) apply false
    id("org.litote.openapi.ktor.client.generator.gradle") version "main-SNAPSHOT"
}

apiClientGenerator {
    initSubproject {
        multiplatform.set(true)
        generatorConfigExtra.set("""modulesIds = setOf("LoggingKotlinModule")""")
        additionalTargets = listOf(
            "iosArm64()",
            "iosX64()",
            "iosSimulatorArm64()",
            "macosArm64()",
            "tvosArm64()",
            "tvosSimulatorArm64()",
            "watchosArm64()",
            "watchosSimulatorArm64()",
            """js {
                        browser()
                        nodejs()
                  }""",
            """@OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
                    wasmJs {
                                browser()
                                nodejs()
                            }""",
            "linuxX64()",
            "linuxArm64()",
            "mingwX64()",
        )
        additionalDependencies.add("io.github.oshai:kotlin-logging:${e2e.versions.logging.get()}")
    }
}

// ktor-client-core pins ws to a version affected by GHSA-96hv-2xvq-fx4p.
// Remove these resolutions once Ktor depends on ws >= 8.21.0.
val wsVersion = "8.21.3"
plugins.withType<YarnPlugin> {
    the<YarnRootExtension>().resolution("ws", wsVersion)
}
plugins.withType<WasmYarnPlugin> {
    the<WasmYarnRootExtension>().resolution("ws", wsVersion)
}
