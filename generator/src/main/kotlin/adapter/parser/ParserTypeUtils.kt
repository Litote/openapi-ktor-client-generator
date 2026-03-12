package org.litote.openapi.ktor.client.generator.adapter.parser

import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName

private val NULLABLE_STRING = STRING.copy(nullable = true)
private val NULLABLE_BOOLEAN = BOOLEAN.copy(nullable = true)
private val NULLABLE_LONG = LONG.copy(nullable = true)
private val NULLABLE_DOUBLE = DOUBLE.copy(nullable = true)
private val NULLABLE_FLOAT = FLOAT.copy(nullable = true)
private val NULLABLE_INT = INT.copy(nullable = true)

internal fun TypeName.isPrimitive(): Boolean = isString() || isBoolean() || isLong() || isDouble() || isFloat() || isInt()

internal fun TypeName.isString(): Boolean = if (isNullable) this == NULLABLE_STRING else this == STRING

internal fun TypeName.isBoolean(): Boolean = if (isNullable) this == NULLABLE_BOOLEAN else this == BOOLEAN

internal fun TypeName.isLong(): Boolean = if (isNullable) this == NULLABLE_LONG else this == LONG

internal fun TypeName.isDouble(): Boolean = if (isNullable) this == NULLABLE_DOUBLE else this == DOUBLE

internal fun TypeName.isFloat(): Boolean = if (isNullable) this == NULLABLE_FLOAT else this == FLOAT

internal fun TypeName.isInt(): Boolean = if (isNullable) this == NULLABLE_INT else this == INT
