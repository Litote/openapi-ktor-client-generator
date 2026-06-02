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

After generation, each API tag produces a client class (e.g. `UserClient`, `PetClient`).
All clients share by default a single `ClientConfiguration` instance.

### Minimal example

```kotlin
val config = ClientConfiguration() // default generated configuration class
val client = UserClient(config) // UserClient is the generated client

val users = client.getUsers() // returns a sealed class to manage errors
```

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

## Advanced usage and troubleshooting

See [ADVANCED_USAGE.md](CONTRIBUTING.md)

## Generating subprojects

The plugin provides an `initApiClientSubproject` task to generate ready-to-use Gradle subprojects.
See **[PROJECT_GENERATION.md](PROJECT_GENERATION.md)** for the full documentation: single/multi-module.

## Contributing & internal architecture

See [CONTRIBUTING.md](CONTRIBUTING.md)
