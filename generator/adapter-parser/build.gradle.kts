plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(project(":generator:domain"))
    implementation(project(":generator:port"))
    implementation(project(":generator:config"))
    implementation(project(":shared"))
    implementation(libs.openapi.bindings)
    implementation(libs.kotlin.poet)
    implementation(libs.serialization)
    implementation(libs.snakeyaml)
}

mavenPublishing {
    pom {
        description = "openapi ktor generator - OpenAPI specification parser adapter"
    }
}
