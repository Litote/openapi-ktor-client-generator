# AGENTS.md — OpenAPI Ktor Client Generator

> **Purpose**: Instructions for AI agents working on this codebase.
> For installation, configuration, and usage, see [`README.md`](README.md) and [`PROJECT_GENERATION.md`](PROJECT_GENERATION.md).
> For contributing guidelines and project architecture, see (MANDATORY!) [`CONTRIBUTING.md`](CONTRIBUTING.md).
> For a project analysis, see (MANDATORY!) [`AI_CONTEXT.md`](AI_CONTEXT.md).
---

## Critical Rules

### NEVER Do

| Category         | Forbidden Actions                                                                |
|------------------|----------------------------------------------------------------------------------|
| **Code**         | Use `!!`, `println`, `runBlocking`, `GlobalScope`                                |
| **Architecture** | Move classes across modules, introduce circular dependencies, create new modules |
| **Dependencies** | Add/upgrade dependencies without explicit request, change version catalogs       |
| **Security**     | Log secrets/API keys, expose environment variables, commit credentials           |
| **Scope**        | Mass refactors, rename symbols unnecessarily, formatting-only changes            |
| **Deploy**       | `./gradlew deploy`, `./gradlew deployPlugins`, triggering releases manually      |
| **Commit**       | Commit unless the user explicitly asks for a commit or a PR                      |
| **Generated**    | Edit `PluginVersion.kt` manually (generated at build time from `libs.versions.toml`); modify files under `e2e/build/` or `e2e-split/client/` |

### ALWAYS Do

| Category                   | Required Actions                                                                                          |
|----------------------------|-----------------------------------------------------------------------------------------------------------|
| **Validation**             | Run `./gradlew formatKotlin && ./gradlew check` after every iteration                                     |
| **Quality Gate**           | Run `./gradlew check jacocoAggregatedReport sonar sonarCheck` before finalizing **every** task — **`sonarCheck` must pass (0 issues, 0 hotspots, gate OK)**. Requires `systemProp.sonar.token` in `~/.gradle/gradle.properties`. If the token is unavailable locally, note it explicitly and let CI validate. This includes `.github/workflows/` YAML files (analysed by Sonar for security). `.md`-only changes do **not** require Sonar. |
| **Testing**                | When you try to fix a bug, start by adding the test and THEN fix the bug. Add tests for all logic changes |
| **Imports**                | Use single imports only                                                                                   |
| **Language**               | Write all code, comments, and documentation in English                                                    |
| **Visibility**             | Prefer `internal` visibility by default                                                                   |
| **Immutability**           | Prefer `val` over `var`, use immutable data structures                                                    |
| **Document**               | User-facing changes → `README.md`; contributor/architecture changes → `CONTRIBUTING.md`; agent-relevant changes → `AI_CONTEXT.md` + `AGENTS.md` |
| **Keep AI doc up-to-date** | Update `AI_CONTEXT.md` when adding/removing domain types, changing public API, or making architectural decisions. Update `AGENTS.md` when rules or workflows change. |
---

## Code Style

### Kotlin Conventions

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- 4-space indentation
- Avoid nullable types unless required
- Use sealed classes for finite state models
- Prefer functional programming patterns

### Logging (KotlinLogging)

```kotlin
private companion object {
    private val logger = KotlinLogging.logger {}
}

// Usage:
logger.debug { "Processing: $fileName" }
logger.warn { "Unexpected: $value" }
logger.error(exception) { "Failed: $item" }
```

### Test Naming

```kotlin
@Test
fun `GIVEN precondition WHEN action THEN expected result`() { 
    //...
    }
```

---

## Testing Requirements

| Change Type | Required Tests |
|-------------|----------------|
| Generator logic | `generator/src/test/` |
| Gradle plugin | `gradle-plugin/src/test/` |
| Module behavior | `module/*/src/test/` |
| Integration | `e2e/` project |

**Test Characteristics:** deterministic, fast, isolated.

---

## Definition of Done

A change is complete when:

- [ ] `./gradlew formatKotlin` passes
- [ ] `./gradlew check` passes (compiles without warnings, all tests pass)
- [ ] `./gradlew check jacocoAggregatedReport sonar sonarCheck` passes — **`sonarCheck` must exit with BUILD SUCCESSFUL (0 issues, 0 hotspots, gate OK)**
- [ ] No public API is broken
- [ ] Only relevant files are modified
- [ ] Type safety is preserved
- [ ] Architecture boundaries are respected
- [ ] Tests are added for new logic

---

## Agent Behavior Guidelines

**When generating code:**
- Be minimal — change only what's necessary
- Be conservative — preserve existing patterns
- Be explicit — no hidden side effects
- Preserve type safety and determinism

**When uncertain:**
- Prefer no change over speculative change
- Favor architectural integrity over feature completion
- Explain conflicts with requirements

---

## Typical Change Workflow

### Adding support for a new OpenAPI construct

1. **Domain** (`generator:domain`) — add or extend a `*Spec` type
2. **Parser** (`generator:adapter-parser/OpenApiSpecificationParser`) — translate the OpenAPI input to the new domain type
3. **Renderer** (`generator:adapter-renderer`) — generate Kotlin code from the new domain type
4. **Test** (`generator/src/test/kotlin/`) — add a test in the root generator module (integration-level: covers parser + renderer together)
5. **Update** `AI_CONTEXT.md` if the domain model changed

### Fixing a bug in generated code

1. Add a failing test in `generator/src/test/kotlin/` that reproduces the bug
2. Identify which layer is wrong (parser? domain? renderer?)
3. Fix only that layer
4. Re-run `./gradlew check` to confirm the test passes

---

## Debugging

```bash
# Verbose build output
./gradlew build --info

# Inspect files generated by the e2e project
find e2e/build/generated -name "*.kt" | head -20
cat e2e/build/generated/src/main/kotlin/org/example/client/ClientConfiguration.kt

# Run a single test class
./gradlew :generator:test --tests "*.SomeTestClass"
```

---

## Resources

- [Ktor Client](https://ktor.io/docs/client-welcome.html)
- [OpenAPI Spec](https://spec.openapis.org/oas)
- [KotlinPoet](https://square.github.io/kotlinpoet/)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- [SnakeYAML](https://bitbucket.org/snakeyaml/snakeyaml) — used for YAML support via `YamlContentConverter`
