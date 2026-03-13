# Gradle subproject generation

The plugin provides an `initApiClientSubproject` task to generate a ready-to-use Gradle subproject
containing a pre-configured `build.gradle.kts` and your OpenAPI spec file.

```bash
./gradlew initApiClientSubproject \
  -PopenApiFile=<path/to/spec.yaml> \
  [-PsubprojectName=<directory-name>] \
  [-PbasePackage=<base.package>] \
  [-PsplitByClient=true] \
  [-PsplitGranularity=BY_TAG|BY_TAG_AND_PATH|BY_TAG_AND_OPERATION] \
  [-PsharedModelGranularity=SHARED_ALL|SHARED_PER_GROUP]
```

| Parameter                  | Description                                                                                          | Required |
|----------------------------|------------------------------------------------------------------------------------------------------|----------|
| `-PopenApiFile`            | Path to the OpenAPI spec (absolute or relative to the project root)                                 | **Yes**  |
| `-PsubprojectName`         | Name of the directory to create. Defaults to the spec filename without extension                    | No       |
| `-PbasePackage`            | Base package for all generated classes. Defaults to `org.example.<specname>`                       | No       |
| `-PsplitByClient`          | Generate one subproject per client + one shared subproject. Defaults to `false`                    | No       |
| `-PsplitGranularity`       | Controls how operations are grouped into clients. See [Split granularity](#split-granularity) below | No       |
| `-PsharedModelGranularity` | Controls how shared models are grouped. See [Shared model granularity](#shared-model-granularity) below | No   |

> **Note:** this task must be run from the **root project**. It is intentionally not available in subprojects.

## Single-subproject generation (default)

```bash
./gradlew initApiClientSubproject -PopenApiFile=./specs/petstore.json
```

This creates the following structure:

```
petstore/
├── build.gradle.kts        # pre-configured with the plugin + dependencies
└── src/main/openapi/
    └── petstore.json
```

The generated `build.gradle.kts` looks like:

```kotlin
plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    id("org.litote.openapi.ktor.client.generator.gradle") version "0.1.9"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("io.ktor:ktor-client-cio:3.4.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.1")
    implementation("io.ktor:ktor-client-core:3.4.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.1")
    implementation("io.ktor:ktor-client-logging:3.4.1")
}

apiClientGenerator {
    generators {
        create("petstore") {
            openApiFile = file("src/main/openapi/petstore.json")
        }
    }
}
```

Then add the subproject to your `settings.gradle.kts`:

```kotlin
include("petstore")
```

## Multi-subproject generation (`-PsplitByClient=true`)

When your OpenAPI spec has multiple tags (= multiple client classes), you can generate one Gradle
subproject per client plus a `shared` subproject containing the `ClientConfiguration` and all models
used by more than one client.

```bash
./gradlew initApiClientSubproject \
  -PopenApiFile=./specs/petstore.json \
  -PsplitByClient=true \
  -PbasePackage=com.example.petstore
```

Assuming the spec has a `Users` tag and a `Products` tag, this creates directly under your project root:

```
src/main/openapi/
└── petstore.json
shared/
└── build.gradle.kts        # generates ClientConfiguration + shared models
users-client/
└── build.gradle.kts        # generates UsersClient + models used only by UsersClient
products-client/
└── build.gradle.kts        # generates ProductsClient + models used only by ProductsClient
```

Each client subproject declares `api(project(":shared"))` so that shared classes are available at compile time.
All subprojects share the same root `basePackage`. The `shared` module uses it as-is; each client module
appends the client name (without "Client" suffix, first char lowercased, camelCase preserved — following
the Kotlin convention `org.example.myProject`) so that packages stay distinct while cross-references to
`ClientConfiguration` and shared models resolve correctly.
(e.g. `UsersClient` → `com.example.petstore.users`, `ApiV1MediaClient` → `com.example.petstore.apiV1Media`)

> **Note:** client directory names are derived from the client name in **kebab-case**
> (e.g. `UsersClient` → `users-client/`, `ApiV1MediaClient` → `api-v1-media-client/`)

Add the generated subprojects to your `settings.gradle.kts` (the exact list is also printed by the task):

```kotlin
include("shared", "users-client", "products-client")
```

### Model placement rules

| Model used by        | Placed in                   |
|----------------------|-----------------------------|
| 2 or more clients    | `shared`                    |
| exactly 1 client     | that client's subproject    |
| no client (orphan)   | `shared`                    |

If a `sealed class` is placed in `shared`, all its subtypes are also placed in `shared` regardless of individual usage.

## Split granularity

By default (`-PsplitByClient=true`), operations are grouped into one client per OpenAPI **tag**.
The `-PsplitGranularity` parameter changes this key:

| Value | Client key | Example |
|-------|-----------|---------|
| `BY_TAG` *(default)* | tag | `UsersClient` |
| `BY_TAG_AND_PATH` | tag + sanitized path | `UsersGetV1UsersIdClient` |
| `BY_TAG_AND_OPERATION` | tag + path + HTTP method | `UsersGetV1UsersIdGetClient` |

```bash
# One client per tag+path combination
./gradlew initApiClientSubproject \
  -PopenApiFile=./specs/petstore.json \
  -PsplitByClient=true \
  -PsplitGranularity=BY_TAG_AND_PATH
```

Use `BY_TAG_AND_PATH` or `BY_TAG_AND_OPERATION` when a single tag groups many unrelated operations
and you want finer-grained subprojects.

## Shared model granularity

By default (`-PsharedModelGranularity=SHARED_ALL`), all models used by more than one client go into a
single `shared` subproject. Use `SHARED_PER_GROUP` to instead create one dedicated shared subproject
per **unique set** of clients that share models.

```bash
./gradlew initApiClientSubproject \
  -PopenApiFile=./specs/petstore.json \
  -PsplitByClient=true \
  -PsharedModelGranularity=SHARED_PER_GROUP
```

Assuming `Address` is used by both `OrderClient` and `UserClient`, and `Category` is used by both
`OrderClient` and `ProductClient`, this creates:

```
src/main/openapi/
└── petstore.json
shared/
└── build.gradle.kts          # ClientConfiguration + orphan models only
shared-order-user/
└── build.gradle.kts          # Address (used by OrderClient + UserClient)
shared-order-product/
└── build.gradle.kts          # Category (used by OrderClient + ProductClient)
order-client/
└── build.gradle.kts          # api(project(":shared")) + api(project(":shared-order-user")) + api(project(":shared-order-product"))
user-client/
└── build.gradle.kts          # api(project(":shared")) + api(project(":shared-order-user"))
product-client/
└── build.gradle.kts          # api(project(":shared")) + api(project(":shared-order-product"))
```

Per-group shared subproject naming conventions:
- **Directory name**: sorted client names, "Client" suffix stripped, lowercase, joined with `-`, prefixed with `shared-`
  (e.g. `OrderClient` + `UserClient` → `shared-order-user`)
- **Package**: `basePackage` + camelCase of the directory name (Kotlin convention: `org.example.myProject`)
  (e.g. `shared-order-user` → `com.example.petstore.sharedOrderUser.model`)
- **Client subproject package**: `basePackage` + client name without "Client", first char lowercased (camelCase preserved)
  (e.g. `ApiV1MediaClient` → `com.example.petstore.apiV1Media`)

> **Note:** `SHARED_PER_GROUP` is most useful when your spec has 3+ clients with partially overlapping models.
> For two clients that share all models, it behaves identically to `SHARED_ALL`.

## Customising the generated `build.gradle.kts`

These options are configured in the `initSubproject { }` block of the root `apiClientGenerator` DSL,
and apply to **all** subprojects generated by the task.

```kotlin
apiClientGenerator {
    initSubproject {
        // options described below
    }
}
```

| Property               | Description                                                                                                                                | Default value             | Allowed values               |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|---------------------------|------------------------------|
| `kotlinVersion`        | Kotlin version in the generated `build.gradle.kts`                                                                                        | from `libs.versions.toml` | Any valid version            |
| `ktorVersion`          | Ktor version in the generated `build.gradle.kts`                                                                                          | from `libs.versions.toml` | Any valid version            |
| `coroutinesVersion`    | `kotlinx-coroutines` version in the generated `build.gradle.kts`                                                                          | from `libs.versions.toml` | Any valid version            |
| `serializationVersion` | `kotlinx-serialization` version in the generated `build.gradle.kts`                                                                       | from `libs.versions.toml` | Any valid version            |
| `buildScriptTemplate`  | Replaces the entire auto-generated `plugins {}` + `dependencies {}` block. Use this when your project has a Gradle version catalog.       | `null` (auto-generated)   | Any Gradle Kotlin DSL string |
| `generatorConfigExtra` | Extra lines appended inside every `create("...") { }` block. Use this to enable modules or set shared properties across all subprojects.  | `null` (nothing appended) | Any Gradle Kotlin DSL lines  |

### Overriding dependency versions

```kotlin
apiClientGenerator {
    initSubproject {
        kotlinVersion        = "2.0.0"
        ktorVersion          = "2.3.12"
        coroutinesVersion    = "1.7.3"
        serializationVersion = "1.6.3"
    }
}
```

### Using a Gradle version catalog

If your project uses a `libs.versions.toml` version catalog, replace the auto-generated
`plugins {}` and `dependencies {}` blocks with `buildScriptTemplate`:

```kotlin
apiClientGenerator {
    initSubproject {
        buildScriptTemplate = """
            plugins {
                alias(libs.plugins.kotlin.jvm)
                alias(libs.plugins.kotlin.serialization)
                alias(libs.plugins.openapi.ktor.client.generator)
            }

            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
            }
        """.trimIndent()
    }
}
```

> **Note (multi-module):** for client subprojects, `dependencies { api(project(":shared")) }` is
> always appended automatically after your template — you do not need to include it.

### Adding shared generator configuration

Use `generatorConfigExtra` to append lines inside every generated `create("...") { }` block,
for example to enable optional modules across all subprojects:

```kotlin
apiClientGenerator {
    initSubproject {
        generatorConfigExtra = """
            modulesIds.add("UnknownEnumValueModule")
            modulesIds.add("LoggingSl4jModule")
        """.trimIndent()
    }
}
```

