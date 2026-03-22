pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    versionCatalogs {
        create("e2e") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "e2e-split"


// <openapi-ktor-generated-includes>
// </openapi-ktor-generated-includes>
