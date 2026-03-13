## Prerequisites

- JDK 25
- Gradle 9.4.0+

## Quick Reference

```bash
# MANDATORY after every code change:
./gradlew clean formatKotlin && ./gradlew check

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


## Generator Architecture

The `generator` module follows a **hexagonal architecture** (ports & adapters).

```
generator/src/main/kotlin/
│
│  ← Composition root (public API, wires adapters together)
├── ApiGenerator.kt              ← generate(configuration): parses, configures renderers, generates
│                                   parseClientNames(path): returns client names from a spec
├── ApiGeneratorConfiguration.kt ← public config: operationFilter uses OperationMeta (domain type)
│                                   splitByClient, targetClientName for split-by-client mode
├── ApiGeneratorModule.kt        ← SPI: hook into concrete renderers before generation
│
├── domain/          ← pure business model, zero external dependencies
│   ├── ModelUsageAnalyzer       ← analyzes which clients reference which models (transitively)
│   ├── PartitionedGenerationSpec← result of split: shared spec + per-client specs
│   └── ...
├── port/            ← interfaces (ports): inward and outward contracts
├── adapter/
│   ├── renderer/    ← domain → KotlinPoet FileSpec/TypeSpec
│   └── writer/      ← GeneratedFile → disk
└── application/     ← orchestration via ports, no OpenAPI or KotlinPoet imports
    ├── GenerateCodeService      ← accepts port interfaces, generates from GenerationSpec
    └── GenerationSpecPartitioner← partitions a GenerationSpec into shared + per-client specs
```

**Dependency rules:**
- `domain/` has **no imports** from KotlinPoet, OpenAPI bindings, Ktor, or I/O libraries
- `port/` depends only on `domain/`
- `application/` depends only on `domain/` and `port/` — never on `adapter/`
- `adapter/parser/` and `adapter/renderer/` are independent — they do not import each other
- `adapter/writer/` uses `domain.GeneratedFile` — the `FileSystemWriter` port contains no KotlinPoet types
- `ApiGenerator.kt` (composition root) is the only place that imports from all layers
- `ApiGeneratorModule` SPI uses only `port/*GeneratorConfig` interfaces — never concrete adapter classes


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
