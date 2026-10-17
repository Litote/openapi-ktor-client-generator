package yaml.api.client

import io.ktor.http.Headers
import kotlin.Int

/**
 * Response of an operation: [T] is the body type of its successful responses.
 */
public sealed interface OperationResponse<out T> {
  public val headers: Headers
}

/**
 * Successful response declared by the specification ([body] is `Unit` when it has none).
 */
public sealed interface OperationResponseSuccess<out T> : OperationResponse<T> {
  public val body: T
}

/**
 * Failure response declared by the specification, of body [E] (`Unit` when it has none).
 */
public sealed interface OperationResponseFailure<out E> {
  public val body: E
}

/**
 * Response with a status not declared by the specification, or failure of the call (network error, unreadable body: status 500).
 */
public sealed interface OperationResponseUnknownFailure {
  public val statusCode: Int
}
