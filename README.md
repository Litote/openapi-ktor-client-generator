# OpenAPI Ktor Client Generator

A powerful Gradle plugin that transforms OpenAPI v3 specifications into production-ready Kotlin Ktor client code. 
You can customize the generated clients and models to match your project's specific needs.

## Prerequisites

- JDK 17+
- Gradle 9+

## Installation

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("org.litote.openapi.ktor.client.generator.gradle") version "0.1.1"
}
```

## Configuration

Configure the plugin in your `build.gradle.kts`:

```kotlin
apiClientGenerator {
    generators {
        create("openapi") { // Task name: generateOpenapi
            outputDirectory = file("build/generated")
            openApiFile = file("src/main/openapi/openapi.json")
            basePackage = "com.example.api"
            // Optional: Add more configuration options below
        }
        // You can create multiple generators with different names
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

| Property | Description | Default value | Allowed values |
| --- | --- | --- | --- |
| `openApiFile` | OpenAPI v3 source file | `file("src/main/openapi/${name}.json")` | Any existing OpenAPI file |
| `outputDirectory` | Target directory for generated sources (a `src/main/kotlin` subdirectory will be added) | `file("build/api-${name}")` | Any relative directory |
| `basePackage` | Base package for all generated classes | `org.example` | Any valid package name |
| `allowedPaths` | Restrict generation to a subset of OpenAPI paths | empty (all paths are generated) | Any subset of paths defined in the OpenAPI spec |
| `modulesIds` | Extra generation modules to enable | Empty (no modules) | `UnknownEnumValueModule`, `LoggingSl4jModule` |

## Project Structure

- `generator/` - Core code generation logic and OpenAPI parsing
- `gradle-plugin/` - Gradle plugin implementation and task integration
- `e2e/` - End-to-end test project(s) with example configurations
- `module/` - Optional modules for extended functionality
- `shared/` - Shared utilities and code used across modules
