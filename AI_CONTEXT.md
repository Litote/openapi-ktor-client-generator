# AI Context — OpenAPI Ktor Client Generator

> Codebase analysis for AI agents. Keep this file up-to-date after significant changes.

---

## Module Structure

```
openapi-ktor-client-generator/
├── shared/                       → Common string/collection utilities (capitalize, snakeToCamelCase, …)
├── generator/                    → Core code generation engine (hexagonal architecture)
├── gradle-plugin/                → Gradle plugin integration layer
├── module/
│   ├── unknown-enum-value/       → Optional module: unknown enum value handling
│   ├── logging-sl4j/             → Optional module: SLF4J logging
│   └── logging-kotlin/           → Optional module: kotlin-logging (oshai) logging
├── convention/                   → Build convention plugins
├── e2e/                          → End-to-end test project
├── settings.gradle.kts
└── build.gradle.kts
```

---

## Generator Module (`generator/`) — Hexagonal Architecture (Gradle-enforced)

The `generator` module is split into **7 Gradle sub-modules**. Dependency violations cause
compile errors, not just warnings.

### Gradle Dependency Graph

```mermaid
graph TD
    shared[":shared"]
    domain["generator:domain"]
    port["generator:port"]
    config["generator:config"]
    app["generator:application"]
    writer["generator:adapter-writer"]
    parser["generator:adapter-parser"]
    renderer["generator:adapter-renderer"]
    root["generator (root)"]

    domain --> shared
    port --> domain
    config --> domain
    config --> port
    app --> domain
    app --> port
    writer --> domain
    writer --> port
    parser --> domain
    parser --> port
    parser --> config
    renderer --> domain
    renderer --> port
    renderer --> config
    renderer --> writer
    root --> config
    root --> app
    root --> parser
    root --> renderer
    root --> writer
```

### Sub-module Contents

| Sub-module | Root package | Key classes |
|---|---|---|
| `generator:domain` | `*.domain` | `GenerationSpec`, `ClientSpec`, `OperationSpec`, `ModelSpec` (sealed), `DomainTypeSpec` (sealed), `ModelPropertySpec`, `OperationParameterSpec`, `RequestBodySpec`, `ResponseEntrySpec`, `FormFieldSpec`, `ClientConfigurationSpec`, `SecuritySchemeSpec`, `ComponentParameterSpec`, `DefaultValueSpec`, `OperationMetaSpec`, `ParameterLocationSpec`, `ModelUsageAnalyzer` (top-level fns), `PartitionedGenerationSpec`, `PerClientGenerationSpec`, `SharedGroupSpec`, `GeneratedFileSpec` |
| `generator:port` | `*.port` | `ApiSpecificationParser` (parse takes only `operationFilter`), `ApiConfigurationRenderer`, `ApiClientRenderer`, `ApiModelRenderer`, `ApiFileSystemWriter`, `ApiConfigurationGeneratorConfig`, `ApiClientGeneratorConfig`, `ApiModelGeneratorConfig` |
| `generator:config` | `*.generator` | `ApiGeneratorConfiguration`, `ApiGeneratorModule`, `GenerationResult`, `SplitGranularity`, `SharedModelGranularity` |
| `generator:application` | `*.application` | `GenerateCodeService`, `GenerationSpecPartitioner` |
| `generator:adapter-writer` | `*.adapter.writer` | `KotlinPoetFileWriter` |
| `generator:adapter-parser` | `*.adapter.parser` | `OpenApiSpecificationParser(configuration)`, `ApiModel`, `TypeNameConverter`, `ParserNames`, `ParserTypes`, `ApiOperation`, `ApiClassProperty` |
| `generator:adapter-renderer` | `*.adapter.renderer` | `ApiClientGenerator`, `ApiModelGenerator`, `ApiClientConfigurationGenerator`, `YamlContentConverterGenerator`, `OperationBuilder`, `ResponseBuilder`, `KotlinPoets`, `KtorPoets` |
| `generator` (root) | `*.generator` | `ApiGenerator.kt` — composition root, the ONLY file importing all layers |

### Architectural Invariants (Gradle-enforced)

