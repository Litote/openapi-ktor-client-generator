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
├── ApiGeneratorConfiguration.kt ← public config: operationFilter uses OperationMeta (domain type)
├── ApiGeneratorModule.kt        ← SPI: hook into concrete renderers before generation
│
├── domain/          ← pure business model, zero external dependencies
│   ├── GenerationSpec           ← top-level: everything to generate
│   ├── ClientSpec               ← one Ktor client class per tag
│   ├── OperationSpec            ← one suspend method per HTTP operation
│   ├── ModelSpec (sealed)       ← DataClass | Enum | SealedClass | Object | Alias
│   ├── DomainType (sealed)      ← Primitive | List | Set | Map | ModelReference | Inline | Json
│   ├── SecuritySchemeSpec       ← public: API-key security scheme (name, keyName, location, paramName)
│   ├── SecuritySchemeLocation   ← public enum: HEADER | QUERY
│   ├── GeneratedFile            ← internal: pure representation of a generated source file
│   └── …
│
├── port/            ← interfaces (ports): inward and outward contracts
│   ├── SpecificationParser      ← OpenAPI file → GenerationSpec
│   ├── ConfigurationRenderer    ← renders client configuration class to disk
│   ├── ClientRenderer           ← renders one client class to disk (internal fun interface)
│   ├── ModelRenderer            ← renders one model class to disk (internal fun interface)
│   ├── ConfigurationGeneratorConfig ← (public) module hook: exceptionLogging, jsonProperties
│   ├── ClientGeneratorConfig    ← (public) module hook: extensibility for client rendering
│   ├── ModelGeneratorConfig     ← (public) module hook: defaultEnumValue
│   └── FileSystemWriter         ← GeneratedFile → disk
│
├── adapter/
│   ├── parser/      ← OpenAPI bindings → domain (no KotlinPoet in public surface)
│   │   ├── ApiModel             ← (internal) wraps OpenAPIV3 parsed model
│   │   ├── ApiOperation         ← (internal) intermediate operation representation
│   │   ├── ApiClassProperty     ← (internal) intermediate property representation
│   │   ├── OpenApiSpecificationParser  ← implements SpecificationParser
│   │   ├── TypeNameConverter    ← KotlinPoet TypeName → DomainType
│   │   ├── ParserTypeUtils      ← (internal) TypeName predicates for parser use
│   │   └── ParserNameUtils      ← (internal) name utilities for parser use
│   │
│   ├── renderer/    ← domain → KotlinPoet FileSpec/TypeSpec
│   │   ├── ApiModelGenerator    ← implements ModelGeneratorConfig; ModelSpec → TypeSpec
│   │   ├── ApiClientGenerator   ← implements ClientGeneratorConfig; ClientSpec → TypeSpec
│   │   ├── ApiClientConfigurationGenerator ← implements ConfigurationRenderer + ConfigurationGeneratorConfig
│   │   ├── OperationBuilder, ResponseBuilder, ClientGenerationContext
│   │   ├── DomainTypeMapper     ← DomainType → KotlinPoet TypeName
│   │   ├── DefaultValueMapper   ← DefaultValue → CodeBlock
│   │   └── KotlinPoets, KtorPoets  ← KotlinPoet utilities (incl. FileSpec.toGeneratedFile())
│   │
│   └── writer/      ← GeneratedFile → disk
│       └── KotlinPoetFileWriter ← implements FileSystemWriter (no KotlinPoet in port)
│
└── application/     ← orchestration via ports, no OpenAPI or KotlinPoet imports
    └── GenerateCodeService      ← accepts port interfaces, generates from GenerationSpec
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
