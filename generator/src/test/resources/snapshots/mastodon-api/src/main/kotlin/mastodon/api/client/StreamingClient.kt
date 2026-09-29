package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.plugins.sse.ClientSSESession
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class StreamingClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Watch for direct messages
   */
  public suspend fun getStreamingDirect(block: suspend ClientSSESession.() -> Unit) {
    try {
      configuration.client.sse(urlString = "api/v1/streaming/direct") {
        block()
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
    }
  }

  /**
   * Watch the public timeline for a hashtag
   */
  public suspend fun getStreamingHashtag(tag: String, block: suspend ClientSSESession.() -> Unit) {
    try {
      configuration.client.sse(urlString = "api/v1/streaming/hashtag", request = {
        url {
          parameters.append("tag", tag)
        }
      }
      ) {
        block()
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
    }
  }

  /**
   * Watch the local timeline for a hashtag
   */
  public suspend fun getStreamingHashtagLocal(tag: String, block: suspend ClientSSESession.() -> Unit) {
    try {
      configuration.client.sse(urlString = "api/v1/streaming/hashtag/local", request = {
        url {
          parameters.append("tag", tag)
        }
      }
      ) {
        block()
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
    }
  }

  /**
   * Check if the server is alive
   */
  public suspend fun getStreamingHealth(): GetStreamingHealthResponse {
    try {
      val response = configuration.client.`get`("api/v1/streaming/health") {
      }
      return when (response.status.value) {
        200 -> GetStreamingHealthResponseSuccess(response.headers)
        401, 404, 429, 503 -> GetStreamingHealthResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetStreamingHealthResponseFailure410(response.headers)
        422 -> GetStreamingHealthResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetStreamingHealthResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStreamingHealthResponseUnknownFailure(500)
    }
  }

  /**
   * Watch for list updates
   */
  public suspend fun getStreamingList(list: String, block: suspend ClientSSESession.() -> Unit) {
    try {
      configuration.client.sse(urlString = "api/v1/streaming/list", request = {
        url {
          parameters.append("list", list)
        }
      }
      ) {
        block()
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
    }
  }

  /**
   * Watch the federated timeline
   */
  public suspend fun getStreamingPublic(onlyMedia: Boolean? = null, block: suspend ClientSSESession.() -> Unit) {
    try {
      configuration.client.sse(urlString = "api/v1/streaming/public", request = {
        url {
          if (onlyMedia != null) {
            parameters.append("only_media", onlyMedia.toString())
          }
        }
      }
      ) {
        block()
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
    }
  }

  /**
   * Watch the local timeline
   */
  public suspend fun getStreamingPublicLocal(onlyMedia: Boolean? = null, block: suspend ClientSSESession.() -> Unit) {
    try {
      configuration.client.sse(urlString = "api/v1/streaming/public/local", request = {
        url {
          if (onlyMedia != null) {
            parameters.append("only_media", onlyMedia.toString())
          }
        }
      }
      ) {
        block()
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
    }
  }

  /**
   * Watch for remote statuses
   */
  public suspend fun getStreamingPublicRemote(onlyMedia: Boolean? = null, block: suspend ClientSSESession.() -> Unit) {
    try {
      configuration.client.sse(urlString = "api/v1/streaming/public/remote", request = {
        url {
          if (onlyMedia != null) {
            parameters.append("only_media", onlyMedia.toString())
          }
        }
      }
      ) {
        block()
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
    }
  }

  /**
   * Watch your home timeline and notifications
   */
  public suspend fun getStreamingUser(block: suspend ClientSSESession.() -> Unit) {
    try {
      configuration.client.sse(urlString = "api/v1/streaming/user") {
        block()
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
    }
  }

  /**
   * Watch your notifications
   */
  public suspend fun getStreamingUserNotification(block: suspend ClientSSESession.() -> Unit) {
    try {
      configuration.client.sse(urlString = "api/v1/streaming/user/notification") {
        block()
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
    }
  }

  @Serializable
  public sealed class GetStreamingHealthResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetStreamingHealthResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStreamingHealthResponse() {
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
  public data class GetStreamingHealthResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStreamingHealthResponse()

  @Serializable
  public data class GetStreamingHealthResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStreamingHealthResponse()

  @Serializable
  public data class GetStreamingHealthResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStreamingHealthResponse()

  @Serializable
  public data class GetStreamingHealthResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStreamingHealthResponse()
}
