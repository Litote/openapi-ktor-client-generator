plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(project(":generator"))
}

mavenPublishing {
    pom {
        description = "unknown enum value handler module for openapi ktor generator"
    }
}
