# OpenAPI Ktor Client Generator

![Plugin Version](https://img.shields.io/gradle-plugin-portal/v/org.litote.openapi.ktor.client.generator.gradle)
[![Apache2 license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

A powerful Gradle plugin that transforms OpenAPI v3 specifications into production-ready Kotlin Ktor client code. 
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
        create("openapi") { // create a new task name: generateOpenapi
            outputDirectory = file("build/generated")
            openApiFile = file("src/main/openapi/openapi.json")
            basePackage = "com.example.api"
            // Optional: Add more configuration options below
        }
        // You can create multiple tasks with different names
    }
}
```

For a complete example with all available options, see [e2e/build.gradle.kts](e2e/build.gradle.kts).

## Usage

Run the generation task directly:

```bash
./gradlew generateOpenapi
```

Or trigger generation as part of the build process:

```bash
./gradlew build
```

This will generate Ktor client code based on your OpenAPI specification and plugin configuration. The generated code will be placed in the configured `outputDirectory`.

## Configuration Properties

### Root properties

| Property       | Description                 | Default value                   | Allowed values                                  |
|----------------|-----------------------------|---------------------------------|-------------------------------------------------|
| `generators`   | Generators configuration    | `{}`                            | Any configuration                               |
| `skip`         | Skip all clients generation | false                           | Boolean                                         |

### Generator properties

| Property | Description                                                                             | Default value | Allowed values |
| --- |-----------------------------------------------------------------------------------------| --- | --- |
| `openApiFile` | OpenAPI v3 source file                                                                  | `file("src/main/openapi/${name}.json")` | Any existing OpenAPI file |
| `outputDirectory` | Target directory for generated sources (a `src/main/kotlin` subdirectory will be added) | `file("build/api-${name}")` | Any relative directory |
| `basePackage` | Base package for all generated classes                                                  | `org.example` | Any valid package name |
| `allowedPaths` | Restrict generation to a subset of OpenAPI paths                                        | empty (all paths are generated) | Any subset of paths defined in the OpenAPI spec |
| `modulesIds` | Extra generation modules to enable                                                      | Empty (no modules) | `UnknownEnumValueModule`, `LoggingSl4jModule` |
| `skip`         | Skip this client generation                                                             | false                           | Boolean                                         |

## Troubleshooting

### Gradle error: Implicit dependencies between tasks

If you get an error with this message, the generator tasks have implicit dependencies with other tasks.
(see https://docs.gradle.org/current/userguide/validation_problems.html#implicit_dependency)

Add the dependencies explicitly in the gradle.kts file:

```kotlin
project
    .tasks
    .named { name ->
        //any condition to match the tasks names
        name.contains("whatever")
    }
    .configureEach {
        project.tasks.withType(org.litote.openapi.ktor.client.generator.plugin.GenerateTask::class.java).forEach {
            dependsOn(it) 
        }
    }
```

### Linter errors

Generated code is not linted. Just ignore the errors by adding a `.editorconfig` file to your project,
for example for ktlint:

```
[build/**/*]
ktlint = disabled
```




