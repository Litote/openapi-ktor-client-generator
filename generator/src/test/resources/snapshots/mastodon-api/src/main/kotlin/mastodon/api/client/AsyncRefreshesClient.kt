package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.AsyncRefreshResponse
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class AsyncRefreshesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get Status of Async Refresh
   */
  public suspend fun getAsyncRefreshV1Alpha(id: String): GetAsyncRefreshV1AlphaResponse {
    try {
      val response = configuration.client.`get`("api/v1_alpha/async_refreshes/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetAsyncRefreshV1AlphaResponseSuccess(response.body<AsyncRefreshResponse>(), response.headers)
        401, 404, 429, 503 -> GetAsyncRefreshV1AlphaResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAsyncRefreshV1AlphaResponseFailure410(response.headers)
        422 -> GetAsyncRefreshV1AlphaResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAsyncRefreshV1AlphaResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAsyncRefreshV1AlphaResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetAsyncRefreshV1AlphaResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAsyncRefreshV1AlphaResponseSuccess(
    public val body: AsyncRefreshResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAsyncRefreshV1AlphaResponse() {
    /**
     * Number of requests permitted per time period
     */
    public val xRateLimitLimit: Int?
      get() = headers["X-RateLimit-Limit"]?.toIntOrNull()

    /**
     * Number of requests you can still make
     */
    public val xRateLimitRemaining: Int?
      get() = headers["X-RateLimit-Remaining"]?.toIntOrNull()

    /**
     * Timestamp when your rate limit will reset
     */
    public val xRateLimitReset: String?
      get() = headers["X-RateLimit-Reset"]
  }

  @Serializable
  public data class GetAsyncRefreshV1AlphaResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAsyncRefreshV1AlphaResponse()

  @Serializable
  public data class GetAsyncRefreshV1AlphaResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAsyncRefreshV1AlphaResponse()

  @Serializable
  public data class GetAsyncRefreshV1AlphaResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAsyncRefreshV1AlphaResponse()

  @Serializable
  public data class GetAsyncRefreshV1AlphaResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAsyncRefreshV1AlphaResponse()
}
