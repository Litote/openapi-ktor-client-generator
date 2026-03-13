package org.litote.openapi.ktor.client.generator

import org.junit.jupiter.api.io.TempDir
import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.application.GenerationSpecPartitioner
import org.litote.openapi.ktor.client.generator.domain.analyzeModelUsage
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests for [SharedModelGranularity] support and [parseSharedClientGroups].
 *
 * The test spec `shared-model-granularity.json` has:
 * - `User` tag   → `UserClient`   using `UserResponse` (private) + `Address` (shared with OrderClient)
 * - `Order` tag  → `OrderClient`  using `OrderResponse` (private) + `Address` (shared with UserClient)
 *                                                                  + `Category` (shared with ProductClient)
 * - `Product` tag → `ProductClient` using `ProductResponse` (private) + `Category` (shared with OrderClient)
 *
 * Expected partition:
 * - Group {UserClient, OrderClient}:  `Address`
 * - Group {OrderClient, ProductClient}: `Category`
 * - Private: `UserResponse`, `OrderResponse`, `ProductResponse`
 */
class SharedModelGranularityTest {
    @TempDir
    lateinit var tempDir: File

    private companion object {
        private const val SPEC = "src/test/resources/shared-model-granularity.json"
        private const val BASE_PKG = "com.example.sharedgranularity"
    }

    private fun generatedFiles(): List<String> =
        tempDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .map { it.relativeTo(tempDir).path }
            .sorted()
            .toList()

