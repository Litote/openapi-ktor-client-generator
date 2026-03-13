plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(project(":generator:domain"))
    implementation(project(":generator:port"))
}

mavenPublishing {
    pom {
        description = "openapi ktor generator - application services"
    }
}
