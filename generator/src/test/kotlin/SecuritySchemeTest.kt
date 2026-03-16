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

import org.litote.openapi.ktor.client.generator.adapter.parser.OpenApiSpecificationParser
import org.litote.openapi.ktor.client.generator.domain.SecuritySchemeLocationSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SecuritySchemeTest {
    @Test
    fun `GIVEN karto spec with security schemes WHEN parsing THEN security schemes are extracted`() {
        val configuration =
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/sample.json",
                outputDirectory = "build/test-security",
                basePackage = "org.example",
            )

        val spec = OpenApiSpecificationParser(configuration).parse { true }

        assertEquals(2, spec.clientConfiguration.apiKeySchemes.size, "Should have 2 API key security schemes")

        val headerScheme = spec.clientConfiguration.apiKeySchemes.find { it.location == SecuritySchemeLocationSpec.HEADER }
        assertTrue(headerScheme != null, "Should have a header API key scheme")
        assertEquals("api_key_header", headerScheme.name)
        assertEquals("X-Api-Key", headerScheme.keyName)

        val queryScheme = spec.clientConfiguration.apiKeySchemes.find { it.location == SecuritySchemeLocationSpec.QUERY }
        assertTrue(queryScheme != null, "Should have a query API key scheme")
        assertEquals("api_key_query_param", queryScheme.name)
        assertEquals("api_key", queryScheme.keyName)
    }

    @Test
    fun `GIVEN spec without security schemes WHEN parsing THEN empty list returned`() {
        val configuration =
            ApiGeneratorConfiguration(
                openApiFile = "src/test/resources/openapi.json",
                outputDirectory = "build/test-security",
                basePackage = "org.example",
            )

        val spec = OpenApiSpecificationParser(configuration).parse { true }

        assertTrue(spec.clientConfiguration.apiKeySchemes.isEmpty(), "Should have no API key security schemes")
    }
}
