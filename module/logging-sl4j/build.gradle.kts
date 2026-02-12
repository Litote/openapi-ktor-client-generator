plugins {
    id("kotlin-convention")
}

dependencies {
    implementation(project(":generator"))
}

mavenPublishing {
    pom {
        description = "logging sl4j module for openapi ktor generator"
    }
}
