package org.litote.openapi.ktor.client.generator

/**
 * Defines how shared models are grouped into Gradle subprojects when `splitByClient = true`.
 *
 * @see ApiGeneratorConfiguration.sharedModelGranularity
 */
public enum class SharedModelGranularity {
    /**
     * All models used by more than one client are placed in a single shared subproject (default behaviour).
     */
    SHARED_ALL,

    /**
     * Models are grouped by the exact set of clients that use them.
     * Each unique group gets its own dedicated Gradle subproject and package.
     *
     * Example: if `ModelA` is used by `OrderClient` and `UserClient`, it goes into a
     * `shared-order-user` subproject with package `{basePackage}.sharedOrderUser.model`.
     */
    SHARED_PER_GROUP,
}
