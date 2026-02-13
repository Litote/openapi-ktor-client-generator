package org.litote.openapi.ktor.client.generator.shared

/**
 * Creates a [Pair] of the calling object and the provided value if the value is not null; otherwise, returns null.
 *
 * @param value the value to pair with the calling object. If null, the function returns null.
 * @return a [Pair] of the receiver object and the provided non-null value, or null if the value is null.
 */
public infix fun <K : Any, V : Any> K.toOrNull(value: V?): Pair<K, V>? = value?.let { Pair(this, it) }
