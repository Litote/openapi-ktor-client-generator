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
    "generator:domain",
    "generator:port",
    "generator:config",
    "generator:application",
    "generator:adapter-writer",
    "generator:adapter-parser",
    "generator:adapter-renderer",
    "gradle-plugin",
    "module:unknown-enum-value",
    "module:logging-sl4j",
    "module:logging-kotlin"
)
