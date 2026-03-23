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

## Configuration Properties

### Root properties

| Property         | Description                                                             | Default value | Allowed values    |
|------------------|-------------------------------------------------------------------------|---------------|-------------------|
| `generators`     | One or more generator configurations                                    | `{}`          | Any configuration |
| `skip`           | Skip all client generation                                              | `false`       | Boolean           |
| `initSubproject` | Options for the `initApiClientSubproject` project generation task       | see [PROJECT_GENERATION.md](PROJECT_GENERATION.md) | |

### Generator properties

| Property           | Description                                                                          | Default value                           | Allowed values                            |
|--------------------|--------------------------------------------------------------------------------------|------------------------------------------|-------------------------------------------|
| `openApiFile`      | OpenAPI v3 source file                                                               | `file("src/main/openapi/${name}.json")` | Any existing OpenAPI file                 |
| `outputDirectory`  | Target directory for generated sources (`src/main/kotlin` is appended automatically) | `file("build/api-${name}")`             | Any relative directory                    |
| `basePackage`      | Base package for all generated classes                                               | `org.example`                           | Any valid package name                    |
| `allowedPaths`     | Restrict generation to a subset of OpenAPI paths                                     | empty (all paths generated)             | Any subset of paths defined in the spec   |
| `modulesIds`       | Built-in module IDs to enable (loaded from classpath via SPI)                        | empty                                   | `UnknownEnumValueModule`, `LoggingSl4jModule`, `LoggingKotlinModule`, `BasicAuthModule` |
| `customModules`    | Custom module instances defined inline in the build script                           | empty                                   | Any `ApiGeneratorModule` implementation (see advanced usage)         |
| `skip`             | Skip this generator                                                                  | `false`                                 | Boolean                                   |
| `splitByClient`    | Enable split-by-client mode — see [PROJECT_GENERATION.md](PROJECT_GENERATION.md)     | `false`                                 | Boolean                                   |
| `targetClientName` | In split mode: name of the client to generate (`null` = shared subproject) — see [PROJECT_GENERATION.md](PROJECT_GENERATION.md) | `null`                                  | Any tag-derived client name from the spec |

## Generating a new subproject

The plugin provides an `initApiClientSubproject` task to generate a ready-to-use Gradle subproject.
See **[PROJECT_GENERATION.md](PROJECT_GENERATION.md)** for the full documentation: single/multi-module, version catalog support, extra generator configuration, and Kotlin Multiplatform (KMP) support.

## Advanced usage

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

### Contributing & internal architecture

See [CONTRIBUTING.md](CONTRIBUTING.md) for the hexagonal architecture diagram.
