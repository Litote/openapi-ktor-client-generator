pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }

    versionCatalogs {
        create("e2e") {
            from(files("./gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "openapi-ktor-client-generator-e2e"
