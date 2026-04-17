## Advanced usage


### `ClientConfiguration` parameters

All parameters have sensible defaults — only override what you need.

| Parameter | Type | Default | Description |
|---|---|---|---|
| `baseUrl` | `String` | value from spec | Base URL prepended to every request |
| `logLevel` | `LogLevel` | `LogLevel.HEADERS` | Ktor logging verbosity (`ALL`, `HEADERS`, `BODY`, `INFO`, `NONE`) |
| `engine` | `HttpClientEngineFactory<*>` | `CIO` | Ktor engine (swap for `MockEngine` in tests, `OkHttp` on Android, etc.) |
| `json` | `Json` | `Json { ignoreUnknownKeys = true }` | kotlinx.serialization `Json` instance |
| `httpClientAuthorization` | `HttpClientConfig<*>.() -> Unit` | `{}` | Hook to inject auth headers or other per-request config |
| `httpClientConfig` | `HttpClientConfig<*>.() -> Unit` | `defaultHttpClientConfig(…)` | Full Ktor client config — override to replace the default setup entirely |
| `client` | `HttpClient` | built from `engine` + `httpClientConfig` | Pre-built `HttpClient` — inject a mock in tests |
| `exceptionLogger` | `Throwable.() -> Unit` | `{ printStackTrace() }` | Called when a client catches an unexpected exception |

### Modules

Modules extend the code generator with additional behaviour. They are activated by adding their ID to `modulesIds` in the generator configuration:

```kotlin
apiClientGenerator {
    generators {
        create("openapi") {
            openApiFile = file("src/main/openapi/openapi.json")
            basePackage = "com.example.api"
            modulesIds.add("UnknownEnumValueModule")
            modulesIds.add("LoggingKotlinModule")
        }
    }
}
```

#### Built-in modules

| Module ID | Effect |
|---|---|
| `UnknownEnumValueModule` | Adds an `UNKNOWN_` fallback constant to every generated enum and enables `coerceInputValues = true` in the Json configuration, so unknown server values never cause a deserialization error |
| `LoggingSl4jModule` | Configures the `ClientConfiguration` exception logger to use SLF4J (`LoggerFactory.getLogger(…).error(…)`). **JVM-only** — do not use in KMP projects targeting non-JVM platforms |
| `LoggingKotlinModule` | Configures the `ClientConfiguration` exception logger to use kotlin-logging / oshai (`KotlinLogging.logger(…).error(…)`) |
| `BasicAuthModule` | Adds an `accessToken: String?` parameter to `ClientConfiguration` and configures `httpClientAuthorization` to inject an `Authorization: Bearer <token>` header on every request |

#### Custom module at runtime

You can define a module inline in your `build.gradle.kts` without publishing a separate artifact. Implement `ApiGeneratorModule` and pass it via `customModules`:

```kotlin
import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.domain.ClientSpec
import org.litote.openapi.ktor.client.generator.domain.GeneratedFileSpec

val copyrightModule = object : ApiGeneratorModule {
    // Prepend a copyright header to every generated file
    override fun transformFile(file: GeneratedFileSpec): GeneratedFileSpec =
        file.copy(content = "// Copyright 2026 Acme Corp — do not edit\n" + file.content)

    // Keep only GET operations in every client
    override fun transformClientSpec(spec: ClientSpec): ClientSpec =
        spec.copy(operations = spec.operations.filter { it.method == "GET" })
}

apiClientGenerator {
    generators {
        create("openapi") {
            openApiFile = file("src/main/openapi/openapi.json")
            basePackage = "com.example.api"
            customModules.add(copyrightModule)
        }
    }
}
```

> **Configuration cache compatibility:** anonymous module instances are not serializable, so tasks that use `customModules` are not compatible with the Gradle configuration cache. Three alternatives:
>
> - **`buildSrc` or a convention plugin** — define the module as a named class there; Gradle can serialize it and the task stays configuration cache compatible:
    >
    >   ```kotlin
>   // buildSrc/src/main/kotlin/CopyrightModule.kt
>   import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
>   import org.litote.openapi.ktor.client.generator.domain.GeneratedFileSpec
>
>   class CopyrightModule : ApiGeneratorModule {
>       override fun transformFile(file: GeneratedFileSpec): GeneratedFileSpec =
>           file.copy(content = "// Copyright 2026 Acme Corp\n" + file.content)
>   }
>   ```
    >
    >   ```kotlin
>   // build.gradle.kts
>   customModules.add(CopyrightModule())
>   ```
>
> - **SPI via `modulesIds`** — package the module as a library with a `META-INF/services/org.litote.openapi.ktor.client.generator.ApiGeneratorModule` entry, add it to the buildscript classpath, and reference it by ID. The built-in modules (`UnknownEnumValueModule`, `LoggingSl4jModule`, `LoggingKotlinModule`) follow exactly this pattern and can serve as implementation examples.
>
> - **Disable the configuration cache** — if neither approach suits your project, set `org.gradle.configuration-cache=false` in `gradle.properties` (this is the default value).

