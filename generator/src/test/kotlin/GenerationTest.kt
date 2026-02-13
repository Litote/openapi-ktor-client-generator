/*
 * Copyright 2026 litote.org
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

import kotlin.test.Test
import kotlin.test.assertTrue

class GenerationTest {
    @Test
    fun `GIVEN simple openapi json file WHEN generating client THEN generation succeeds with expected counts`() {
        val config =
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/openapi.json",
                outputDirectory = "build/openapi",
            )

        // When
        val result = generate(config)

        // Then
        assertTrue(result.isSuccess, "Generation should succeed")
        val success = result.getOrNull()!!
        assertTrue(success.clientsGenerated > 0, "Should generate at least one client")
        assertTrue(success.modelsGenerated > 0, "Should generate at least one model")
    }

    @Test
    fun `GIVEN non-existent openapi file WHEN generating client THEN generation fails`() {
        val config =
            ApiGeneratorConfiguration(
                openApiFile = "non-existent-file.json",
                outputDirectory = "build/openapi",
            )

        // When
        val result = generate(config)

        // Then
        assertTrue(result.isFailure, "Generation should fail for non-existent file")
        val failure = result as GenerationResult.Failure
        assertTrue(failure.message.contains("non-existent-file.json"), "Error message should contain filename")
    }
}
