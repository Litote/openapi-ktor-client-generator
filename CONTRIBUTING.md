## Prerequisites

- JDK 25
- Gradle 9.4.0+

## Quick Reference

```bash
# MANDATORY after every code change:
./gradlew formatKotlin && ./gradlew check

# Run specific tests:
./gradlew :generator:test
./gradlew :generator:test --tests "*.ParsingTests"

# E2E testing (separate project):
./gradlew publishToMavenLocal
cd e2e && ./gradlew build

# Debug:
./gradlew build --info
```

## Module Architecture

```
shared/                 → Shared abstractions (ConfigurationOptions, utilities)
generator/              → Code generation engine (OpenApiParser, KotlinCodeGenerator)
gradle-plugin/          → Gradle integration (GeneratorPlugin, tasks)
module/unknown-enum-value/ → Handles unmapped enum values
module/logging-sl4j/    → SLF4J logging in generated clients
convention/             → Build convention plugins
e2e/                    → End-to-end tests (separate Gradle project)
```
---

## Core Components Reference

| Component | Responsibility |
|-----------|----------------|
| `GeneratorPlugin` | Gradle plugin, orchestrates tasks |
| `ApiClientGenerator` | Main generation pipeline |
| `OpenApiParser` | Parses OpenAPI v3, builds internal model |
| `KotlinCodeGenerator` | Generates Kotlin code via KotlinPoet |
| `GenerationModule` | Extension interface for optional features |

---

## Update dependencies

```bash
./gradlew versionCatalogUpdate
```

## Publishing

```bash
# Maven 
./gradlew publish
# Gradle portal 
./gradlew publishPlugins
```