#### What modules can do

A module can implement any combination of the following hooks — all are no-ops by default:

| Hook | Called when | Can do |
|---|---|---|
| `processConfiguration(ApiConfigurationGeneratorConfig)` | Before `ClientConfiguration` is rendered | Set custom Json properties (`coerceInputValues`, etc.), override the exception-logging lambda |
| `processClient(ApiClientGeneratorConfig)` | Before any client class is rendered | Configure client-level rendering options (reserved for future use) |
| `processModel(ApiModelGeneratorConfig)` | Before any model class is rendered | Set a fallback enum constant (`defaultEnumValue`) |
| `transformClientSpec(ClientSpec): ClientSpec` | For each client, before KotlinPoet rendering | Add, remove or rewrite operations; rename the client; change parameters or response types |
| `transformModelSpec(ModelSpec): ModelSpec` | For each model, before KotlinPoet rendering | Add, remove or rewrite properties; change the model kind (data class, enum, sealed…) |
| `transformFile(GeneratedFileSpec): GeneratedFileSpec` | After KotlinPoet rendering, before writing to disk | Add a file header, rewrite imports, inject code at the text level |

Hooks are applied in the order the modules are listed. `transform*` hooks receive an immutable domain object and must return the (possibly modified) replacement — the original is never mutated.

### Using the Version Catalog

A version catalog is published to Maven Central alongside the plugin. It exposes all library versions used by the generator (Ktor, kotlinx.serialization, coroutines, etc.), which you can use to align your own dependencies.

In your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("openapiKtor") {
            from("org.litote.openapi.ktor.client.generator:version-catalog:<last version>")
        }
    }
}
```

Then reference compatible versions in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(openapiKtor.bundles.ktor)
    implementation(openapiKtor.serialization)
    implementation(openapiKtor.coroutines)
}
```

### YAML support

If your OpenAPI spec uses `application/yaml` or `application/x-yaml` content types, the generator automatically:
- Generates a `YamlContentConverter` class in the client package
- Registers it in `ContentNegotiation` for both `application/yaml` and `application/x-yaml`
- Sets the correct `Content-Type` header on YAML requests

You must add **SnakeYAML** to your project dependencies:

```kotlin
dependencies {
    implementation("org.yaml:snakeyaml:<latest version>")
}
```

The latest SnakeYAML version can be found in the [published version catalog](README.md#using-the-version-catalog) (`openapiKtor.versions.snakeyaml`).

### KMP limitations

#### YAML content type

The `YamlContentConverter` (see below) generated when your API uses `application/yaml` content types depends on
[SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml), which is a **JVM-only** library. If you
target non-JVM platforms, place the SnakeYAML dependency in a `jvmMain` source set and implement a
platform-specific YAML converter for other targets.

#### `LoggingSl4jModule` and KMP

The `LoggingSl4jModule` generates code that uses `org.slf4j.LoggerFactory`, which is JVM-only.
Do not use this module in KMP projects targeting non-JVM platforms.

## Troubleshooting

### Implicit dependencies between tasks

If you get a Gradle error about implicit task dependencies
(see [validation_problems#implicit_dependency](https://docs.gradle.org/current/userguide/validation_problems.html#implicit_dependency)),
add the dependencies explicitly:

```kotlin
project
    .tasks
    .named { name -> name.contains("whatever") }
    .configureEach {
        project.tasks.withType(org.litote.openapi.ktor.client.generator.plugin.GenerateTask::class.java).forEach {
            dependsOn(it)
        }
    }
```

### Linter errors on generated code

Generated code is not linted. Suppress linter warnings by adding an `.editorconfig` entry, for example for ktlint:

```
[build/**/*]
ktlint = disabled
```