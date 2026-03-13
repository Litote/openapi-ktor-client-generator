plugins {
    id("kotlin-convention")
}

dependencies {
    api(project(":generator:domain"))
}

mavenPublishing {
    pom {
        description = "openapi ktor generator - port interfaces"
    }
}
