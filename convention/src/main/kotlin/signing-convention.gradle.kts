plugins {
    id("signing")
}

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
