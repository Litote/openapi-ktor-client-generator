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
}

rootProject.name = "e2e-split"


// <openapi-ktor-generated-includes>
include(":client:shared", ":client:shared-userusersget-useruserspost", ":client:shared-orderordersget-orderorderspost", ":client:user-users-get-client", ":client:user-users-post-client", ":client:order-orders-get-client", ":client:order-orders-post-client")
// </openapi-ktor-generated-includes>
