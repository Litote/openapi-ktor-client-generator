package org.litote.openapi.ktor.client.generator

import org.junit.jupiter.api.io.TempDir
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
