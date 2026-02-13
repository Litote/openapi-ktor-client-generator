# AGENTS.md — OpenAPI Ktor Client Generator

> **Purpose**: Instructions for AI agents working on this codebase.
> For installation, configuration, and usage, see [`README.md`](README.md).

---

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

---

## Critical Rules

### NEVER Do

| Category | Forbidden Actions |
|----------|-------------------|
| **Code** | Use `!!`, `println`, `runBlocking`, `GlobalScope` |
| **Architecture** | Move classes across modules, introduce circular dependencies, create new modules |
| **Dependencies** | Add/upgrade dependencies without explicit request, change version catalogs |
| **Security** | Log secrets/API keys, expose environment variables, commit credentials |
| **Scope** | Mass refactors, rename symbols unnecessarily, formatting-only changes |

### ALWAYS Do

| Category | Required Actions |
|----------|------------------|
| **Validation** | Run `./gradlew formatKotlin && ./gradlew check` after every change |
| **Testing** | Add tests for all logic changes |
| **Imports** | Use single imports only |
| **Language** | Write all code, comments, and documentation in English |
| **Visibility** | Prefer `internal` visibility by default |
| **Immutability** | Prefer `val` over `var`, use immutable data structures |

---

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

**Boundary Rules:**
- Do NOT move classes between modules
- Do NOT introduce cross-module circular dependencies
- Respect the separation of concerns between modules

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

## Core Components Reference

| Component | Responsibility |
|-----------|----------------|
| `GeneratorPlugin` | Gradle plugin, orchestrates tasks |
| `ApiClientGenerator` | Main generation pipeline |
| `OpenApiParser` | Parses OpenAPI v3, builds internal model |
| `KotlinCodeGenerator` | Generates Kotlin code via KotlinPoet |
| `GenerationModule` | Extension interface for optional features |

---

## Debugging

```bash
# Verbose build output
./gradlew build --info

# Inspect generated files
find build/openapi/src/main/kotlin -name "*.kt" | head -20
cat build/openapi/src/main/kotlin/com/example/api/client/Client.kt
```

---

## Resources

- [Ktor Client](https://ktor.io/docs/client-welcome.html)
- [OpenAPI Spec](https://spec.openapis.org/oas)
- [KotlinPoet](https://square.github.io/kotlinpoet/)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
