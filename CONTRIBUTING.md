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
shared/                         → Shared abstractions (utilities)
generator/                      → Composition root — wires all sub-modules together
  generator:domain              → Pure domain model (zero external dependencies)
  generator:port                → Port interfaces (inward & outward contracts)
  generator:config              → Public API types (ApiGeneratorConfiguration, ApiGeneratorModule, GenerationResult)
  generator:application         → Orchestration services (no adapter imports)
  generator:adapter-writer      → File system writer adapter
  generator:adapter-parser      → OpenAPI specification parser adapter
  generator:adapter-renderer    → Kotlin/KotlinPoet code renderer adapter
gradle-plugin/                  → Gradle integration (GeneratorPlugin, tasks)
module/unknown-enum-value/      → Handles unmapped enum values
module/logging-sl4j/            → SLF4J logging in generated clients
convention/                     → Build convention plugins
e2e/                            → End-to-end tests (separate Gradle project)
```
---


## Generator Architecture

The `generator` module follows a **hexagonal architecture** (ports & adapters) enforced by
Gradle sub-module boundaries. Adding an illegal dependency (e.g. KotlinPoet in `domain`) causes
a **compile error**, not just a lint warning.

### Gradle Dependency Graph (enforced at compile time)

```
generator:domain         → :shared
generator:port           → generator:domain
generator:config         → generator:domain + generator:port
generator:application    → generator:domain + generator:port
generator:adapter-writer → generator:domain + generator:port
generator:adapter-parser → generator:domain + generator:port + generator:config
generator:adapter-renderer → generator:domain + generator:port + generator:config + generator:adapter-writer
generator (root)         → generator:config + generator:application + generator:adapter-parser
                           + generator:adapter-renderer + generator:adapter-writer
```

### Sub-module Contents

| Sub-module | Package(s) | Contents |
|---|---|---|
| `generator:domain` | `*.domain` + `*.generator` (enums) | `GenerationSpec`, `ClientSpec`, `ModelSpec`, all domain types; `SplitGranularity`, `SharedModelGranularity` |
| `generator:port` | `*.port` | `SpecificationParser`, `ClientRenderer`, `ModelRenderer`, `ConfigurationRenderer`, `FileSystemWriter`, `*GeneratorConfig` interfaces |
| `generator:config` | `*.generator` | `ApiGeneratorConfiguration`, `ApiGeneratorModule`, `GenerationResult` |
| `generator:application` | `*.application` | `GenerateCodeService`, `GenerationSpecPartitioner` |
| `generator:adapter-writer` | `*.adapter.writer` | `KotlinPoetFileWriter` |
| `generator:adapter-parser` | `*.adapter.parser` | `OpenApiSpecificationParser`, `ApiModel`, `TypeNameConverter`, helpers |
| `generator:adapter-renderer` | `*.adapter.renderer` | `ApiClientGenerator`, `ApiModelGenerator`, `ApiClientConfigurationGenerator`, builders, helpers |
| `generator` (root) | `*.generator` | `ApiGenerator.kt` (composition root — the only file that imports all layers) |

### Key Architectural Invariants (Gradle-enforced)

- **`generator:domain`** compiles with **zero** KotlinPoet / OpenAPI bindings / Ktor / I/O dependencies
- **`generator:port`** depends only on `generator:domain` — no `ApiGeneratorConfiguration` in port interfaces
- **`generator:application`** cannot see adapter classes (not in its dependency graph)
- **`generator:adapter-parser`** cannot see renderer code (no `generator:adapter-renderer` dep)
- **`generator:adapter-renderer`** cannot see parser code (no `generator:adapter-parser` dep)
- The composition root `generator` is the **only** module that can wire all layers together

### `SpecificationParser` Port Design

`SpecificationParser.parse(operationFilter)` receives **only** a domain-typed filter.
The full `ApiGeneratorConfiguration` is injected into `OpenApiSpecificationParser` at
**construction time** in the composition root, keeping the port free of config types.

```
ApiGenerator.kt (root)
  └─► OpenApiSpecificationParser(configuration)  ← adapter-parser
        .parse(configuration.operationFilter)     ← port method (no ApiGeneratorConfiguration here)
```


## Core Components Reference

| Component | Responsibility |
|-----------|----------------|
| `ApiClientConfigurationGenerator` | Generates `ClientConfiguration.kt` and (if YAML) `YamlContentConverter.kt` |
| `YamlContentConverterGenerator`   | Generates `YamlContentConverter.kt` — bridges YAML ↔ JSON via SnakeYAML |
| `OperationBuilder`                | Builds per-operation methods with correct `contentType()` headers |

## YAML Support

When an OpenAPI spec contains `application/yaml` or `application/x-yaml` content types (in request bodies or responses), the generator automatically:

1. Sets `ClientConfigurationSpec.hasYamlContentType = true` (detected in `OpenApiSpecificationParser`)
2. Generates `YamlContentConverter.kt` in the client package (`YamlContentConverterGenerator`)
3. Registers the converter in `ContentNegotiation` for both YAML content types (`ApiClientConfigurationGenerator`)
4. Sets `contentType(ContentType("application", "yaml"))` on operations with YAML request bodies (`OperationBuilder`)

The `YamlContentConverter` bridges YAML ↔ `kotlinx.serialization` via SnakeYAML:
- **Deserialize**: YAML bytes → SnakeYAML `Map/List` → `JsonElement` → `kotlinx.serialization`
- **Serialize**: `kotlinx.serialization` JSON string → SnakeYAML → YAML bytes

Users must add `org.yaml:snakeyaml` to their project dependencies when YAML endpoints are used.

---

## Update dependencies

```bash
# 1. Update version catalog to latest available versions
./gradlew versionCatalogUpdate

# 2. Regenerate dependency verification metadata (MANDATORY after any dependency change)
./gradlew updateVerificationMetadata
```

> ⚠️ **Always run `updateVerificationMetadata` after any dependency upgrade.**
> Skipping this step causes `Dependency verification failed` errors in IntelliJ and for other contributors.

The `updateVerificationMetadata` task rewrites `gradle/verification-metadata.xml` with fresh SHA-256
checksums for all resolved artifacts. It preserves the `trusted-artifacts` rules (sources JARs,
javadoc JARs, `.pom`, `.module` files) which are IDE-only and exempt from checksum verification.

## Publishing

```bash
# Maven 
./gradlew publish
# Gradle portal 
./gradlew publishPlugins
```