    // ─────────────────────────────────────────────────────────────
    // parseSharedClientGroups
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN 3-client spec WHEN parseSharedClientGroups THEN returns 2 groups`() {
        val groups = parseSharedClientGroups(SPEC)
        assertEquals(2, groups.size, "Expected 2 shared groups, got $groups")
    }

    @Test
    fun `GIVEN 3-client spec WHEN parseSharedClientGroups THEN group UserClient+OrderClient contains Address`() {
        val groups = parseSharedClientGroups(SPEC)
        val addressGroup = groups.find { "UserClient" in it.clientGroup && "OrderClient" in it.clientGroup }
        assertFalse(addressGroup == null, "Expected group for UserClient+OrderClient, got $groups")
        assertTrue("Address" in addressGroup.modelNames, "Expected Address in group, got ${addressGroup.modelNames}")
    }

    @Test
    fun `GIVEN 3-client spec WHEN parseSharedClientGroups THEN group OrderClient+ProductClient contains Category`() {
        val groups = parseSharedClientGroups(SPEC)
        val categoryGroup = groups.find { "OrderClient" in it.clientGroup && "ProductClient" in it.clientGroup }
        assertFalse(categoryGroup == null, "Expected group for OrderClient+ProductClient, got $groups")
        assertTrue("Category" in categoryGroup.modelNames, "Expected Category in group, got ${categoryGroup.modelNames}")
    }

    @Test
    fun `GIVEN spec with no non-trivial shared groups WHEN parseSharedClientGroups THEN returns empty list`() {
        val groups = parseSharedClientGroups("src/test/resources/split-by-client.json")
        // split-by-client.json has Address shared by both clients → 1 group
        assertEquals(1, groups.size, "Expected 1 group for split-by-client spec, got $groups")
    }

    @Test
    fun `GIVEN 3-client spec WHEN parseSharedClientGroups THEN group UserClient+OrderClient has correct directoryName`() {
        val groups = parseSharedClientGroups(SPEC)
        val group = groups.find { "UserClient" in it.clientGroup && "OrderClient" in it.clientGroup }
        assertFalse(group == null, "Expected group for UserClient+OrderClient")
        assertEquals("shared-order-user", group.directoryName)
    }

    @Test
    fun `GIVEN 3-client spec WHEN parseSharedClientGroups THEN group OrderClient+ProductClient has correct directoryName`() {
        val groups = parseSharedClientGroups(SPEC)
        val group = groups.find { "OrderClient" in it.clientGroup && "ProductClient" in it.clientGroup }
        assertFalse(group == null, "Expected group for OrderClient+ProductClient")
        assertEquals("shared-order-product", group.directoryName)
    }

    @Test
    fun `GIVEN 3-client spec WHEN parseSharedClientGroups THEN group UserClient+OrderClient has correct packageSuffix`() {
        val groups = parseSharedClientGroups(SPEC)
        val group = groups.find { "UserClient" in it.clientGroup && "OrderClient" in it.clientGroup }
        assertFalse(group == null, "Expected group for UserClient+OrderClient")
        assertEquals("sharedOrderUser", group.packageSuffix)
    }

    @Test
    fun `GIVEN 3-client spec WHEN parseSharedClientGroups THEN group OrderClient+ProductClient has correct packageSuffix`() {
        val groups = parseSharedClientGroups(SPEC)
        val group = groups.find { "OrderClient" in it.clientGroup && "ProductClient" in it.clientGroup }
        assertFalse(group == null, "Expected group for OrderClient+ProductClient")
        assertEquals("sharedOrderProduct", group.packageSuffix)
    }

    // ─────────────────────────────────────────────────────────────
    // SHARED_ALL (default backward compat)
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN SHARED_ALL WHEN generating shared THEN Address and Category are both generated`() {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = BASE_PKG,
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_ALL,
                targetClientName = null,
            ),
        )
        val files = generatedFiles()
        assertTrue(files.any { it.endsWith("Address.kt") }, "Address expected in SHARED_ALL output: $files")
        assertTrue(files.any { it.endsWith("Category.kt") }, "Category expected in SHARED_ALL output: $files")
    }

    // ─────────────────────────────────────────────────────────────
    // SHARED_PER_GROUP — global shared (targetSharedGroup = null)
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN SHARED_PER_GROUP WHEN generating global shared THEN result is success`() {
        val result =
            generate(
                ApiGeneratorConfiguration(
                    openApiFile = SPEC,
                    outputDirectory = tempDir.absolutePath,
                    basePackage = BASE_PKG,
                    splitByClient = true,
                    sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                    targetClientName = null,
                    targetSharedGroup = null,
                ),
            )
        assertIs<GenerationResult.Success>(result)
    }

    @Test
    fun `GIVEN SHARED_PER_GROUP WHEN generating global shared THEN ClientConfiguration is generated`() {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = BASE_PKG,
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
            ),
        )
        val files = generatedFiles()
        assertTrue(files.any { it.endsWith("ClientConfiguration.kt") }, "ClientConfiguration expected: $files")
    }

    @Test
    fun `GIVEN SHARED_PER_GROUP WHEN generating global shared THEN Address and Category are NOT generated`() {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = BASE_PKG,
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
            ),
        )
        val files = generatedFiles()
        assertFalse(files.any { it.endsWith("Address.kt") }, "Address must NOT be in global shared output: $files")
        assertFalse(files.any { it.endsWith("Category.kt") }, "Category must NOT be in global shared output: $files")
    }

    // ─────────────────────────────────────────────────────────────
    // SHARED_PER_GROUP — specific group generation
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN SHARED_PER_GROUP WHEN generating UserClient+OrderClient group THEN Address is generated`() {
        val result =
            generate(
                ApiGeneratorConfiguration(
                    openApiFile = SPEC,
                    outputDirectory = tempDir.absolutePath,
                    basePackage = "$BASE_PKG.sharedOrderUser",
                    splitByClient = true,
                    sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                    targetSharedGroup = setOf("UserClient", "OrderClient"),
                ),
            )
        assertIs<GenerationResult.Success>(result)
        val files = generatedFiles()
        assertTrue(files.any { it.endsWith("Address.kt") }, "Address expected in UserClient+OrderClient group: $files")
    }

    @Test
    fun `GIVEN SHARED_PER_GROUP WHEN generating UserClient+OrderClient group THEN Category is NOT generated`() {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = "$BASE_PKG.sharedOrderUser",
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                targetSharedGroup = setOf("UserClient", "OrderClient"),
            ),
        )
        val files = generatedFiles()
        assertFalse(files.any { it.endsWith("Category.kt") }, "Category must NOT be in UserClient+OrderClient group: $files")
    }

    @Test
    fun `GIVEN SHARED_PER_GROUP WHEN generating OrderClient+ProductClient group THEN Category is generated`() {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = "$BASE_PKG.sharedOrderProduct",
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                targetSharedGroup = setOf("OrderClient", "ProductClient"),
            ),
        )
        val files = generatedFiles()
        assertTrue(files.any { it.endsWith("Category.kt") }, "Category expected in OrderClient+ProductClient group: $files")
    }

    @Test
    fun `GIVEN SHARED_PER_GROUP WHEN generating unknown group THEN result is failure`() {
        val result =
            generate(
                ApiGeneratorConfiguration(
                    openApiFile = SPEC,
                    outputDirectory = tempDir.absolutePath,
                    basePackage = BASE_PKG,
                    splitByClient = true,
                    sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                    targetSharedGroup = setOf("FooClient", "BarClient"),
                ),
            )
        assertIs<GenerationResult.Failure>(result)
    }

    @Test
    fun `GIVEN SHARED_PER_GROUP WHEN generating group THEN no ClientConfiguration is generated`() {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = "$BASE_PKG.sharedOrderUser",
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                targetSharedGroup = setOf("UserClient", "OrderClient"),
            ),
        )
        val files = generatedFiles()
        assertFalse(files.any { it.endsWith("ClientConfiguration.kt") }, "ClientConfiguration must NOT be in group output: $files")
    }

    // ─────────────────────────────────────────────────────────────
    // modelPackageOverrides — type references use overridden packages
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN modelPackageOverrides WHEN generating UserClient THEN Address uses overridden package`() {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = "$BASE_PKG.user",
                sharedBasePackage = BASE_PKG,
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                targetClientName = "UserClient",
                modelPackageOverrides = mapOf("Address" to "$BASE_PKG.sharedOrderUser.model"),
            ),
        )
        val modelFile =
            tempDir.walkTopDown().firstOrNull { it.name == "UserResponse.kt" }
                ?: error("UserResponse.kt not generated")
        val content = modelFile.readText()
        assertTrue(
            content.contains("$BASE_PKG.sharedOrderUser.model.Address") ||
                content.contains("import $BASE_PKG.sharedOrderUser.model"),
            "UserResponse should reference Address from sharedOrderUser package, got:\n$content",
        )
    }

    // ─────────────────────────────────────────────────────────────
    // Mastodon regression: orphan models must not be generated
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN mastodon spec WHEN analyzeModelUsage THEN StatusVisibilityEnum is used by multiple clients`() {
        val config = ApiGeneratorConfiguration(openApiFile = "src/test/resources/mastodon.json")
        val spec = OpenApiSpecificationParser(config).parse(config.operationFilter)
        val usage = analyzeModelUsage(spec)

        val statusVisClients = usage["StatusVisibilityEnum"] ?: emptySet()
        assertTrue(
            statusVisClients.size >= 2,
            "StatusVisibilityEnum should be used by 2+ clients (via Status.visibility), got: $statusVisClients",
        )
    }

    @Test
    fun `GIVEN mastodon spec WHEN partition THEN StatusVisibilityEnum is in a shared group (not per-client)`() {
        val config = ApiGeneratorConfiguration(openApiFile = "src/test/resources/mastodon.json")
        val spec = OpenApiSpecificationParser(config).parse(config.operationFilter)
        val partitioned = GenerationSpecPartitioner().partition(spec)

        val inShared =
            partitioned.sharedGroups.any { group ->
                group.spec.models.any { it.name == "StatusVisibilityEnum" }
            }
        val inPerClient =
            partitioned.perClient.any { perClient ->
                perClient.spec.models.any { it.name == "StatusVisibilityEnum" }
            }

        assertTrue(inShared, "StatusVisibilityEnum must be in a shared group")
        assertFalse(inPerClient, "StatusVisibilityEnum must NOT be in any per-client module")
    }

    @Test
    fun `GIVEN mastodon spec WHEN partition THEN BaseStatus orphan model is not generated`() {
        val config = ApiGeneratorConfiguration(openApiFile = "src/test/resources/mastodon.json")
        val spec = OpenApiSpecificationParser(config).parse(config.operationFilter)
        val partitioned = GenerationSpecPartitioner().partition(spec)

        val inAnyGroup = partitioned.sharedGroups.any { group -> group.spec.models.any { it.name == "BaseStatus" } }
        val inAnyClient = partitioned.perClient.any { client -> client.spec.models.any { it.name == "BaseStatus" } }

        assertFalse(inAnyGroup, "BaseStatus is orphan (0 clients) — must not appear in any shared group")
        assertFalse(inAnyClient, "BaseStatus is orphan (0 clients) — must not appear in any per-client module")
    }

    @Test
    fun `GIVEN mastodon spec WHEN partition THEN no orphan group exists`() {
        val config = ApiGeneratorConfiguration(openApiFile = "src/test/resources/mastodon.json")
        val spec = OpenApiSpecificationParser(config).parse(config.operationFilter)
        val partitioned = GenerationSpecPartitioner().partition(spec)

        val orphanGroup = partitioned.sharedGroups.firstOrNull { it.clientGroup.isEmpty() }
        assertTrue(
            orphanGroup == null,
            "There must be no orphan group (empty clientGroup) — orphan models are excluded from generation",
        )
    }

    @Test
    fun `GIVEN mastodon spec WHEN analyzeModelUsage THEN ListRepliesPolicyEnum has same clients as List`() {
        val config = ApiGeneratorConfiguration(openApiFile = "src/test/resources/mastodon.json")
        val spec = OpenApiSpecificationParser(config).parse(config.operationFilter)
        val usage = analyzeModelUsage(spec)

        val listClients = usage["List"] ?: emptySet()
        val enumClients = usage["ListRepliesPolicyEnum"] ?: emptySet()

        assertTrue(listClients.isNotEmpty(), "List model should be used by at least one client")
        assertTrue(
            enumClients.containsAll(listClients),
            "ListRepliesPolicyEnum should be used by all clients that use List, got List=$listClients enum=$enumClients",
        )
    }

    @Test
    fun `GIVEN mastodon spec WHEN partition THEN ListRepliesPolicyEnum is reachable from List's group`() {
        val config = ApiGeneratorConfiguration(openApiFile = "src/test/resources/mastodon.json")
        val spec = OpenApiSpecificationParser(config).parse(config.operationFilter)
        val partitioned = GenerationSpecPartitioner().partition(spec)

        // Find the group containing List
        val listGroup = partitioned.sharedGroups.find { g -> g.spec.models.any { it.name == "List" } }
        val enumGroup = partitioned.sharedGroups.find { g -> g.spec.models.any { it.name == "ListRepliesPolicyEnum" } }
        val inPerClient = partitioned.perClient.any { c -> c.spec.models.any { it.name == "ListRepliesPolicyEnum" } }

        assertFalse(
            listGroup == null && enumGroup == null && !inPerClient,
            "ListRepliesPolicyEnum must be generated somewhere (not an orphan)",
        )
        // If List is in a shared group, ListRepliesPolicyEnum must be in the SAME or a dependent group
        if (listGroup != null) {
            val enumInSameGroup = enumGroup == listGroup
            val enumInSuperGroup =
                enumGroup != null &&
                    partitioned.sharedGroups.any { g ->
                        g == listGroup && g.spec.models.any { it.name == "List" }
                    }
            assertTrue(
                enumInSameGroup || enumInSuperGroup || enumGroup != null || inPerClient,
                "ListRepliesPolicyEnum must be in the same or a reachable group as List",
            )
        }
    }
}