- **`generator:domain`**: zero KotlinPoet / OpenAPI bindings / Ktor / I/O in dependency graph
- **`generator:port`**: depends only on `generator:domain` — no `ApiGeneratorConfiguration` in port interfaces
- **`generator:application`**: cannot see any adapter class (not in dependency graph)
- **`generator:adapter-parser`**: cannot see renderer code
- **`generator:adapter-renderer`**: cannot see parser code
- `ApiGenerator.kt` is the **only** place that imports all layers

### ApiSpecificationParser Port Design

`parse(operationFilter)` takes only a domain-typed filter. `ApiGeneratorConfiguration` is
injected at **construction time** of `OpenApiSpecificationParser` in the composition root:

```kotlin
OpenApiSpecificationParser(configuration).parse(configuration.operationFilter)
```

---

## Public API (`ApiGenerator.kt`)

```kotlin
// Main entry point — generates everything (or a split portion when splitByClient=true)
fun generate(configuration: ApiGeneratorConfiguration): GenerationResult

// Returns the list of client class names parsed from an OpenAPI spec (used by InitSubprojectTask)
fun parseClientNames(openApiFilePath: String, splitGranularity: SplitGranularity = BY_TAG): List<String>

// Returns all shared client groups (models shared between a specific set of clients)
fun parseSharedClientGroups(openApiFilePath: String, splitGranularity: SplitGranularity = BY_TAG): List<SharedClientGroup>

data class SharedClientGroup(
    val clientGroup: Set<String>,          // exact set of clients sharing these models
    val directoryName: String,             // e.g. "shared-order-user"
    val packageSuffix: String,             // e.g. "sharedOrderUser"
)

data class ApiGeneratorConfiguration(
    val openApiFile: String,
    val outputDirectory: String,
    val basePackage: String = "org.example",
    val operationFilter: (OperationMetaSpec) -> Boolean = { true },
    val modelPackage: String = "$basePackage.model",
    val clientPackage: String = "$basePackage.client",
    val modules: List<ApiGeneratorModule> = emptyList(),
    // Split-by-client mode:
    val splitByClient: Boolean = false,
    val targetClientName: String? = null,      // null = generate shared; "Foo" = generate FooClient + private models
    val sharedBasePackage: String? = null,      // base package of the shared subproject
    // Granularity features:
    val splitGranularity: SplitGranularity = BY_TAG,
    val sharedModelGranularity: SharedModelGranularity = SHARED_ALL,
    val targetSharedGroup: Set<String>? = null, // generate only models for this client group
    val modelPackageOverrides: Map<String, String> = emptyMap(), // model name → package
)
```

### `SplitGranularity` — Client split granularity

Controls the key used to group OpenAPI operations into clients:

| Value | Key | Example client name |
|-------|-----|---------------------|
| `BY_TAG` (default) | tag | `UserClient` |
| `BY_TAG_AND_PATH` | tag + sanitized path | `UserGetV1UsersIdClient` |
| `BY_TAG_AND_OPERATION` | tag + path + method | `UserGetV1UsersIdGetClient` |

### `SharedModelGranularity` — Shared model granularity

Controls how models shared by multiple clients are grouped:

| Value | Behavior |
|-------|----------|
| `SHARED_ALL` (default) | All shared models go to one `shared/` subproject |
| `SHARED_PER_GROUP` | Models are grouped by the exact set of clients using them; each unique group gets its own subproject (e.g. `shared-order-user/`) |

### Split-by-client generation logic

When `splitByClient = true`:
1. Parse the full spec
2. `GenerationSpecPartitioner.partition(spec)` → `PartitionedGenerationSpec` (always computes per-group sharedGroups)
3. `targetClientName == null`, `targetSharedGroup == null` → generate global shared (ClientConfiguration + orphan/global models)
4. `targetSharedGroup == Set("OrderClient","UserClient")` → generate shared models for that exact client group
5. `targetClientName == "FooClient"` → generate FooClient + models used ONLY by FooClient, no config

Model placement rules:
- Used by 2+ clients → shared (SHARED_ALL) or per-group subproject (SHARED_PER_GROUP)
- Used by exactly 1 client → that client's subproject
- Used by 0 clients (orphan) → global shared
- If a SealedClass is shared → all its subtypes are shared too (propagated)

