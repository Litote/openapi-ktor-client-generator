/*
 *    Copyright 2026 litote.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.litote.openapi.ktor.client.generator

import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Snapshot-based tests for validating generated code.
 *
 * These tests compare the generated output against "golden" reference files stored
 * in `src/test/resources/snapshots/`.
 *
 * To update snapshots when intentional changes are made to the generator:
 * - Run tests with `UPDATE_SNAPSHOTS=true ./gradlew :generator:test`
 * - Or set the system property: `-DupdateSnapshots=true`
 *
 * The test will fail if generated code differs from snapshots (unless updating).
 */
class SnapshotTest {
    companion object {
        private const val SNAPSHOTS_DIR = "src/test/resources/snapshots"
        private const val OUTPUT_DIR = "build/snapshot-test-output"

        /**
         * Check if we should update snapshots instead of comparing.
         */
        private fun shouldUpdateSnapshots(): Boolean =
            System.getenv("UPDATE_SNAPSHOTS")?.toBoolean() == true ||
                System.getProperty("updateSnapshots")?.toBoolean() == true
    }

    @Test
    fun `GIVEN openapi minimal spec WHEN generating THEN output matches snapshot`() {
        runSnapshotTest(
            snapshotName = "simple-api",
            openApiFile = "src/test/resources/openapi.json",
        )
    }

    @Test
    fun `GIVEN sample spec WHEN generating THEN output matches snapshot`() {
        runSnapshotTest(
            snapshotName = "sample-api",
            openApiFile = "src/test/resources/sample.json",
        )
    }

    @Test
    @Ignore(
        "OpenAPI 3.1.0 is not yet supported by kotlinx-openapi-bindings see https://github.com/flock-community/kotlin-openapi-bindings/issues/8",
    )
    fun `GIVEN mastodon openapi spec WHEN generating THEN output matches snapshot`() {
        runSnapshotTest(
            snapshotName = "mastodon-api",
            openApiFile = "src/test/resources/mastodon.json",
        )
    }

    /**
     * Runs a snapshot test for a given OpenAPI specification.
     */
    private fun runSnapshotTest(
        snapshotName: String,
        openApiFile: String,
    ) {
        // Given
        val config =
            ApiGeneratorConfiguration(
                openApiFile = openApiFile,
                outputDirectory = "$OUTPUT_DIR/$snapshotName",
                basePackage = snapshotName.replace('-', '.'),
            )

        // Clean output directory
        File(config.outputDirectory).deleteRecursively()

        // When
        val result = generate(config)

        // Then
        val errorMessage =
            when (result) {
                is GenerationResult.Failure -> "Generation failed: ${result.message}\nCause: ${result.error}"
                else -> "Generation should succeed"
            }
        assertTrue(result.isSuccess, errorMessage)

        val snapshotDir = File("$SNAPSHOTS_DIR/$snapshotName")
        val outputDir = File(config.outputDirectory)

        if (shouldUpdateSnapshots()) {
            updateSnapshots(outputDir, snapshotDir)
            println("Snapshots updated for '$snapshotName'")
        } else {
            compareWithSnapshots(outputDir, snapshotDir, snapshotName)
        }
    }

    /**
     * Updates the snapshot files from the generated output.
     */
    private fun updateSnapshots(
        outputDir: File,
        snapshotDir: File,
    ) {
        // Delete existing snapshots
        snapshotDir.deleteRecursively()
        snapshotDir.mkdirs()

        // Copy generated files to snapshot directory
        outputDir
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { generatedFile ->
                val relativePath = generatedFile.relativeTo(outputDir)
                val snapshotFile = snapshotDir.resolve(relativePath)
                snapshotFile.parentFile.mkdirs()
                generatedFile.copyTo(snapshotFile, overwrite = true)
            }
    }

    /**
     * Compares generated files against snapshots.
     */
    private fun compareWithSnapshots(
        outputDir: File,
        snapshotDir: File,
        snapshotName: String,
    ) {
        if (!snapshotDir.exists()) {
            fail(
                """
                |Snapshot directory not found: $snapshotDir
                |Run with UPDATE_SNAPSHOTS=true to create initial snapshots:
                |  UPDATE_SNAPSHOTS=true ./gradlew :generator:test --tests "*SnapshotTest*"
                """.trimMargin(),
            )
        }

        val generatedFiles =
            outputDir
                .walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .map { it.relativeTo(outputDir).path to it.readText() }
                .toMap()

        val snapshotFiles =
            snapshotDir
                .walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .map { it.relativeTo(snapshotDir).path to it.readText() }
                .toMap()

        // Check for missing files in generated output
        val missingInGenerated = snapshotFiles.keys - generatedFiles.keys
        if (missingInGenerated.isNotEmpty()) {
            fail(
                """
                |Files present in snapshot but not generated for '$snapshotName':
                |${missingInGenerated.joinToString("\n") { "  - $it" }}
                """.trimMargin(),
            )
        }

        // Check for extra files in generated output
        val extraInGenerated = generatedFiles.keys - snapshotFiles.keys
        if (extraInGenerated.isNotEmpty()) {
            fail(
                """
                |Files generated but not in snapshot for '$snapshotName':
                |${extraInGenerated.joinToString("\n") { "  - $it" }}
                |Run with UPDATE_SNAPSHOTS=true to update snapshots.
                """.trimMargin(),
            )
        }

        // Compare content of each file
        val differences = mutableListOf<String>()
        for ((path, snapshotContent) in snapshotFiles) {
            val generatedContent = generatedFiles[path]!!
            if (snapshotContent != generatedContent) {
                differences.add(buildDiffMessage(path, snapshotContent, generatedContent))
            }
        }

        if (differences.isNotEmpty()) {
            fail(
                """
                |Snapshot mismatch for '$snapshotName':
                |${differences.joinToString("\n\n")}
                |
                |Run with UPDATE_SNAPSHOTS=true to update snapshots if these changes are intentional:
                |  UPDATE_SNAPSHOTS=true ./gradlew :generator:test --tests "*SnapshotTest*"
                """.trimMargin(),
            )
        }
    }

    /**
     * Builds a human-readable diff message for a file.
     */
    private fun buildDiffMessage(
        path: String,
        expected: String,
        actual: String,
    ): String {
        val expectedLines = expected.lines()
        val actualLines = actual.lines()

        val diffLines = mutableListOf<String>()
        diffLines.add("=== $path ===")

        val maxLines = maxOf(expectedLines.size, actualLines.size)
        var diffCount = 0
        val maxDiffs = 10 // Limit number of diffs shown

        for (i in 0 until maxLines) {
            val expectedLine = expectedLines.getOrNull(i)
            val actualLine = actualLines.getOrNull(i)

            if (expectedLine != actualLine && diffCount < maxDiffs) {
                diffCount++
                diffLines.add("Line ${i + 1}:")
                if (expectedLine != null) {
                    diffLines.add("  - (expected) $expectedLine")
                } else {
                    diffLines.add("  - (expected) <missing>")
                }
                if (actualLine != null) {
                    diffLines.add("  + (actual)   $actualLine")
                } else {
                    diffLines.add("  + (actual)   <missing>")
                }
            }
        }

        if (diffCount >= maxDiffs) {
            diffLines.add("... and more differences")
        }

        return diffLines.joinToString("\n")
    }
}
