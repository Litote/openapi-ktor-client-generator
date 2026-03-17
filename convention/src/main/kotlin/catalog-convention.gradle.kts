plugins {
    id("project-convention")
    `version-catalog`
    id("signing")
}

plugin("vanniktech.maven.publish")

signing {
    val inMemoryKey = providers.gradleProperty("signingInMemoryKey").orNull
    if (inMemoryKey != null) {
        val keyId = providers.gradleProperty("signingInMemoryKeyId").orNull
        val password = providers.gradleProperty("signingInMemoryKeyPassword").orNull ?: ""
        useInMemoryPgpKeys(keyId, inMemoryKey, password)
    } else {
        useGpgCmd()
    }
}
