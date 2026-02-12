pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    includeBuild("convention")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "openapi-ktor-client-generator"
include(
    "shared",
    "generator",
    "gradle-plugin",
    "module:unknown-enum-value",
    "module:logging-sl4j"
)
