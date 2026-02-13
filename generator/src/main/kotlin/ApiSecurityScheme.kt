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

/**
 * Represents an API Key security scheme from OpenAPI specification.
 *
 * @property name The name of the security scheme (e.g., "api_key_header")
 * @property keyName The actual name of the API key parameter (e.g., "X-Api-Key")
 * @property location Where the API key should be sent ("header" or "query")
 */
public data class ApiSecurityScheme(
    val name: String,
    val keyName: String,
    val location: ApiKeyLocation,
) {
    public enum class ApiKeyLocation {
        HEADER,
        QUERY,
    }
}
