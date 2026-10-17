package org.example

import org.example.client.OperationResponse
import org.example.client.OperationResponseFailure
import org.example.client.OperationResponseSuccess
import org.example.client.OperationResponseUnknownFailure

/**
 * Body of a successful response. Compiled by the e2e build: the common response interfaces are sealed in a single
 * module, so this `when` is exhaustive without `else` branch.
 */
internal fun <T> OperationResponse<T>.bodyOrThrow(): T =
    when (this) {
        is OperationResponseSuccess<T> -> body
        is OperationResponseFailure<*> -> error("API error: $body")
        is OperationResponseUnknownFailure -> error("HTTP status $statusCode")
    }
