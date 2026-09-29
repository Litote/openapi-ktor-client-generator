plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(project(":generator"))
}

mavenPublishing {
    pom {
        description = "kotlin.time.Instant (date-time format) module for openapi ktor generator"
    }
}