### `modelPackageOverrides`

Map of model name → package to use when generating type references. Applied in both `ApiModelGenerator` (model property types) and `OperationBuilder`/`ResponseBuilder` (client method types). Used when models live in different subprojects with different packages.

---

## Gradle Plugin (`gradle-plugin/`)

### Key files

| File | Role |
|------|------|
| `GeneratorPlugin.kt` | `Plugin<Project>` — registers tasks, wires source sets |
| `ApiClientGeneratorsExtension.kt` | Root DSL: `apiClientGenerator { generators { }; skip; initSubproject { } }` |
| `ApiClientGenerator.kt` | Per-generator DSL: `openApiFile`, `outputDirectory`, `basePackage`, `allowedPaths`, `modulesIds`, `skip`, `splitGranularity`, `sharedModelGranularity`, `targetSharedGroup`, `additionalSharedGroupPackages` |
| `GenerateTask.kt` | `@CacheableTask` — calls `generate(ApiGeneratorConfiguration)` |
| `InitSubprojectTask.kt` | Generates a new Gradle subproject from an OpenAPI spec |
| `InitSubprojectExtension.kt` | DSL for `initSubproject { }`: version overrides (`kotlinVersion`, `ktorVersion`, …), `buildScriptTemplate`, `generatorConfigExtra`, `multiplatform` (KMP mode), `subprojectRootDirectory` |

### Plugin flow

```mermaid
flowchart TD
    A["GeneratorPlugin.apply(project)"]
    B["create ApiClientGeneratorsExtension"]
    C["register initApiClientSubproject task"]
    D["afterEvaluate: for each generator"]
    E["register GenerateTask"]
    F["wire outputDirectory as Kotlin source set\n(jvm or commonMain)"]
    G["make compile / lint tasks depend on GenerateTask"]
    H["GenerateTask.execute()"]
    I["ApiGenerator.generate(configuration)"]

    A --> B
    A --> C
    A --> D
    D --> E
    D --> F
    D --> G
    E --> H
    H --> I
```

### Generated output layout

**Default (`splitByClient = false`)**:
```
outputDirectory/
└── src/main/kotlin/
    ├── {basePackage}/client/
    │   ├── ClientConfiguration.kt
    │   ├── UserClient.kt
    │   └── ProductClient.kt
    └── {basePackage}/model/
        ├── User.kt
        └── Status.kt
```

**Split mode (`splitByClient = true`, `targetClientName = null` → shared subproject)**:
```
outputDirectory/
└── src/main/kotlin/
    ├── {basePackage}/client/ClientConfiguration.kt
    └── {basePackage}/model/SharedModel.kt
```

**Split mode (`splitByClient = true`, `targetClientName = "UserClient"` → client subproject)**:
```
outputDirectory/
└── src/main/kotlin/
    ├── {basePackage}/client/UserClient.kt
    └── {basePackage}/model/UserPrivateModel.kt
```

### initApiClientSubproject

```bash
# Single subproject
./gradlew initApiClientSubproject -PopenApiFile=./specs/petstore.json [-PsubprojectName=petstore]

# Multi-module (one subproject per client) — default BY_TAG granularity
./gradlew initApiClientSubproject -PopenApiFile=./specs/petstore.json -PsplitByClient=true

# Multi-module with split granularity
./gradlew initApiClientSubproject -PopenApiFile=./specs/petstore.json -PsplitByClient=true -PsplitGranularity=BY_TAG_AND_PATH

# Multi-module with per-group shared models
./gradlew initApiClientSubproject -PopenApiFile=./specs/petstore.json -PsplitByClient=true -PsharedModelGranularity=SHARED_PER_GROUP
```

**Single subproject** generates:
```
{name}/
├── build.gradle.kts   ← kotlin-jvm + serialization + plugin + ktor deps + apiClientGenerator block
└── src/main/openapi/
    └── {spec}.json
```

**Multi-module SHARED_ALL** (`-PsplitByClient=true`) generates:
```
{name}/
├── settings.gradle.kts   ← rootProject.name = "{name}" + include("shared", "userClient", …)
├── build.gradle.kts      ← empty
├── src/main/openapi/{spec}.json
├── shared/build.gradle.kts          ← splitByClient=true, no targetClientName, basePackage set
└── {clientNameLower}/build.gradle.kts  ← splitByClient=true, targetClientName=OriginalName, api(project(":shared"))
```