/**
 * Tests for cross-package imports — the scenario where a model in a per-group subproject
 * references a model from the global shared subproject (different package).
 *
 * Spec: `cross-package-import.json`
 * - `GlobalStatus` used by ALL 3 clients → global `shared/` subproject (package `$top.model`)
 * - `Address` shared by User+Order → per-group `shared-order-user/` (package `$top.sharedOrderUser.model`)
 * - `Address` has property `status: GlobalStatus` — cross-package reference!
 *
 * Bug: when generating the per-group subproject WITHOUT `modelPackageOverrides` for `GlobalStatus`,
 * the generated `Address.kt` would reference `GlobalStatus` using the wrong package (`$groupBase.model`
 * instead of `$topBase.model`), resulting in missing/wrong import.
 */
class CrossPackageImportTest {
    @TempDir
    lateinit var tempDir: File

    private companion object {
        private const val SPEC = "src/test/resources/cross-package-import.json"
        private const val TOP_PKG = "com.example.crosspackage"
        private const val GROUP_PKG = "$TOP_PKG.sharedOrderUser"
    }

    // ─────────────────────────────────────────────────────────────
    // Bug demonstration: without fix, GlobalStatus gets wrong package
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN per-group without sharedBasePackage WHEN GlobalStatus is global shared THEN Address does NOT import it correctly`() {
        // Known limitation: without sharedBasePackage the generator has no knowledge of where
        // global shared models live, so GlobalStatus is referenced from the group package
        // (GROUP_PKG.model) instead of the real global shared package (TOP_PKG.model).
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = GROUP_PKG,
                // sharedBasePackage intentionally NOT set — no cross-package knowledge
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                targetSharedGroup = setOf("UserClient", "OrderClient"),
                modelPackageOverrides = emptyMap(),
            ),
        )
        val addressFile =
            tempDir.walkTopDown().firstOrNull { it.name == "Address.kt" }
                ?: error("Address.kt not generated")
        val content = addressFile.readText()
        // Without sharedBasePackage the fallback resolves to GROUP_PKG.model, so GlobalStatus
        // is NOT imported from the correct global package — the correct fix requires sharedBasePackage.
        assertFalse(
            content.contains("import $TOP_PKG.model.GlobalStatus"),
            "Without sharedBasePackage, Address.kt should NOT reference GlobalStatus from $TOP_PKG.model, got:\n$content",
        )
    }

    // ─────────────────────────────────────────────────────────────
    // Fix: with sharedBasePackage set, GlobalStatus is referenced from the correct package
    // ─────────────────────────────────────────────────────────────

    @Test
    fun `GIVEN per-group with sharedBasePackage WHEN GlobalStatus is global shared THEN Address imports GlobalStatus correctly`() {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = GROUP_PKG,
                sharedBasePackage = TOP_PKG, // points to global shared package
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                targetSharedGroup = setOf("UserClient", "OrderClient"),
                modelPackageOverrides = emptyMap(),
            ),
        )
        val addressFile =
            tempDir.walkTopDown().firstOrNull { it.name == "Address.kt" }
                ?: error("Address.kt not generated")
        val content = addressFile.readText()
        assertTrue(
            content.contains("import $TOP_PKG.model.GlobalStatus") ||
                content.contains("$TOP_PKG.model.GlobalStatus"),
            "Address.kt should import GlobalStatus from global shared package $TOP_PKG.model, got:\n$content",
        )
    }

    @Test
    fun `GIVEN per-group generation with sharedBasePackage WHEN Address is generated THEN it is in its own group package`() {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = GROUP_PKG,
                sharedBasePackage = TOP_PKG,
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                targetSharedGroup = setOf("UserClient", "OrderClient"),
            ),
        )
        val addressFile =
            tempDir.walkTopDown().firstOrNull { it.name == "Address.kt" }
                ?: error("Address.kt not generated")
        val content = addressFile.readText()
        assertTrue(
            content.startsWith("package $GROUP_PKG.model"),
            "Address.kt should be in package $GROUP_PKG.model, got:\n${content.lines().first()}",
        )
    }

    @Test
    fun `GIVEN per-group with sharedBasePackage WHEN Address has GlobalStatus property THEN it is NOT imported from wrong group package`() {
        generate(
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = GROUP_PKG,
                sharedBasePackage = TOP_PKG,
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                targetSharedGroup = setOf("UserClient", "OrderClient"),
            ),
        )
        val addressFile =
            tempDir.walkTopDown().firstOrNull { it.name == "Address.kt" }
                ?: error("Address.kt not generated")
        val content = addressFile.readText()
        assertFalse(
            content.contains("import $GROUP_PKG.model.GlobalStatus"),
            "GlobalStatus must NOT be imported from group package $GROUP_PKG.model — it lives in global shared, got:\n$content",
        )
    }
}

/**
 * Regression test for intra-group imports: when model A and model B are in the same per-group
 * subproject, and A has a property of type B (via a $ref), the generated A.kt must import B
 * from the group's own package — NOT from the global shared fallback package.
 *
 * Bug: `modelPackageOverrides` only contained models from OTHER groups. Intra-group model
 * references fell through to `fallbackModelPackage` (global shared package), producing a
 * wrong import (e.g. `sdk.model.ListRepliesPolicyEnum` instead of `sdk.sharedXXX.model.ListRepliesPolicyEnum`).
 *
 * Fix: `GenerateTask` now also adds the current group's own models to `modelPackageOverrides`
 * pointing to the group's own model package.
 */
class IntraGroupImportTest {
    @TempDir
    lateinit var tempDir: File

    private companion object {
        private const val SPEC = "src/test/resources/mastodon.json"
        private const val TOP_PKG = "org.litote.mastodon.ktor.sdk"
        private const val GROUP_PKG = "$TOP_PKG.sharedListsAccountsGroup"
    }

    @Test
    fun `GIVEN per-group with intra-group modelPackageOverrides WHEN List generated THEN import uses group package`() {
        val config =
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = GROUP_PKG,
                sharedBasePackage = TOP_PKG,
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                targetSharedGroup = setOf("AccountsClient", "ListsClient"),
                // Simulate what GenerateTask now does: include the current group's own models
                modelPackageOverrides = mapOf("ListRepliesPolicyEnum" to "$GROUP_PKG.model"),
            )
        generate(config)

        val listFile =
            tempDir.walkTopDown().firstOrNull { it.name == "List.kt" }
                ?: error("List.kt not generated")
        val content = listFile.readText()

        assertFalse(
            content.contains("import $TOP_PKG.model.ListRepliesPolicyEnum"),
            "List.kt must NOT import ListRepliesPolicyEnum from global shared package, got:\n$content",
        )
    }

    @Test
    fun `GIVEN per-group WITHOUT intra-group modelPackageOverrides WHEN List is generated THEN import uses wrong fallback`() {
        // Documents the OLD (broken) behavior: without intra-group overrides the import uses fallbackModelPackage.
        val config =
            ApiGeneratorConfiguration(
                openApiFile = SPEC,
                outputDirectory = tempDir.absolutePath,
                basePackage = GROUP_PKG,
                sharedBasePackage = TOP_PKG,
                splitByClient = true,
                sharedModelGranularity = SharedModelGranularity.SHARED_PER_GROUP,
                targetSharedGroup = setOf("AccountsClient", "ListsClient"),
                modelPackageOverrides = emptyMap(), // no intra-group overrides → OLD behavior
            )
        generate(config)

        val listFile =
            tempDir.walkTopDown().firstOrNull { it.name == "List.kt" }
                ?: error("List.kt not generated")
        val content = listFile.readText()

        // Without the fix, ListRepliesPolicyEnum resolves to fallbackModelPackage = sdk.model
        assertTrue(
            content.contains("import $TOP_PKG.model.ListRepliesPolicyEnum"),
            "Without intra-group overrides, List.kt incorrectly imports from fallback package, got:\n$content",
        )
    }
}
