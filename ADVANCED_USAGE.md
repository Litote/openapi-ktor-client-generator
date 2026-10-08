## Advanced usage


### `ClientConfiguration` parameters

All parameters have sensible defaults — only override what you need.

| Parameter | Type | Default | Description |
|---|---|---|---|
| `baseUrl` | `String` | value from spec | Base URL prepended to every request |
| `logLevel` | `LogLevel` | `LogLevel.HEADERS` | Ktor logging verbosity (`ALL`, `HEADERS`, `BODY`, `INFO`, `NONE`) |
| `userAgent` | `String?` | `userAgent` generator property, or `null` | `User-Agent` header sent with every request (see [User-Agent](#user-agent)). `null` keeps the Ktor default |
| `engine` | `HttpClientEngineFactory<*>` (`HttpClientEngineFactory<*>?` with `engine = "platform"`) | `engine` generator property (`CIO`) | Ktor engine (swap for `MockEngine` in tests, `OkHttp` on Android, etc.), see [HTTP engine](#http-engine) |
| `json` | `Json` | `Json { ignoreUnknownKeys = true }` | kotlinx.serialization `Json` instance |
| `httpClientAuthorization` | `HttpClientConfig<*>.() -> Unit` | `{}` | Hook to inject auth headers or other per-request config |
| `httpClientConfig` | `HttpClientConfig<*>.() -> Unit` | `defaultHttpClientConfig(…)` | Full Ktor client config — override to replace the default setup entirely |
| `client` | `HttpClient` | built from `engine` + `httpClientConfig` | Pre-built `HttpClient` — inject a mock in tests |
| `exceptionLogger` | `Throwable.() -> Unit` | `{ printStackTrace() }` | Called when a client catches an unexpected exception (`CancellationException` is always rethrown and never passed to this logger) |

### User-Agent

Some public APIs ask clients to identify themselves with a `User-Agent` header. Set its default value with the
`userAgent` generator property:

```kotlin
apiClientGenerator {
    generators {
        create("openapi") {
            userAgent = "MyApp/1.0 (+https://example.com)"
        }
    }
}
```

Every `ClientConfiguration()` then sends this header. It can still be changed at runtime:
`ClientConfiguration(userAgent = "MyApp/2.0 (+mailto:contact@example.com)")`. A `User-Agent` header set on a request
takes precedence. The header is installed with the Ktor `UserAgent` plugin by `defaultHttpClientConfig`, so it is
not applied when `httpClientConfig` or `client` is replaced.

#### In a browser

Browsers do not let scripts change the `User-Agent` header (nor the `Referer` header): on Kotlin/JS and Kotlin/Wasm
targets running in a browser, the `userAgent` value is ignored and the browser sends its own `User-Agent`.
The calling site is then identified by the `Referer` header, which the browser fills in from the page URL. If the
API asks clients to identify themselves:

- check whether the API accepts the `Referer` in place of the `User-Agent` (for instance when the site shows a
  contact address);
- display the contact information required by the API on the site;
- keep a `Referrer-Policy` that sends the `Referer` to the API. The browser default
  (`strict-origin-when-cross-origin`) sends only the origin (`https://example.com/`) to another site, and
  `no-referrer` removes it.

Nothing in the generated code checks these points.

### HTTP engine

`ClientConfiguration` uses the Ktor `CIO` engine by default. CIO supports HTTPS only on the JVM: on Kotlin/Native
the calls fail with "TLS sessions are not supported on Native platform". Choose another default engine with the
`engine` generator property:

```kotlin
apiClientGenerator {
    generators {
        create("openapi") {
            // a single engine, for every platform
            engine = "io.ktor.client.engine.okhttp.OkHttp"
            // or: no default engine, Ktor selects the engine available on each platform
            engine = "platform"
        }
    }
}
```

With `"platform"`, `engine` is `null` by default and the `HttpClient` is created without engine: Ktor uses the engine
found in the dependencies of each platform. `io.ktor:ktor-client-engine-defaults` (experimental, Ktor 3.6.0+) adds
OkHttp on the JVM and Android, Curl on Linux, Darwin on Apple platforms and Js on JS/Wasm; you can also declare the
engines yourself in each source set. The `ktor-client-cio` dependency is then no longer needed.

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.ktor:ktor-client-engine-defaults:<ktor version>")
        }
    }
}
```

### Testing code that uses a client

Since each client is an interface, it can be replaced by a fake in tests, without any mocking library.
This also works in Kotlin Multiplatform `commonTest`:

```kotlin
val fakeClient =
    object : UserClient {
        override suspend fun getUsers(): UserClient.GetUsersResponse =
            UserClient.GetUsersResponseSuccess(listOf(User(id = "1", name = "Alice")))
        // other operations...
    }
