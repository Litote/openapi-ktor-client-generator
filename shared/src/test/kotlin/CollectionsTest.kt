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

package org.litote.openapi.ktor.client.generator.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CollectionsTest {
    @Test
    fun `GIVEN non-null value WHEN toOrNull THEN returns pair`() {
        // Given
        val key = "name"
        val value = "John"

        // When
        val result = key toOrNull value

        // Then
        assertEquals(Pair("name", "John"), result)
    }

    @Test
    fun `GIVEN null value WHEN toOrNull THEN returns null`() {
        // Given
        val key = "name"
        val value: String? = null

        // When
        val result = key toOrNull value

        // Then
        assertNull(result)
    }

    @Test
    fun `GIVEN integer key and string value WHEN toOrNull THEN returns pair`() {
        // Given
        val key = 42
        val value = "answer"

        // When
        val result = key toOrNull value

        // Then
        assertEquals(Pair(42, "answer"), result)
    }

    @Test
    fun `GIVEN data class instances WHEN toOrNull with non-null THEN returns pair`() {
        // Given
        data class Person(
            val name: String,
        )

        data class Address(
            val city: String,
        )

        val person = Person("John")
        val address = Address("Paris")

        // When
        val result = person toOrNull address

        // Then
        assertEquals(Pair(person, address), result)
    }

    @Test
    fun `GIVEN data class instances WHEN toOrNull with null THEN returns null`() {
        // Given
        data class Person(
            val name: String,
        )

        data class Address(
            val city: String,
        )

        val person = Person("John")
        val address: Address? = null

        // When
        val result = person toOrNull address

        // Then
        assertNull(result)
    }
}
