package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.plugins.sse.ClientSSESession
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.`get`
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlinx.serialization.Serializable
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
        200 -> GetStreamingHealthResponseSuccess
        401, 404, 429, 503 -> GetStreamingHealthResponseFailure401(response.body<Error>())
        410 -> GetStreamingHealthResponseFailure410
        422 -> GetStreamingHealthResponseFailure(response.body<ValidationError>())
        else -> GetStreamingHealthResponseUnknownFailure(response.status.value)
      }
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
    catch(e: Exception) {
      configuration.exceptionLogger(e)
    }
  }

  @Serializable
  public sealed class GetStreamingHealthResponse

  @Serializable
  public object GetStreamingHealthResponseSuccess : GetStreamingHealthResponse()

  @Serializable
  public data class GetStreamingHealthResponseFailure401(
    public val body: Error,
  ) : GetStreamingHealthResponse()

  @Serializable
  public object GetStreamingHealthResponseFailure410 : GetStreamingHealthResponse()

  @Serializable
  public data class GetStreamingHealthResponseFailure(
    public val body: ValidationError,
  ) : GetStreamingHealthResponse()

  @Serializable
  public data class GetStreamingHealthResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStreamingHealthResponse()
}