```

Mocking libraries can work too.

### Response body types

The type of the `body` property of each generated response class depends on the declared media type:

| Response media type | Generated `body` type |
|---|---|
| `application/json`, `application/yaml`, `*/*` with a schema | The schema type (`ByteArray` for a `type: string, format: binary` schema) |
| `text/plain`, `text/csv`, … (any `text/*`) | `String` |
| `application/octet-stream`, `image/png`, `application/pdf`, … (any other non-JSON type) | `ByteArray` |
| `text/event-stream` | SSE operation (a `block: suspend ClientSSESession.() -> Unit` callback) |

For text and binary responses, the generated method also sends an `Accept` header with the declared media types.
When a response declares both JSON and a binary type, the JSON schema is used.

```kotlin
when (val response = client.downloadFile("42")) {
    is DownloadFileResponseSuccess -> File("report.pdf").writeBytes(response.body)
    else -> error("download failed: $response")
}
```

The whole body is loaded into memory. Streaming large files with `ByteReadChannel` is not supported yet.

### Inline response schemas

A JSON/YAML response whose schema is an inline object with `properties` (instead of a `$ref`) is generated as a data
class nested in the client interface, named `<Operation>ResponseBody` (e.g. `PlanResponseBody`). When several responses
of the same operation are inline objects, the status code is part of the name (`StopsResponse200Body`,
`StopsResponse404Body`). A free-form `type: object` without `properties` is still typed `JsonElement`.

### Response headers

Every generated response class, including the `*ResponseUnknownFailure` fallback, exposes the raw Ktor
`headers: Headers` of the HTTP response. The property is declared on the sealed base class, so it is available
without a cast:

```kotlin
val response = client.getAccountFollowers(id = "42")
val rateLimitRemaining = response.headers["X-RateLimit-Remaining"]
```

When the spec declares `responses.<code>.headers` (inline or `$ref: #/components/headers/...`), the response class
also gets one typed, nullable property per header. The property returns `null` when the header is missing or
cannot be parsed:

```kotlin
when (val response = client.getAccountFollowers(id = "42")) {
    is GetAccountFollowersResponseSuccess -> {
        val next = response.link              // String?  ("Link" header)
        val remaining = response.xRateLimitRemaining // Int?  ("X-RateLimit-Remaining" header)
    }
    else -> Unit
}
```

| Header schema | Property type |
|---|---|
| `integer` (`int64`) | `Long?` |
| `integer` (other formats) | `Int?` |
| `number` | `Double?` |
| `boolean` | `Boolean?` |
| anything else (`string`, `date-time`, enums, arrays, …) | `String?` (raw value) |

The `Content-Type` response header is ignored, as the OpenAPI specification requires. A header whose property name would clash
with `body`, `headers` or `statusCode` gets a `Header` suffix (e.g. `bodyHeader`).
When several status codes share one response class, the class exposes the union of their headers.
`headers` is `@Transient`, so it is not part of the kotlinx.serialization form of the response classes.

### Parameter serialization

Query, header, path and cookie parameters follow the OpenAPI `style` and `explode` keywords. When they are not
declared, the OpenAPI defaults apply: `form` + `explode: true` for query and cookie parameters, `simple` for path
and header parameters.

| Location | Style | Array `[a, b]` | Object `{x: 1, y: 2}` |
|---|---|---|---|
| query | `form`, `explode: true` (default) | `?id=a&id=b` | `?x=1&y=2` |
| query | `form`, `explode: false` | `?id=a,b` | `?id=x,1,y,2` |
| query | `spaceDelimited` / `pipeDelimited` | `?id=a%20b` / `?id=a\|b` | `?id=x%201%20y%202` / `?id=x\|1\|y\|2` |
| query | `deepObject` | — | `?id[x]=1&id[y]=2` |
| header | `simple` | `a,b` | `x,1,y,2` (`x=1,y=2` with `explode: true`) |
| cookie | `form` | `id=a,b` | `id=x,1,y,2` |
| path | `simple` | `a,b` | — |

Enum values (inline or `$ref`) are sent with their OpenAPI value (`serialName()`), not the Kotlin constant name.
An inline object parameter is typed with a nested class of the client (e.g. `filter: Filter?`).

Objects are flattened at runtime from their JSON form (`ClientConfiguration.json`), so `@SerialName` and enum values
are respected. `null` properties are skipped, and nested objects in `deepObject` become `id[x][z]=…`.
Only objects with declared `properties` are flattened; a free-form object (`type: object` without properties) is
sent with `toString()`.

Unsupported combinations (`matrix` and `label` styles, `deepObject` on a scalar, a delimited style outside query, …)
log a warning at generation time and fall back to the default style of the parameter location. Exploded cookie
arrays and objects are sent unexploded.

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
| `BasicAuthModule` | Adds an `accessToken: String?` parameter to `ClientConfiguration` and adds a statement to the default `httpClientAuthorization` to inject an `Authorization: Bearer <token>` header on every request |
| `KotlinTimeInstantModule` | Maps `type: string, format: date-time` to `kotlin.time.Instant` (stdlib, no extra dependency) |
| `KotlinxDateTimeLocalDateModule` | Maps `type: string, format: date` to `kotlinx.datetime.LocalDate`. **Requires** `org.jetbrains.kotlinx:kotlinx-datetime` in the project compiling the generated code |
| `KotlinUuidModule` | Maps `type: string, format: uuid` to `kotlin.uuid.Uuid` (stdlib, no extra dependency) |

#### Rich types for string formats

By default every `type: string` schema is generated as `String`, whatever its `format`. The three format modules above
are opt-in and independent: enable only the ones you need.

```kotlin
apiClientGenerator {
    generators {
        create("openapi") {
            openApiFile = file("src/main/openapi/openapi.json")
            modulesIds.addAll("KotlinTimeInstantModule", "KotlinxDateTimeLocalDateModule", "KotlinUuidModule")
        }
    }
}

dependencies {
    // only needed for KotlinxDateTimeLocalDateModule
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:<version>")
}
```

The mapping applies everywhere the type is used: model properties, request/response bodies, path, query, header and
form parameters (converted with `toString()`, which produces the ISO-8601 / canonical form), and default values
(rendered as `Instant.parse("…")`, `LocalDate.parse("…")`, `Uuid.parse("…")`). Serialization relies on the serializers
built into kotlinx-serialization (`Instant`, `Uuid`) and kotlinx-datetime (`LocalDate`). The generated code requires
Kotlin 2.4+, where `kotlin.uuid.Uuid` is stable.

`KotlinTimeInstantModule` truncates the `Instant` values it sends (parameters, form fields and JSON bodies) to the
millisecond by default, e.g. `2026-10-09T15:23:39.886Z`: some servers misread the nanosecond precision of
`Instant.toString()`. Received values are parsed whatever their precision. To choose another precision
(`SECONDS`, `MILLISECONDS`, `MICROSECONDS`, or `NANOSECONDS` for plain `Instant.toString()`), declare the module
in `customModules` instead of `modulesIds`:

```kotlin
import org.litote.openapi.ktor.client.generator.module.kotlintimeinstant.InstantPrecision
import org.litote.openapi.ktor.client.generator.module.kotlintimeinstant.KotlinTimeInstantModule

apiClientGenerator {
    generators {
        create("openapi") {
            openApiFile = file("src/main/openapi/openapi.json")
            customModules.add(KotlinTimeInstantModule(InstantPrecision.SECONDS))
        }
    }
}
```

In JSON bodies, the truncation relies on a `DateTimeFormatSerializer` generated next to `ClientConfiguration` and
applied to the model properties (`createdAt: @Serializable(with = DateTimeFormatSerializer::class) Instant`).

With `initApiClientSubproject`, add the kotlinx-datetime dependency through
`initSubproject { additionalDependencies.add("org.jetbrains.kotlinx:kotlinx-datetime:<version>") }`.

To map other formats (e.g. `uri`), implement the `processTypeMapping` hook in a custom module:

```kotlin
override fun processTypeMapping(config: ApiTypeMappingConfig) {
    config.stringFormatTypes["uri"] = StringFormatType("com.example.Uri", parseFunction = "parse")
}
```

`StringFormatType.formatSuffix` (default `.toString()`) is the Kotlin expression suffix appended to a value
to format it as a path, query, header, cookie or form parameter. When it is not `.toString()`, a
`<Format>FormatSerializer` (e.g. `UriFormatSerializer`) using it is generated next to `ClientConfiguration` and
applied to the model properties, so JSON bodies use the same format.

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
> - **SPI via `modulesIds`** — package the module as a library with a `META-INF/services/org.litote.openapi.ktor.client.generator.ApiGeneratorModule` entry, add it to the buildscript classpath, and reference it by ID. The built-in modules (`UnknownEnumValueModule`, `LoggingSl4jModule`, `LoggingKotlinModule`, ...) follow exactly this pattern and can serve as implementation examples.
>
> - **Disable the configuration cache** — if neither approach suits your project, set `org.gradle.configuration-cache=false` in `gradle.properties` (this is the default value).

#### What modules can do

A module can implement any combination of the following hooks — all are no-ops by default:

| Hook | Called when | Can do |
|---|---|---|
| `processConfiguration(ApiConfigurationGeneratorConfig)` | Before `ClientConfiguration` is rendered | Set custom Json properties (`coerceInputValues`, etc.), override the exception-logging lambda, add `String?` constructor parameters, add statements to the default `httpClientAuthorization` lambda (`httpClientAuthorizationStatements`) |
| `processClient(ApiClientGeneratorConfig)` | Before any client class is rendered | Configure client-level rendering options (reserved for future use) |
| `processModel(ApiModelGeneratorConfig)` | Before any model class is rendered | Set a fallback enum constant (`defaultEnumValue`) |
| `processTypeMapping(ApiTypeMappingConfig)` | Once, before models, clients and `ClientConfiguration` are rendered | Map an OpenAPI `string` format to a Kotlin type (`stringFormatTypes["uuid"] = StringFormatType("kotlin.uuid.Uuid")`) |
| `transformClientSpec(ClientSpec): ClientSpec` | For each client, before KotlinPoet rendering | Add, remove or rewrite operations; rename the client; change parameters or response types |
| `transformModelSpec(ModelSpec): ModelSpec` | For each model, before KotlinPoet rendering | Add, remove or rewrite properties; change the model kind (data class, enum, sealed…) |
| `transformFile(GeneratedFileSpec): GeneratedFileSpec` | After KotlinPoet rendering, before writing to disk | Add a file header, rewrite imports, inject code at the text level |

To configure the HTTP client, add statements to `httpClientAuthorizationStatements` rather than replacing
`httpClientAuthorizationDefaultValue`: the statements of all the modules are kept, in order, while
`httpClientAuthorizationDefaultValue` only keeps the value of the last module that sets it (it is applied before
the statements).

```kotlin
override fun processConfiguration(generator: ApiConfigurationGeneratorConfig) {
    generator.additionalStringParameters.add("tenantId")
    generator.httpClientAuthorizationStatements.add("""tenantId?.let { id -> defaultRequest { header("X-Tenant", id) } }""")
    generator.additionalImports.add("io.ktor.client.request" to "header")
}
```

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

The latest SnakeYAML version can be found in the [published version catalog](#using-the-version-catalog) (`openapiKtor.versions.snakeyaml`).

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