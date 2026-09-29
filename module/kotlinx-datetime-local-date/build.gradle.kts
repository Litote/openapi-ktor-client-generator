plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(project(":generator"))
}

mavenPublishing {
    pom {
        description = "kotlinx.datetime.LocalDate (date format) module for openapi ktor generator"
    }
}
