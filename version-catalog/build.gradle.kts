plugins {
    id("catalog-convention")
}

catalog {
    versionCatalog {
        from(files("../gradle/libs.versions.toml"))
    }
}

mavenPublishing {
    configure(com.vanniktech.maven.publish.VersionCatalog())
    pom {
        description = "Version Catalog for openapi ktor generator"
    }
}