**Multi-module SHARED_PER_GROUP** (`-PsplitByClient=true -PsharedModelGranularity=SHARED_PER_GROUP`) generates:
```
{name}/
├── settings.gradle.kts   ← include("shared", "shared-order-user", "orderClient", "userClient", …)
├── shared/build.gradle.kts              ← global shared (ClientConfiguration + orphan models)
├── shared-order-user/build.gradle.kts   ← models used by both OrderClient and UserClient
├── orderClient/build.gradle.kts         ← api(project(":shared")) + api(project(":shared-order-user"))
└── userClient/build.gradle.kts          ← api(project(":shared")) + api(project(":shared-order-user"))
```

Key points:
- Client directory names start with **lowercase** (`userClient`, not `UserClient`)
- All subprojects use the **same `basePackage`** derived from the spec filename (e.g. `org.example.petstore`) for cross-reference consistency
- Per-group shared subproject directory: sorted client names (strip "Client" suffix, lowercase, join with `-`), e.g. `shared-order-user`
- Per-group shared package suffix: camelCase of directory name, e.g. `sharedOrderUser`
- Use `includeBuild("{name}")` in the parent `settings.gradle.kts` (NOT `include`)
- Or run standalone: `cd {name} && ./gradlew build`

---

## `shared/` Module

Provides pure Kotlin utility functions (no framework deps):
- `Strings.kt`: `capitalize()`, `uncapitalize()`, `snakeToCamelCase()`, `tagToCamelCase()`, `toUpperSnakeCase()`, `sanitizeToIdentifier()`, `ensureEndsWith()`
- `Collections.kt`: `toOrNull()`

Used by both `generator/` and `gradle-plugin/`.

---

## Module System (SPI)

Optional modules loaded via `ApiGeneratorModule.getModule(id)`:
- `"UnknownEnumValueModule"` — adds `UNKNOWN` default value to enums
- `"LoggingSl4jModule"` — adds SLF4J logging to generated clients
- `"LoggingKotlinModule"` — adds kotlin-logging (oshai) logging to generated clients

Modules implement hooks on `ApiConfigurationGeneratorConfig`, `ApiClientGeneratorConfig`, `ApiModelGeneratorConfig`.

---

## Quality Gate (SonarCloud)

The project uses SonarCloud for static analysis with a strict **0 issues** quality gate.

### Running the full quality loop

```bash
./gradlew check jacocoAggregatedReport sonar sonarCheck
```

- `check` — compiles + runs all tests + generates per-module JaCoCo `.exec` files
- `jacocoAggregatedReport` — merges all `.exec` into `build/reports/jacoco/jacocoAggregatedReport/jacocoAggregatedReport.xml`
- `sonar` — sends analysis + aggregated coverage to SonarCloud
- `sonarCheck` — **calls the SonarCloud API** to verify gate status, issues count, and hotspot count; **fails the build** if any is non-zero

> ⚠️ **MANDATORY before finalizing any task**: `sonarCheck` must exit with `BUILD SUCCESSFUL`.
> If it fails, fix all reported issues before marking the task done.

### `sonarCheck` task

The `sonarCheck` Gradle task (registered in root `build.gradle.kts`) calls three SonarCloud API endpoints:

| Endpoint | What it checks |
|----------|----------------|
| `/api/qualitygates/project_status` | Quality gate status must be `OK` |
| `/api/issues/search?resolved=false` | Total open issues must be `0` |
| `/api/hotspots/search?status=TO_REVIEW` | Unreviewed security hotspots must be `0` |

It prints a summary box and fails with a clear error message listing each violation.

### Token setup

Add to `~/.gradle/gradle.properties` (never commit):
```
systemProp.sonar.token=<your-token>
```

### Project

- **Project key**: `Litote_openapi-ktor-client-generator`
- **Organization**: `litote`
- **URL**: https://sonarcloud.io/project/overview?id=Litote_openapi-ktor-client-generator

### Coverage notes

