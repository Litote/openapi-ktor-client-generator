/*
 * Copyright 2026 litote.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litote.openapi.ktor.client.generator

import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class GeneratedCodeSafetyTest {
    private companion object {
        val outputDirectory: File by lazy {
            File("build/test-generated-code-safety").also { directory ->
                directory.deleteRecursively()
                generate(
                    ApiGeneratorConfiguration(
                        openApiFile = "src/test/resources/unsafe-values.json",
                        outputDirectory = directory.path,
                        basePackage = "org.example",
                    ),
                )
            }
        }
    }

    private fun generatedFile(path: String): String = File(outputDirectory, "src/main/kotlin/org/example/$path").readText()

    @Test
    fun `GIVEN default configuration WHEN generating THEN credentials headers are masked in the logs`() {
        val configuration = generatedFile("client/ClientConfiguration.kt")

        assertContains(configuration, "sanitizeHeader")
        assertContains(configuration, "\"authorization\"")
        assertContains(configuration, "\"cookie\"")
        assertContains(configuration, "\"set-cookie\"")
    }

    @Test
    fun `GIVEN header api key WHEN generating THEN its header is masked in the logs`() {
        assertContains(generatedFile("client/ClientConfiguration.kt"), "\"x-secret-token\"")
    }

    @Test
    fun `GIVEN path with quotes and dollar WHEN generating THEN the path stays a string literal`() {
        val client = generatedFile("client/UnsafeClient.kt")

        assertFalse(client.contains("""items"); evil(""""), "the path must not close its string literal")
        assertContains(client, """items\"); evil(\"/${'$'}{'${'$'}'}x""")
    }

    @Test
    fun `GIVEN string default on a non string property WHEN generating THEN it is not rendered as code`() {
        val model = generatedFile("model/Item.kt")

        assertFalse(model.contains("evil()"), "a default must not be rendered as code:\n$model")
        assertContains(model, "size: Long? = 3")
    }

    @Test
    fun `GIVEN string default on an enum WHEN generating THEN it is the name of the enum constant`() {
        assertContains(generatedFile("model/Item.kt"), "mode: Mode? = Mode.SLOW")
    }

    @Test
    fun `GIVEN string default on a string property WHEN generating THEN it stays a string literal`() {
        assertContains(generatedFile("model/Item.kt"), """a \"quoted\" ${'$'}{'${'$'}'}value""")
    }

    @Test
    fun `GIVEN binary form field WHEN generating THEN the filename is escaped in Content-Disposition`() {
        assertContains(generatedFile("client/UnsafeClient.kt"), """.filename.replace("\\", "\\\\").replace("\"", "\\\"")""")
    }
}
