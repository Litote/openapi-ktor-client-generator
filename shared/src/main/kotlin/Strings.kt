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

private val illegalIdentifierCharRegex = "[^a-zA-Z0-9_]".toRegex()
private val sanitizeToCamelCaseRegex = "[^a-zA-Z0-9][a-z]".toRegex()

/**
 * Returns true when this string contains characters that are not valid in a Kotlin identifier.
 */
public fun String.hasIllegalIdentifierChars(): Boolean = illegalIdentifierCharRegex.containsMatchIn(this)

/**
 * Converts a string with illegal identifier characters to a valid camelCase identifier.
 * For example, "planetiler:buildtime" -> "planetilerBuildtime"
 */
public fun String.sanitizeToIdentifier(): String =
    replace(sanitizeToCamelCaseRegex) { it.value.last().uppercase() }
        .replace(illegalIdentifierCharRegex, "")

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

private val camelCaseToSnakeCaseRegex = "([a-z])([A-Z])".toRegex()

/**
 * Converts camelCase or dash-separated strings to UPPER_SNAKE_CASE.
 * For example:
 * - "camelCase" -> "CAMEL_CASE"
 * - "my-tag" -> "MY_TAG"
 * - "myTagName" -> "MY_TAG_NAME"
 * - "my-tagName" -> "MY_TAG_NAME"
 */
public fun String.toUpperSnakeCase(): String =
    replace("-", "_")
        .replace(camelCaseToSnakeCaseRegex) { "${it.groupValues[1]}_${it.groupValues[2]}" }
        .uppercase()
