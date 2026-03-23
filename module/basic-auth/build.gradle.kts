plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(project(":generator"))
}

mavenPublishing {
    pom {
        description = "Basic auth module for openapi ktor generator"
    }
}
