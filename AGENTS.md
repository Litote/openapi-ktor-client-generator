# OpenAPI Ktor Client Generator

A powerful Gradle plugin that transforms OpenAPI v3 specifications into production-ready Ktor Kotlin client code.
Generate type-safe, customizable clients that harness the power of Ktor and kotlinx.serialization.

## References

For prerequisites, installation, configuration, usage, project structure, and configuration properties, see `README.md`.

## Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use four spaces for indentation (consistent across all files)
- Name tests using GIVEN WHEN THEN pattern:
  ```kotlin
     @Test
     fun `GIVEN you have an umbrella WHEN weather is sunny THEN don't take it`() { ... }
  ```
- Use descriptive variable and function names
- Prefer functional programming patterns
- Use type-safe builders and DSLs for configuration
- Document public APIs with KDoc comments
- **ALWAYS** use single imports
- **All documentation, comments, and commit messages must be in English**
- **NEVER** suppress compiler warnings without good reason

## Logging

- **NEVER** use `println` for logging
- **ALWAYS** use KotlinLogging for logging
- Define a logger in companion object:
  ```kotlin
  private companion object {
      private val logger = KotlinLogging.logger {}
  }
  ```
- Use structured logging with lambda syntax:
  ```kotlin
  logger.debug { "Processing file: $fileName" }
  logger.warn { "Unexpected value: $value" }
  logger.error(exception) { "Failed to process: $item" }
  ```
- Prefer `debug` level for informational messages
- Use `warn` for unexpected but recoverable situations
- Use `error` for failures that need attention
- Never log sensitive information (API keys, passwords, tokens)

## Module Boundaries

- Do not move classes across modules.
- Do not introduce circular dependencies.
- Do not create new modules unless explicitly requested.
- Respect separation between `generator`, `gradle-plugin`, `shared`, `module/*`, `convention`, and `e2e`.

## Dependency Rules

- Do NOT add new dependencies without an explicit request or a clear fix requirement.
- Do NOT upgrade dependencies unless required to fix an issue.
- Do NOT change version catalogs without prior approval.
- Avoid reflection-heavy libraries and annotation processors.

## Kotlin & Concurrency Rules

### General Kotlin Rules

- Prefer `val` over `var`.
- Never use `!!`.
- Avoid nullable types unless required.
- Avoid global mutable state.
- Prefer `internal` visibility by default.
- Use sealed classes for finite state models.
- Prefer immutable data structures.

### Coroutines & Concurrency

- Never block coroutine threads.
- Never use `runBlocking` in production code.
- Never use `GlobalScope`.
- Use structured concurrency.
- Prefer `suspend` functions over callbacks.
- Avoid launching coroutines without a clear parent scope.

## Testing Requirements

- All logic changes must include tests.
- Generator changes → add generator tests.
- Gradle plugin behavior changes → add plugin tests.
- Module behavior changes → add module tests.

Tests must be:
- deterministic
- fast
- isolated

Never remove tests unless fixing a broken test.

## Security Constraints

- Never log API keys or secrets.
- Never expose environment variables.
- Validate all external inputs.
- Do not introduce dynamic class loading.

## Performance Constraints

- Avoid unnecessary allocations in hot paths.
- Avoid reflection.
- Avoid deep object copying.
- Avoid blocking I/O in execution paths.
- Avoid heavy synchronization primitives.

## Modification Scope Rules

- Modify only files directly related to the request.
- Avoid formatting-only changes.
- Avoid reordering imports unless necessary.
- Avoid renaming symbols unless required.
- Avoid mass refactors.

If unsure:
- Prefer no change over speculative change.

## Documentation Rules

- Update KDoc if public behavior changes.
- Do not add redundant comments.
- Document WHY, not WHAT.
- Keep documentation concise.

## What Is Forbidden

- Introducing breaking changes.
- Rewriting architecture.
- Converting internal APIs to public.
- Changing Gradle configuration without approval.
- Introducing new frameworks.
- Silently swallowing exceptions.
- Adding hidden side effects.
- Suppressing compiler warnings without justification.

## Definition of Done

A change is acceptable only if:
- It compiles without warnings.
- All relevant tests pass.
- No public API is broken.
- No unrelated file is modified.
- Type safety is preserved.
- Concurrency guarantees are preserved.
- Architecture boundaries are respected.

## Agent Behavior

When generating code:
- Be explicit.
- Be minimal.
- Be conservative.
- Preserve intent.
- Preserve type safety.
- Preserve determinism.

If a request conflicts with architectural integrity, favor integrity and explain the conflict.

## Architecture

### Core Framework Components

**GeneratorPlugin** — Gradle plugin implementation that orchestrates code generation,
configures Gradle tasks, and manages build integration.

**ApiClientGenerator** — Main orchestrator that controls the generation pipeline,
manages configuration options, and coordinates Ktor client creation.

**OpenApiParser** — Parses OpenAPI v3 files, extracts operations and data models,
and builds an internal representation for code generation.

**KotlinCodeGenerator** — Generates type-safe Kotlin code using KotlinPoet,
creates Ktor clients with kotlinx.serialization, and produces data models.

**GenerationModule** — Interface to extend generation with optional modules,
enabling features like SLF4J logging or unknown enum value handling.

### Module Organization

1. **shared**: Shared abstractions and code (`ConfigurationOptions`, utilities)
2. **generator**: Code generation engine (`OpenApiParser`, `KotlinCodeGenerator`)
3. **gradle-plugin**: Gradle integration (`GeneratorPlugin`, Gradle tasks)
4. **module/unknown-enum-value**: Handling unmapped enum values
5. **module/logging-sl4j**: SLF4J logging integration in generated clients

### Key Architectural Patterns

- **Configuration DSL**: Fluent APIs for plugin configuration
- **Separation of Concerns**: Parser, generator, and plugin are decoupled
- **Type Safety**: Extensive use of generics for compile-time correctness
- **Builder Patterns**: Type-safe construction of clients and models
- **Module Extensibility**: Modular architecture for optional features

## Testing

The project includes unit tests for code generation:

### Running Tests

```bash
# All tests
./gradlew check

# Generator tests only
./gradlew :generator:test

# Specific test
./gradlew :generator:test --tests "*.ParsingTests"

# With more output
./gradlew check --info
```

### Test Types

- **Parsing Tests**: Verify OpenAPI files are correctly parsed
- **Generation Tests**: Validate generated Kotlin code is syntactically correct
- **Integration Tests**: Verify generated clients compile and function
- **Plugin Tests**: Validate Gradle plugin integration

### E2E Testing

The `e2e/` project serves as an end-to-end test project with real configurations. The project is not linked to the root projet:

```bash
# Publish the plugin locally
./gradlew publishToMavenLocal

# Build e2e project with generation
cd e2e
./gradlew build

# Check generated sources
ls -la e2e/build/generated/src/main/kotlin/
```

## Security

### API Key Management

- **NEVER** commit API keys or secrets to the repository
- Use environment variables for all sensitive configuration
- Store test access keys locally only

### Input Validation

- Validate all OpenAPI files provided
- Verify package names are valid
- Validate directory paths before generation

### Dependency Security

- Keep dependencies up-to-date via `gradle/libs.versions.toml` catalog
- Use specific versions to avoid supply chain attacks
- Regularly review dependencies for known vulnerabilities
- Follow the principle of least privilege in generation

## Development Workflow

### Branch Strategy

- **main**: trunk branch
- Base all PRs against the `main` branch
- Use descriptive branch names: `feature/enum-support`, `fix/openapi-parsing`

### Code Quality

- **ALWAYS** run `./gradlew build` before submitting PRs
- Ensure all tests pass
- Follow patterns established in existing code
- Add tests for new functionality
- Update documentation for API changes

## Debugging

### Detailed Logs

```bash
# Build with detailed logs
./gradlew build --info

# Even more details (debug)
./gradlew build --debug
```

### Inspect Generated Files

```bash
# Display generated structure
find build/openapi/src/main/kotlin -name "*.kt" | head -20

# Display a generated file
cat build/openapi/src/main/kotlin/com/example/api/client/Client.kt
```

## Publishing

### Publish Locally

```bash
./gradlew publishToMavenLocal
```

## Resources

- [Gradle Documentation](https://docs.gradle.org/)
- [Ktor Client Documentation](https://ktor.io/docs/client-welcome.html)
- [OpenAPI Specification](https://spec.openapis.org/oas)
- [Kotlinx.Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [KotlinPoet](https://square.github.io/kotlinpoet/)
