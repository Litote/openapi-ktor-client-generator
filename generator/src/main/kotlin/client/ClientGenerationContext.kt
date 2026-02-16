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

package org.litote.openapi.ktor.client.generator.client

import com.squareup.kotlinpoet.TypeSpec
import org.litote.openapi.ktor.client.generator.ApiOperation
import org.litote.openapi.ktor.client.generator.ModelGenerator

/**
 * Context for client generation, tracking state during the build process.
 */
internal data class ClientGenerationContext(
    val name: String,
    val operations: List<ApiOperation>,
    val modelGenerator: ModelGenerator,
    var hasHeaders: Boolean = false,
    var hasPathComponents: Boolean = false,
    val additionalEntities: MutableSet<TypeSpec> = mutableSetOf(),
)

/**
 * Context containing the generated client class and metadata.
 */
internal data class ClientFileContext(
    val name: String,
    val operations: List<ApiOperation>,
    val hasHeaders: Boolean,
    val hasPathComponents: Boolean,
    val clientClass: TypeSpec,
) {
    constructor(generationContext: ClientGenerationContext, clientClass: TypeSpec) : this(
        generationContext.name,
        generationContext.operations,
        generationContext.hasHeaders,
        generationContext.hasPathComponents,
        clientClass,
    )
}
