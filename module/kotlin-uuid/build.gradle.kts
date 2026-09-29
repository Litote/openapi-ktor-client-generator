plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(project(":generator"))
}

mavenPublishing {
    pom {
        description = "kotlin.uuid.Uuid (uuid format) module for openapi ktor generator"
    }
}
