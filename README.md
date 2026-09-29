# OpenAPI Ktor Client Generator

![Plugin Version](https://img.shields.io/gradle-plugin-portal/v/org.litote.openapi.ktor.client.generator.gradle)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Litote_openapi-ktor-client-generator&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Litote_openapi-ktor-client-generator)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Litote_openapi-ktor-client-generator&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Litote_openapi-ktor-client-generator)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Litote_openapi-ktor-client-generator&metric=bugs)](https://sonarcloud.io/summary/new_code?id=Litote_openapi-ktor-client-generator)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Litote_openapi-ktor-client-generator&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=Litote_openapi-ktor-client-generator)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Litote_openapi-ktor-client-generator&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=Litote_openapi-ktor-client-generator)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Litote_openapi-ktor-client-generator&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=Litote_openapi-ktor-client-generator)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Litote_openapi-ktor-client-generator&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=Litote_openapi-ktor-client-generator)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Litote_openapi-ktor-client-generator&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=Litote_openapi-ktor-client-generator)
[![Apache2 license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

A Gradle plugin that transforms OpenAPI v3.x specifications into production-ready Kotlin Ktor client code.

The generated client code is **fully KMP-compatible**.

You can customize the generated clients and models to match your project's specific needs.

## Prerequisites

- JDK 17+
- Gradle 9+

## Installation

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("org.litote.openapi.ktor.client.generator.gradle") version "<last version>"
}
```

Replace `<last version>` with the latest release: ![Plugin Version](https://img.shields.io/gradle-plugin-portal/v/org.litote.openapi.ktor.client.generator.gradle)

## Configuration

Configure the plugin in your `build.gradle.kts`:

```kotlin
apiClientGenerator {
    generators {
        create("openapi") { // registers a task named generateOpenapi
            outputDirectory = file("build/generated")
            openApiFile = file("src/main/openapi/openapi.json")
            basePackage = "com.example.api"
        }
        // you can declare multiple generators
    }
}
```

A full working example is available in [e2e/build.gradle.kts](e2e/build.gradle.kts).

## Usage

Run the generation task directly:

```bash
./gradlew generateOpenapi
```

Or let it run automatically as part of the build:

```bash
./gradlew build
```

The generated code is placed in the configured `outputDirectory`. You also need to add Ktor, kotlinx-serialization, and kotlinx-coroutines to your dependencies for the project to compile.

### OpenAPI spec

The generator accepts OpenAPI V3 specification files in both **JSON** and **YAML** format.

## Using the generated client

After generation, each API tag produces a client (e.g. `UserClient`, `PetClient`) made of:

- an interface `UserClient` declaring the operations (with their default parameter values) and the nested response types (`UserClient.GetUsersResponse`, …);
- a factory function `UserClient(configuration)` returning the default implementation;
- the Ktor implementation `DefaultUserClient(configuration) : UserClient`.

All clients share by default a single `ClientConfiguration` instance.

### Minimal example

```kotlin
val config = ClientConfiguration() // default generated configuration class
val client: UserClient = UserClient(config) // factory function returning a DefaultUserClient

val users = client.getUsers() // returns a sealed class to manage errors
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

## Gradle task configuration properties

### Root properties

| Property         | Description                                                             | Default value | Allowed values    |
|------------------|-------------------------------------------------------------------------|---------------|-------------------|
| `generators`     | One or more generator configurations                                    | `{}`          | Any configuration |
| `skip`           | Skip all client generation                                              | `false`       | Boolean           |
| `initSubproject` | Options for the `initApiClientSubproject` project generation task       | see [PROJECT_GENERATION.md](PROJECT_GENERATION.md) | |

### Generator properties

| Property           | Description                                                                          | Default value                           | Allowed values                                                          |
|--------------------|--------------------------------------------------------------------------------------|------------------------------------------|-------------------------------------------------------------------------|
| `openApiFile`      | OpenAPI v3 source file                                                               | `file("src/main/openapi/${name}.json")` | Any existing OpenAPI file                                               |
| `outputDirectory`  | Target directory for generated sources (`src/main/kotlin` is appended automatically) | `file("build/api-${name}")`             | Any relative directory                                                  |
| `basePackage`      | Base package for all generated classes                                               | `org.example`                           | Any valid package name                                                  |
| `allowedPaths`     | Restrict generation to a subset of OpenAPI paths                                     | empty (all paths generated)             | Any subset of paths defined in the spec                                 |
| `modulesIds`       | Built-in module IDs to enable (loaded from classpath via SPI)                        | empty                                   | Any module defined via SPI (see [ADVANCED_USAGE.md](ADVANCED_USAGE.md)) |
| `customModules`    | Custom module instances defined inline in the build script                           | empty                                   | Any `ApiGeneratorModule` implementation (see [ADVANCED_USAGE.md](ADVANCED_USAGE.md))            |
| `skip`             | Skip this generator                                                                  | `false`                                 | Boolean                                                                 |
| `splitByClient`    | Enable split-by-client mode — see [PROJECT_GENERATION.md](PROJECT_GENERATION.md)     | `false`                                 | Boolean                                                                 |
| `targetClientName` | In split mode: name of the client to generate (`null` = shared subproject) — see [PROJECT_GENERATION.md](PROJECT_GENERATION.md) | `null`                                  | Any tag-derived client name from the spec                               |

### Rich types for date, date-time and uuid

By default, `string` schemas are generated as `String` whatever their `format`. Enable the opt-in modules to get rich types:

| Module ID | OpenAPI format | Kotlin type | Extra dependency |
|---|---|---|---|
| `KotlinTimeInstantModule` | `date-time` | `kotlin.time.Instant` | none |
| `KotlinxDateTimeLocalDateModule` | `date` | `kotlinx.datetime.LocalDate` | `org.jetbrains.kotlinx:kotlinx-datetime` |
| `KotlinUuidModule` | `uuid` | `kotlin.uuid.Uuid` | none |

```kotlin
modulesIds.addAll("KotlinTimeInstantModule", "KotlinxDateTimeLocalDateModule", "KotlinUuidModule")
```

See [ADVANCED_USAGE.md](ADVANCED_USAGE.md#rich-types-for-string-formats) for details.

## Advanced usage and troubleshooting

See [ADVANCED_USAGE.md](ADVANCED_USAGE.md)

## Generating subprojects

The plugin provides an `initApiClientSubproject` task to generate ready-to-use Gradle subprojects.
See **[PROJECT_GENERATION.md](PROJECT_GENERATION.md)** for the full documentation: single/multi-module.

## Contributing & internal architecture

See [CONTRIBUTING.md](CONTRIBUTING.md)
