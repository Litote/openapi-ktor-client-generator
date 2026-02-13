package org.litote.openapi.ktor.client.generator.shared

/**
 * Returns a capitalized string.
 */
public fun String.capitalize(): String = if (firstOrNull()?.isLowerCase() == true) replaceFirstChar { it.uppercase() } else this

/**
 * Returns a uncapitalized string.
 */
public fun String.uncapitalize(): String = if (firstOrNull()?.isUpperCase() == true) replaceFirstChar { it.lowercase() } else this

private val snakeToCamelCaseRegex = "_[a-z]".toRegex()

/**
 * Returns true when this string contains an underscore.
 */
public fun String.isSnakeCase(): Boolean = contains('_')

/**
 * Converts snake_case to camelCase by uppercasing each character after an underscore.
 */
public fun String.snakeToCamelCase(): String =
    replace(snakeToCamelCaseRegex) { it.value.last().uppercase() }
        .replace("_", "")

private val tagToCamelCaseRegex = "-[a-z]".toRegex()

/**
 * Converts dash-separated tags to camelCase by uppercasing characters after dashes,
 * removing remaining dashes, and capitalizing the result.
 */
public fun String.tagToCamelCase(): String = replace(tagToCamelCaseRegex) { it.value.last().uppercase() }.replace("-", "").capitalize()

/**
 * Returns a string that ends with the specified suffix.
 * If the string already ends with the suffix, returns the original string unchanged.
 * Otherwise, appends the suffix to the string.
 */
public fun String.ensureEndsWith(suffix: String): String = if (endsWith(suffix)) this else this + suffix
