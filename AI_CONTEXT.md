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
│   └── logging-sl4j/             → Optional module: SLF4J logging
├── convention/                   → Build convention plugins
├── e2e/                          → End-to-end test project
├── settings.gradle.kts
└── build.gradle.kts
```

---

## Generator Module (`generator/`) — Hexagonal Architecture

```
generator/src/main/kotlin/
├── domain/               → Pure business models (zero external deps)
│   ├── GenerationSpec    → Top-level: clientConfiguration + clients + models
│   ├── ClientSpec        → One Ktor client class per OpenAPI tag
│   ├── OperationSpec     → One suspend method per HTTP operation (with inlineModels)
│   ├── ModelSpec (sealed)→ DataClassSpec | EnumSpec | SealedClassSpec | ObjectSpec | AliasSpec
│   ├── DomainType (sealed)→ Primitive | ListType | SetType | MapType | ModelReference | InlineType | JsonType
│   ├── ModelProperty     → Property with originalName, camelCaseName, type, nestedModels
│   ├── OperationParameter→ with additionalModel for inline types
│   ├── RequestBodySpec   → with inlineModels
│   ├── ResponseEntry     → statusCodes + bodyType + isSuccess
│   ├── FormFieldSpec
│   ├── ClientConfigurationSpec, SecuritySchemeSpec
│   ├── ComponentParameterSpec, DefaultValue
│   ├── OperationMeta, ParameterLocation
│   ├── ModelUsageAnalyzer       → analyzes modelName → Set<clientName> (transitively, BFS)
│   └── PartitionedGenerationSpec / PerClientGenerationSpec → result of split-by-client partition
│
├── port/                 → Interfaces (contracts)
│   ├── SpecificationParser    → parse(config, filter): GenerationSpec
│   ├── ConfigurationRenderer  → render()
│   ├── ClientRenderer         → render(ClientSpec) [functional interface]
│   ├── ModelRenderer          → render(ModelSpec) [functional interface]
│   ├── FileSystemWriter
│   ├── ConfigurationGeneratorConfig → jsonProperties, exceptionLogging
│   ├── ClientGeneratorConfig
│   └── ModelGeneratorConfig   → defaultEnumValue
│
├── adapter/
│   ├── parser/
│   │   ├── OpenApiSpecificationParser  → implements SpecificationParser
│   │   ├── TypeNameConverter, ParserNameUtils, ParserTypeUtils
│   │   └── ApiModel, ApiOperation, ApiClassProperty
│   ├── renderer/
│   │   ├── ApiClientGenerator          → ClientSpec → FileSpec (KotlinPoet)
│   │   ├── ApiModelGenerator           → ModelSpec → FileSpec (KotlinPoet)
│   │   ├── ApiClientConfigurationGenerator → ClientConfigurationSpec → FileSpec
│   │   ├── OperationBuilder, ResponseBuilder
│   │   ├── DomainTypeMapper            → DomainType → KotlinPoet TypeName
│   │   └── KotlinPoets, KtorPoets      → KotlinPoet utilities
│   └── writer/
│       └── KotlinPoetFileWriter        → GeneratedFile → disk
│
└── application/
    ├── GenerateCodeService      → orchestrates: configRenderer + clientRenderer + modelRenderer
    └── GenerationSpecPartitioner → partitions GenerationSpec into shared + perClient
```
├── port/                 → Interfaces (contracts)
│   ├── SpecificationParser    → parse(config, filter): GenerationSpec
│   ├── ConfigurationRenderer  → render()
│   ├── ClientRenderer         → render(ClientSpec) [functional interface]
│   ├── ModelRenderer          → render(ModelSpec) [functional interface]
│   ├── FileSystemWriter
│   ├── ConfigurationGeneratorConfig → jsonProperties, exceptionLogging
│   ├── ClientGeneratorConfig
│   └── ModelGeneratorConfig   → defaultEnumValue
│
├── adapter/
│   ├── parser/
│   │   ├── OpenApiSpecificationParser  → implements SpecificationParser
│   │   ├── TypeNameConverter, ParserNameUtils, ParserTypeUtils
│   │   └── ApiModel, ApiOperation, ApiClassProperty
│   ├── renderer/
│   │   ├── ApiClientGenerator          → ClientSpec → FileSpec (KotlinPoet)
│   │   ├── ApiModelGenerator           → ModelSpec → FileSpec (KotlinPoet)
│   │   ├── ApiClientConfigurationGenerator → ClientConfigurationSpec → FileSpec
│   │   ├── OperationBuilder, ResponseBuilder
│   │   ├── DomainTypeMapper            → DomainType → KotlinPoet TypeName
│   │   └── KotlinPoets, KtorPoets      → KotlinPoet utilities
│   └── writer/
│       └── KotlinPoetFileWriter        → GeneratedFile → disk
│
└── application/
    └── GenerateCodeService → orchestrates: configRenderer + clientRenderer + modelRenderer
```

**Dependency rules:** `domain` ← `port` ← `application`; `adapter` depends on `domain`+`port`; `ApiGenerator.kt` is the only file importing all layers.

---

## Public API (`ApiGenerator.kt`)

