plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(project(":shared"))
}

mavenPublishing {
    pom {
        description = "openapi ktor generator - domain model"
    }
}
