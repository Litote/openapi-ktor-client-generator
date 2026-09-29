package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.CustomEmoji
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class CustomEmojisClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View all custom emoji
   */
  public suspend fun getCustomEmojis(): GetCustomEmojisResponse {
    try {
      val response = configuration.client.`get`("api/v1/custom_emojis") {
      }
      return when (response.status.value) {
        200 -> GetCustomEmojisResponseSuccess(response.body<List<CustomEmoji>>(), response.headers)
        401, 404, 429, 503 -> GetCustomEmojisResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetCustomEmojisResponseFailure410(response.headers)
        422 -> GetCustomEmojisResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetCustomEmojisResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetCustomEmojisResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetCustomEmojisResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetCustomEmojisResponseSuccess(
    public val body: List<CustomEmoji>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetCustomEmojisResponse() {
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
  public data class GetCustomEmojisResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetCustomEmojisResponse()

  @Serializable
  public data class GetCustomEmojisResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetCustomEmojisResponse()

  @Serializable
  public data class GetCustomEmojisResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetCustomEmojisResponse()

  @Serializable
  public data class GetCustomEmojisResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetCustomEmojisResponse()
}
