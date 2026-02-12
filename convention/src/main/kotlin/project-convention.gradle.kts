group = providers.gradleProperty("GROUP").orNull ?: error("Missing gradle.properties 'group'")
version = providers.gradleProperty("VERSION_NAME").orNull ?: error("Missing gradle.properties 'version'")
