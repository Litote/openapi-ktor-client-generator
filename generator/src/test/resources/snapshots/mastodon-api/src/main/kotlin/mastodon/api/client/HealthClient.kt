package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import kotlin.Int
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class HealthClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get basic health status as JSON
   */
  public suspend fun getHealth(): GetHealthResponse {
    try {
      val response = configuration.client.`get`("health") {
      }
      return when (response.status.value) {
        200 -> GetHealthResponseSuccess(response.headers)
        401, 404, 429, 503 -> GetHealthResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetHealthResponseFailure410(response.headers)
        422 -> GetHealthResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetHealthResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetHealthResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetHealthResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetHealthResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetHealthResponse() {
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
  public data class GetHealthResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetHealthResponse()

  @Serializable
  public data class GetHealthResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetHealthResponse()

  @Serializable
  public data class GetHealthResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetHealthResponse()

  @Serializable
  public data class GetHealthResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetHealthResponse()
}
