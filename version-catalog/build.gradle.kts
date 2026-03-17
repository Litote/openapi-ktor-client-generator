plugins {
    id("catalog-convention")
}

catalog {
    versionCatalog {
        from(files("../gradle/libs.versions.toml"))
    }
}

publishing {
    publications {
        create<MavenPublication>("catalog") {
            from(components["versionCatalog"])
        }
    }
}

tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(tasks.withType<Sign>())
}
