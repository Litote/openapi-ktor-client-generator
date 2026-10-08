package org.litote.openapi.ktor.client.generator

import org.litote.openapi.ktor.client.generator.domain.OperationMetaSpec

public data class ApiGeneratorConfiguration(
    val openApiFile: String = "src/main/openapi/openapi.json",
    val outputDirectory: String = openApiFile.substring(openApiFile.lastIndexOf('/'), openApiFile.lastIndexOf('.')),
    val basePackage: String = "org.example",
    val operationFilter: (OperationMetaSpec) -> Boolean = { true },
    val modelPackage: String = "$basePackage.model",
    val clientPackage: String = "$basePackage.client",
    val modules: List<ApiGeneratorModule> = emptyList(),
    val splitByClient: Boolean = false,
    val targetClientName: String? = null,
    /**
     * Base package of the shared module. When set, `ClientConfiguration` is imported from
     * `sharedBasePackage.client` instead of `basePackage.client`. Use this when a client submodule
     * has a distinct [basePackage] (e.g. `com.example.api.user`) but `ClientConfiguration` lives in
     * the shared module's package (e.g. `com.example.api`).
     */
    val sharedBasePackage: String? = null,
    /**
     * Controls how operations are grouped into client classes.
     * Defaults to [SplitGranularity.BY_TAG] (one client per OpenAPI tag).
     */
    val splitGranularity: SplitGranularity = SplitGranularity.BY_TAG,
    /**
     * Controls how shared models are distributed across Gradle subprojects when [splitByClient] is true.
     * Defaults to [SharedModelGranularity.SHARED_ALL] (all shared models in one `shared` subproject).
     */
    val sharedModelGranularity: SharedModelGranularity = SharedModelGranularity.SHARED_ALL,
    /**
     * When [sharedModelGranularity] is [SharedModelGranularity.SHARED_PER_GROUP] and [targetClientName]
     * is null, targets a specific shared group identified by the exact set of client names that use it.
     * When null, generates the global shared subproject (ClientConfiguration + orphan models).
     */
    val targetSharedGroup: Set<String>? = null,
    /**
     * Overrides the model package for specific model classes.
     * Used when a client depends on per-group shared subprojects with dedicated packages.
     * Maps model class name → fully qualified package name.
     *
     * Example: `mapOf("OrderModel" to "org.example.sharedOrderUser.model")`
     */
    val modelPackageOverrides: Map<String, String> = emptyMap(),
    /**
     * Default value of the `userAgent` parameter of the generated `ClientConfiguration`,
     * sent as the `User-Agent` header of every request (e.g. `"MyApp/1.0 (+https://example.com)"`).
     * When null, no `User-Agent` header is added and Ktor sends its own default.
     */
    val userAgent: String? = null,
    /**
     * Default Ktor engine of the generated `ClientConfiguration`: the fully qualified name of an
     * `HttpClientEngineFactory` (e.g. `"io.ktor.client.engine.okhttp.OkHttp"`), or [PLATFORM_ENGINE]
     * to let Ktor select the engine available on each platform (e.g. with `io.ktor:ktor-client-engine-defaults`).
     * Defaults to [CIO_ENGINE], which supports HTTPS only on the JVM.
     */
    val engine: String = CIO_ENGINE,
) {
    public companion object {
        /** The Ktor CIO engine, the default [engine]. */
        public const val CIO_ENGINE: String = "io.ktor.client.engine.cio.CIO"

        /** [engine] value for which the generated code creates the `HttpClient` without engine, so Ktor selects it. */
        public const val PLATFORM_ENGINE: String = "platform"
    }

    /** Package used to reference `ClientConfiguration` — the shared module's client package when set. */
    val configPackage: String = "${sharedBasePackage ?: basePackage}.client"

    /**
     * Package where model FILES are generated.
     *
     * For `SHARED_PER_GROUP` mode (when [targetSharedGroup] is non-null and [sharedBasePackage]
     * is set), uses [basePackage] so the per-group subproject generates models in its own
     * dedicated package instead of the global shared package.
     *
     * In all other cases falls back to [resolvedModelPackage] to preserve existing behaviour
     * (e.g. `SHARED_ALL` clients keep their private models in `sharedBasePackage.model`).
     */
    val generationModelPackage: String =
        if (targetSharedGroup != null && sharedBasePackage != null) {
            "$basePackage.model"
        } else {
            "${sharedBasePackage ?: basePackage}.model"
        }

    /**
     * Fallback package used for model TYPE REFERENCES not explicitly in [modelPackageOverrides].
     * When [sharedBasePackage] is set this resolves to `sharedBasePackage.model`, which is the
     * global shared subproject's model package — ensuring cross-package imports are generated
     * correctly for models that live in the global shared subproject.
     */
    val resolvedModelPackage: String = "${sharedBasePackage ?: basePackage}.model"
}
