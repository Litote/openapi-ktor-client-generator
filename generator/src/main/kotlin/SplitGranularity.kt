package org.litote.openapi.ktor.client.generator

/**
 * Defines the granularity used to group OpenAPI operations into generated client classes.
 *
 * @see ApiGeneratorConfiguration.splitGranularity
 */
public enum class SplitGranularity {
    /**
     * One client class per OpenAPI tag (default behaviour).
     * Example: tag `user` → `UserClient`.
     */
    BY_TAG,

    /**
     * One client class per unique (tag, path) combination.
     * Example: tag `user` + path `/v1/users/{id}` → `UserV1UsersIdClient`.
     */
    BY_TAG_AND_PATH,

    /**
     * One client class per unique (tag, path, HTTP method) combination.
     * Example: tag `user` + path `/v1/users/{id}` + method `GET` → `UserV1UsersIdGetClient`.
     */
    BY_TAG_AND_OPERATION,
}
