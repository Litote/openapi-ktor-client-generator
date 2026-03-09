# AGENTS.md — OpenAPI Ktor Client Generator

> **Purpose**: Instructions for AI agents working on this codebase.
> For installation, configuration, and usage, see [`README.md`](README.md).
> For contributing guildelines, see (MANDATORY!) [`CONTRIBUTING.md`](CONTRIBUTING.md).

---

## Critical Rules

### NEVER Do

| Category           | Forbidden Actions                                                               |
|--------------------|---------------------------------------------------------------------------------|
| **Code**           | Use `!!`, `println`, `runBlocking`, `GlobalScope`                               |
| **Architecture**   | Move classes across modules, introduce circular dependencies, create new modules |
| **Dependencies**   | Add/upgrade dependencies without explicit request, change version catalogs      |
| **Security**       | Log secrets/API keys, expose environment variables, commit credentials          |
| **Scope**          | Mass refactors, rename symbols unnecessarily, formatting-only changes           |
| **Deploy**         | ./gradlew deploy, ./gradlew deployPlugins                                       |

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