```kotlin
// Main entry point — generates everything (or a split portion when splitByClient=true)
fun generate(configuration: ApiGeneratorConfiguration): GenerationResult

// Returns the list of client class names parsed from an OpenAPI spec (used by InitSubprojectTask)
fun parseClientNames(openApiFilePath: String): List<String>

data class ApiGeneratorConfiguration(
    val openApiFile: String,
    val outputDirectory: String,
    val basePackage: String = "org.example",
    val operationFilter: (OperationMeta) -> Boolean = { true },
    val modelPackage: String = "$basePackage.model",
    val clientPackage: String = "$basePackage.client",
    val modules: List<ApiGeneratorModule> = emptyList(),
    // Split-by-client mode:
    val splitByClient: Boolean = false,      // false = current behavior
    val targetClientName: String? = null,    // null = generate shared; "Foo" = generate FooClient + private models
)
```

### Split-by-client generation logic

When `splitByClient = true`:
1. Parse the full spec
2. `GenerationSpecPartitioner.partition(spec)` → `PartitionedGenerationSpec`
3. `targetClientName == null` → generate shared (ClientConfiguration + models used by 2+ clients), no clients
4. `targetClientName == "FooClient"` → generate FooClient + models used ONLY by FooClient, no config

Model placement rules:
- Used by 2+ clients → shared
- Used by exactly 1 client → that client's subproject
- Used by 0 clients (orphan) → shared
- If a SealedClass is shared → all its subtypes are shared too (propagated)

---

## Gradle Plugin (`gradle-plugin/`)

### Key files

| File | Role |
|------|------|
| `GeneratorPlugin.kt` | `Plugin<Project>` — registers tasks, wires source sets |
| `ApiClientGeneratorsExtension.kt` | Root DSL: `apiClientGenerator { generators { }; skip; initSubproject { } }` |
| `ApiClientGenerator.kt` | Per-generator DSL: `openApiFile`, `outputDirectory`, `basePackage`, `allowedPaths`, `modulesIds`, `skip` |
| `GenerateTask.kt` | `@CacheableTask` — calls `generate(ApiGeneratorConfiguration)` |
| `InitSubprojectTask.kt` | Generates a new Gradle subproject from an OpenAPI spec |
| `InitSubprojectExtension.kt` | DSL for `initSubproject { }`: version overrides (`kotlinVersion`, `ktorVersion`, …), `buildScriptTemplate`, `generatorConfigExtra` |

### Plugin flow

```
GeneratorPlugin.apply(project)
├── Creates ApiClientGeneratorsExtension ("apiClientGenerator")
├── Registers initApiClientSubproject task
└── afterEvaluate: for each generator
    ├── initConventions(project)
    ├── Registers generateX task (GenerateTask)
    ├── Wires srcDir: outputDirectory/src/main/kotlin → kotlin.jvm or kotlin.multiplatform commonMain
    └── Makes KotlinCompile, Jar, lintKotlin depend on generateX
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

# Multi-module (one subproject per client)
./gradlew initApiClientSubproject -PopenApiFile=./specs/petstore.json -PsplitByClient=true
```

**Single subproject** generates:
```
{name}/
├── build.gradle.kts   ← kotlin-jvm + serialization + plugin + ktor deps + apiClientGenerator block
└── src/main/openapi/
    └── {spec}.json
```

**Multi-module** (`-PsplitByClient=true`) generates:
```
{name}/
├── settings.gradle.kts   ← rootProject.name = "{name}" + include("shared", "userClient", …)
├── build.gradle.kts      ← empty
├── src/main/openapi/{spec}.json
├── shared/build.gradle.kts          ← splitByClient=true, no targetClientName, basePackage set
└── {clientNameLower}/build.gradle.kts  ← splitByClient=true, targetClientName=OriginalName, api(project(":shared"))
```

Key points:
- Client directory names start with **lowercase** (`userClient`, not `UserClient`)
- All subprojects use the **same `basePackage`** derived from the spec filename (e.g. `org.example.petstore`) for cross-reference consistency
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

Modules implement hooks on `ConfigurationGeneratorConfig`, `ClientGeneratorConfig`, `ModelGeneratorConfig`.

---

## Key Domain Relationships

```
GenerationSpec
├── clientConfiguration: ClientConfigurationSpec
├── clients: List<ClientSpec>
│   └── ClientSpec.operations: List<OperationSpec>
│       ├── parameters: List<OperationParameter>   → type: DomainType (may be ModelReference/InlineType)
│       ├── requestBody: RequestBodySpec?           → type: DomainType, inlineModels: List<ModelSpec>
│       ├── responses: List<ResponseEntry>          → bodyType: DomainType?
│       └── inlineModels: List<ModelSpec>
└── models: List<ModelSpec>
    ├── DataClassSpec.properties: List<ModelProperty>
    │   └── ModelProperty.type: DomainType (ModelReference = link to another model)
    │   └── ModelProperty.nestedModels: List<ModelSpec>
    ├── SealedClassSpec (discriminator)
    ├── DataClassSpec/ObjectSpec with sealedParentName → child of a SealedClass
    └── EnumSpec, AliasSpec
```

**`DomainType.ModelReference(name)` is the link between clients/models and named model classes.**

---

## Version Constants for initApiClientSubproject gradle task

Generated at build time from `gradle/libs.versions.toml` into `GeneratorPlugin.kt`:
- `DEFAULT_KOTLIN_VERSION`
- `DEFAULT_KTOR_VERSION`
- `DEFAULT_COROUTINES_VERSION`
- `DEFAULT_SERIALIZATION_VERSION`
- `PLUGIN_VERSION`
