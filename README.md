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

A Gradle plugin that transforms OpenAPI v3 specifications into production-ready Kotlin Ktor client code.

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

Where `<last version>` is ![Plugin Version](https://img.shields.io/gradle-plugin-portal/v/org.litote.openapi.ktor.client.generator.gradle)

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

## Configuration Properties

### Root properties

| Property         | Description                                                             | Default value | Allowed values    |
|------------------|-------------------------------------------------------------------------|---------------|-------------------|
| `generators`     | One or more generator configurations                                    | `{}`          | Any configuration |
| `skip`           | Skip all client generation                                              | `false`       | Boolean           |
| `initSubproject` | Options for the `initApiClientSubproject` project generation task       | see [PROJECT_GENERATION.md](PROJECT_GENERATION.md) | |

### Generator properties

| Property           | Description                                                                              | Default value                           | Allowed values                                    |
|--------------------|------------------------------------------------------------------------------------------|------------------------------------------|---------------------------------------------------|
| `openApiFile`      | OpenAPI v3 source file                                                                   | `file("src/main/openapi/${name}.json")` | Any existing OpenAPI file                         |
| `outputDirectory`  | Target directory for generated sources (`src/main/kotlin` is appended automatically)    | `file("build/api-${name}")`             | Any relative directory                            |
| `basePackage`      | Base package for all generated classes                                                   | `org.example`                           | Any valid package name                            |
| `allowedPaths`     | Restrict generation to a subset of OpenAPI paths                                         | empty (all paths generated)             | Any subset of paths defined in the spec           |
| `modulesIds`       | Optional generation modules to enable                                                    | empty                                   | `UnknownEnumValueModule`, `LoggingSl4jModule`, `LoggingKotlinModule`     |
| `skip`             | Skip this generator                                                                      | `false`                                 | Boolean                                           |
| `splitByClient`    | Enable split-by-client mode — see [PROJECT_GENERATION.md](PROJECT_GENERATION.md)        | `false`                                 | Boolean                                           |
| `targetClientName` | In split mode: name of the client to generate (`null` = shared subproject)              | `null`                                  | Any tag-derived client name from the spec         |

## Generating a new subproject

The plugin provides an `initApiClientSubproject` task to generate a ready-to-use Gradle subproject.
See **[PROJECT_GENERATION.md](PROJECT_GENERATION.md)** for the full documentation: single/multi-module, version catalog support, extra generator configuration, and Kotlin Multiplatform (KMP) support.

## KMP limitations

### YAML content type

The `YamlContentConverter` (see below) generated when your API uses `application/yaml` content types depends on
[SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml), which is a **JVM-only** library. If you
target non-JVM platforms, place the SnakeYAML dependency in a `jvmMain` source set and implement a
platform-specific YAML converter for other targets.

### `LoggingSl4jModule` and KMP

The `LoggingSl4jModule` generates code that uses `org.slf4j.LoggerFactory`, which is JVM-only.
Do not use this module in KMP projects targeting non-JVM platforms.

## Advanced usage

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

### Contributing & internal architecture

See [CONTRIBUTING.md](CONTRIBUTING.md) for the hexagonal architecture diagram.
