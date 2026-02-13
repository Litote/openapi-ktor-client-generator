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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringsTest {
    @Test
    fun `GIVEN lowercase string WHEN capitalize THEN first char is uppercase`() {
        // Given
        val input = "hello"

        // When
        val result = input.capitalize()

        // Then
        assertEquals("Hello", result)
    }

    @Test
    fun `GIVEN uppercase string WHEN capitalize THEN string unchanged`() {
        // Given
        val input = "Hello"

        // When
        val result = input.capitalize()

        // Then
        assertEquals("Hello", result)
    }

    @Test
    fun `GIVEN empty string WHEN capitalize THEN returns empty string`() {
        // Given
        val input = ""

        // When
        val result = input.capitalize()

        // Then
        assertEquals("", result)
    }

    @Test
    fun `GIVEN uppercase string WHEN uncapitalize THEN first char is lowercase`() {
        // Given
        val input = "Hello"

        // When
        val result = input.uncapitalize()

        // Then
        assertEquals("hello", result)
    }

    @Test
    fun `GIVEN lowercase string WHEN uncapitalize THEN string unchanged`() {
        // Given
        val input = "hello"

        // When
        val result = input.uncapitalize()

        // Then
        assertEquals("hello", result)
    }

    @Test
    fun `GIVEN empty string WHEN uncapitalize THEN returns empty string`() {
        // Given
        val input = ""

        // When
        val result = input.uncapitalize()

        // Then
        assertEquals("", result)
    }

    @Test
    fun `GIVEN string with underscore WHEN isSnakeCase THEN returns true`() {
        // Given
        val input = "hello_world"

        // When
        val result = input.isSnakeCase()

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN string without underscore WHEN isSnakeCase THEN returns false`() {
        // Given
        val input = "helloWorld"

        // When
        val result = input.isSnakeCase()

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN snake_case string WHEN snakeToCamelCase THEN converts to camelCase`() {
        // Given
        val input = "hello_world"

        // When
        val result = input.snakeToCamelCase()

        // Then
        assertEquals("helloWorld", result)
    }

    @Test
    fun `GIVEN multi_segment_snake_case WHEN snakeToCamelCase THEN converts all segments`() {
        // Given
        val input = "user_first_name"

        // When
        val result = input.snakeToCamelCase()

        // Then
        assertEquals("userFirstName", result)
    }

    @Test
    fun `GIVEN string without underscores WHEN snakeToCamelCase THEN returns unchanged`() {
        // Given
        val input = "hello"

        // When
        val result = input.snakeToCamelCase()

        // Then
        assertEquals("hello", result)
    }

    @Test
    fun `GIVEN dash-separated tag WHEN tagToCamelCase THEN converts to capitalized camelCase`() {
        // Given
        val input = "user-management"

        // When
        val result = input.tagToCamelCase()

        // Then
        assertEquals("UserManagement", result)
    }

    @Test
    fun `GIVEN multi-segment-tag WHEN tagToCamelCase THEN converts all segments and capitalizes`() {
        // Given
        val input = "user-first-name"

        // When
        val result = input.tagToCamelCase()

        // Then
        assertEquals("UserFirstName", result)
    }

    @Test
    fun `GIVEN tag without dashes WHEN tagToCamelCase THEN capitalizes first char`() {
        // Given
        val input = "hello"

        // When
        val result = input.tagToCamelCase()

        // Then
        assertEquals("Hello", result)
    }

    @Test
    fun `GIVEN single char lowercase WHEN tagToCamelCase THEN returns uppercase`() {
        // Given
        val input = "a"

        // When
        val result = input.tagToCamelCase()

        // Then
        assertEquals("A", result)
    }
}