- JaCoCo is applied to all modules via `kotlin-convention.gradle.kts`
- The aggregated report covers all sub-modules (including `adapter-parser`, `adapter-renderer`, etc.) which have no direct tests but are exercised by `generator` tests
- `sonar.coverage.jacoco.xmlReportPaths` is set per-subproject to point to the root aggregated report
- Files at the root of `src/main/kotlin/` (e.g. `ApiGenerator.kt`) produce "File not found in project sources" warnings — this is a known Sonar/JaCoCo limitation with the Kotlin "common root omitted" convention; the code analysis is not affected

---

## Domain Model — `GenerationSpec` Structure

```mermaid
graph TD
    GS["GenerationSpec"]
    CC["ClientConfigurationSpec"]
    CS["ClientSpec"]
    OS["OperationSpec"]
    OP["OperationParameterSpec"]
    RB["RequestBodySpec"]
    RE["ResponseEntrySpec"]
    IL["ModelSpec (inline)"]
    MS["ModelSpec\n(DataClassSpec | SealedClassSpec\n| EnumSpec | AliasSpec | ObjectSpec)"]
    MP["ModelPropertySpec"]
    DT["DomainTypeSpec\n(PrimitiveSpec | ListTypeSpec | SetTypeSpec\n| MapTypeSpec | ModelReferenceSpec\n| InlineTypeSpec | JsonTypeSpec)"]

    GS -->|clientConfiguration| CC
    GS -->|clients| CS
    GS -->|models| MS
    CS -->|operations| OS
    OS -->|parameters| OP
    OS -->|requestBody| RB
    OS -->|responses| RE
    OS -->|inlineModels| IL
    MS -->|properties| MP
    IL -->|properties| MP
    MP -->|type| DT
    OP -->|type| DT
    RB -->|type| DT
    RE -->|bodyType| DT
```

**`DomainTypeSpec.ModelReferenceSpec(name)`** is the link between operations/models and named model classes generated in the model package.

---

## Version Constants for initApiClientSubproject gradle task

Generated at build time from `gradle/libs.versions.toml` into `GeneratorPlugin.kt`:
- `DEFAULT_KOTLIN_VERSION`
- `DEFAULT_KTOR_VERSION`
- `DEFAULT_COROUTINES_VERSION`
- `DEFAULT_SERIALIZATION_VERSION`
- `PLUGIN_VERSION`

---

## CI / CD

Four GitHub Actions workflows in `.github/workflows/`:

| Workflow | Trigger | Purpose |
|---|---|---|
| `ci.yml` | PR → main | PR title validation (Conventional Commits) + full QA |
| `snapshot.yml` | push → main | Deploy `-SNAPSHOT` to Maven Central (guarded: skips if `VERSION_NAME` is not a SNAPSHOT) |
| `release-please.yml` | push → main | Runs release-please (GitHub App): creates Release PR with CHANGELOG, then GitHub Release + tag on merge |
| `release.yml` | GitHub Release published | Full QA → Maven Central → Gradle Plugin Portal → bump next SNAPSHOT |

### Release flow

```
feat/fix PR merged → main
  → release-please.yml: creates/updates Release PR
    (CHANGELOG.md, .release-please-manifest.json)
  → Release PR merged
    → release-please creates tag vX.Y.Z + GitHub Release
      → release.yml triggers
        → Sets VERSION_NAME=X.Y.Z, builds, publishes
        → Bumps to X.Y.(Z+1)-SNAPSHOT, git commit [skip ci]
```

### Versioning

- **Source of truth for last released version:** `.release-please-manifest.json`
- **Conventional Commits types:** `feat:` → minor bump, `fix:`/`perf:` → patch, `feat!:`/`BREAKING CHANGE:` → major
- **Pre-major behaviour (`0.x`):** `feat:` bumps minor (not major) — `bump-minor-pre-major: true`
- **Config:** `release-please-config.json`

### Secrets

`RELEASE_PLEASE_APP_ID` + `RELEASE_PLEASE_APP_PRIVATE_KEY` (GitHub App), `SONAR_TOKEN`, `MAVEN_CENTRAL_*`, `SIGNING_IN_MEMORY_KEY*`, `GRADLE_PUBLISH_KEY/SECRET`.
