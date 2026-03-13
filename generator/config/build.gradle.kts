plugins {
    id("kotlin-convention")
}

dependencies {
    api(project(":generator:domain"))
    api(project(":generator:port"))
}

mavenPublishing {
    pom {
        description = "openapi ktor generator - public configuration API"
    }
}
