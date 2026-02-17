package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Boolean
import kotlin.Int
import kotlin.String
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
  public suspend fun getStreamingDirect(): GetStreamingDirectResponse {
    try {
      val response = configuration.client.`get`("api/v1/streaming/direct") {
      }
      return when (response.status.value) {
        200 -> GetStreamingDirectResponseSuccess
        401, 404, 429, 503 -> GetStreamingDirectResponseFailure401(response.body<Error>())
        410 -> GetStreamingDirectResponseFailure410
        422 -> GetStreamingDirectResponseFailure(response.body<ValidationError>())
        else -> GetStreamingDirectResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStreamingDirectResponseUnknownFailure(500)
    }
  }

  /**
   * Watch the public timeline for a hashtag
   */
  public suspend fun getStreamingHashtag(tag: String): GetStreamingHashtagResponse {
    try {
      val response = configuration.client.`get`("api/v1/streaming/hashtag") {
        url {
          parameters.append("tag", tag)
        }
      }
      return when (response.status.value) {
        200 -> GetStreamingHashtagResponseSuccess
        401, 404, 429, 503 -> GetStreamingHashtagResponseFailure401(response.body<Error>())
        410 -> GetStreamingHashtagResponseFailure410
        422 -> GetStreamingHashtagResponseFailure(response.body<ValidationError>())
        else -> GetStreamingHashtagResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStreamingHashtagResponseUnknownFailure(500)
    }
  }

  /**
   * Watch the local timeline for a hashtag
   */
  public suspend fun getStreamingHashtagLocal(tag: String): GetStreamingHashtagLocalResponse {
    try {
      val response = configuration.client.`get`("api/v1/streaming/hashtag/local") {
        url {
          parameters.append("tag", tag)
        }
      }
      return when (response.status.value) {
        200 -> GetStreamingHashtagLocalResponseSuccess
        401, 404, 429, 503 -> GetStreamingHashtagLocalResponseFailure401(response.body<Error>())
        410 -> GetStreamingHashtagLocalResponseFailure410
        422 -> GetStreamingHashtagLocalResponseFailure(response.body<ValidationError>())
        else -> GetStreamingHashtagLocalResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStreamingHashtagLocalResponseUnknownFailure(500)
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
  public suspend fun getStreamingList(list: String): GetStreamingListResponse {
    try {
      val response = configuration.client.`get`("api/v1/streaming/list") {
        url {
          parameters.append("list", list)
        }
      }
      return when (response.status.value) {
        200 -> GetStreamingListResponseSuccess
        401, 404, 429, 503 -> GetStreamingListResponseFailure401(response.body<Error>())
        410 -> GetStreamingListResponseFailure410
        422 -> GetStreamingListResponseFailure(response.body<ValidationError>())
        else -> GetStreamingListResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStreamingListResponseUnknownFailure(500)
    }
  }

  /**
   * Watch the federated timeline
   */
  public suspend fun getStreamingPublic(onlyMedia: Boolean? = null): GetStreamingPublicResponse {
    try {
      val response = configuration.client.`get`("api/v1/streaming/public") {
        url {
          if (onlyMedia != null) {
            parameters.append("only_media", onlyMedia.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetStreamingPublicResponseSuccess
        401, 404, 429, 503 -> GetStreamingPublicResponseFailure401(response.body<Error>())
        410 -> GetStreamingPublicResponseFailure410
        422 -> GetStreamingPublicResponseFailure(response.body<ValidationError>())
        else -> GetStreamingPublicResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStreamingPublicResponseUnknownFailure(500)
    }
  }

  /**
   * Watch the local timeline
   */
  public suspend fun getStreamingPublicLocal(onlyMedia: Boolean? = null): GetStreamingPublicLocalResponse {
    try {
      val response = configuration.client.`get`("api/v1/streaming/public/local") {
        url {
          if (onlyMedia != null) {
            parameters.append("only_media", onlyMedia.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetStreamingPublicLocalResponseSuccess
        401, 404, 429, 503 -> GetStreamingPublicLocalResponseFailure401(response.body<Error>())
        410 -> GetStreamingPublicLocalResponseFailure410
        422 -> GetStreamingPublicLocalResponseFailure(response.body<ValidationError>())
        else -> GetStreamingPublicLocalResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStreamingPublicLocalResponseUnknownFailure(500)
    }
  }

  /**
   * Watch for remote statuses
   */
  public suspend fun getStreamingPublicRemote(onlyMedia: Boolean? = null): GetStreamingPublicRemoteResponse {
    try {
      val response = configuration.client.`get`("api/v1/streaming/public/remote") {
        url {
          if (onlyMedia != null) {
            parameters.append("only_media", onlyMedia.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetStreamingPublicRemoteResponseSuccess
        401, 404, 429, 503 -> GetStreamingPublicRemoteResponseFailure401(response.body<Error>())
        410 -> GetStreamingPublicRemoteResponseFailure410
        422 -> GetStreamingPublicRemoteResponseFailure(response.body<ValidationError>())
        else -> GetStreamingPublicRemoteResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStreamingPublicRemoteResponseUnknownFailure(500)
    }
  }

  /**
   * Watch your home timeline and notifications
   */
  public suspend fun getStreamingUser(): GetStreamingUserResponse {
    try {
      val response = configuration.client.`get`("api/v1/streaming/user") {
      }
      return when (response.status.value) {
        200 -> GetStreamingUserResponseSuccess
        401, 404, 429, 503 -> GetStreamingUserResponseFailure401(response.body<Error>())
        410 -> GetStreamingUserResponseFailure410
        422 -> GetStreamingUserResponseFailure(response.body<ValidationError>())
        else -> GetStreamingUserResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStreamingUserResponseUnknownFailure(500)
    }
  }

  /**
   * Watch your notifications
   */
  public suspend fun getStreamingUserNotification(): GetStreamingUserNotificationResponse {
    try {
      val response = configuration.client.`get`("api/v1/streaming/user/notification") {
      }
      return when (response.status.value) {
        200 -> GetStreamingUserNotificationResponseSuccess
        401, 404, 429, 503 -> GetStreamingUserNotificationResponseFailure401(response.body<Error>())
        410 -> GetStreamingUserNotificationResponseFailure410
        422 -> GetStreamingUserNotificationResponseFailure(response.body<ValidationError>())
        else -> GetStreamingUserNotificationResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStreamingUserNotificationResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetStreamingDirectResponse

  @Serializable
  public object GetStreamingDirectResponseSuccess : GetStreamingDirectResponse()

  @Serializable
  public data class GetStreamingDirectResponseFailure401(
    public val body: Error,
  ) : GetStreamingDirectResponse()

  @Serializable
  public object GetStreamingDirectResponseFailure410 : GetStreamingDirectResponse()

  @Serializable
  public data class GetStreamingDirectResponseFailure(
    public val body: ValidationError,
  ) : GetStreamingDirectResponse()

  @Serializable
  public data class GetStreamingDirectResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStreamingDirectResponse()

  @Serializable
  public sealed class GetStreamingHashtagResponse

  @Serializable
  public object GetStreamingHashtagResponseSuccess : GetStreamingHashtagResponse()

  @Serializable
  public data class GetStreamingHashtagResponseFailure401(
    public val body: Error,
  ) : GetStreamingHashtagResponse()

  @Serializable
  public object GetStreamingHashtagResponseFailure410 : GetStreamingHashtagResponse()

  @Serializable
  public data class GetStreamingHashtagResponseFailure(
    public val body: ValidationError,
  ) : GetStreamingHashtagResponse()

  @Serializable
  public data class GetStreamingHashtagResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStreamingHashtagResponse()

  @Serializable
  public sealed class GetStreamingHashtagLocalResponse

  @Serializable
  public object GetStreamingHashtagLocalResponseSuccess : GetStreamingHashtagLocalResponse()

  @Serializable
  public data class GetStreamingHashtagLocalResponseFailure401(
    public val body: Error,
  ) : GetStreamingHashtagLocalResponse()

  @Serializable
  public object GetStreamingHashtagLocalResponseFailure410 : GetStreamingHashtagLocalResponse()

  @Serializable
  public data class GetStreamingHashtagLocalResponseFailure(
    public val body: ValidationError,
  ) : GetStreamingHashtagLocalResponse()

  @Serializable
  public data class GetStreamingHashtagLocalResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStreamingHashtagLocalResponse()

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

  @Serializable
  public sealed class GetStreamingListResponse

  @Serializable
  public object GetStreamingListResponseSuccess : GetStreamingListResponse()

  @Serializable
  public data class GetStreamingListResponseFailure401(
    public val body: Error,
  ) : GetStreamingListResponse()

  @Serializable
  public object GetStreamingListResponseFailure410 : GetStreamingListResponse()

  @Serializable
  public data class GetStreamingListResponseFailure(
    public val body: ValidationError,
  ) : GetStreamingListResponse()

  @Serializable
  public data class GetStreamingListResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStreamingListResponse()

  @Serializable
  public sealed class GetStreamingPublicResponse

  @Serializable
  public object GetStreamingPublicResponseSuccess : GetStreamingPublicResponse()

  @Serializable
  public data class GetStreamingPublicResponseFailure401(
    public val body: Error,
  ) : GetStreamingPublicResponse()

  @Serializable
  public object GetStreamingPublicResponseFailure410 : GetStreamingPublicResponse()

  @Serializable
  public data class GetStreamingPublicResponseFailure(
    public val body: ValidationError,
  ) : GetStreamingPublicResponse()

  @Serializable
  public data class GetStreamingPublicResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStreamingPublicResponse()

  @Serializable
  public sealed class GetStreamingPublicLocalResponse

  @Serializable
  public object GetStreamingPublicLocalResponseSuccess : GetStreamingPublicLocalResponse()

  @Serializable
  public data class GetStreamingPublicLocalResponseFailure401(
    public val body: Error,
  ) : GetStreamingPublicLocalResponse()

  @Serializable
  public object GetStreamingPublicLocalResponseFailure410 : GetStreamingPublicLocalResponse()

  @Serializable
  public data class GetStreamingPublicLocalResponseFailure(
    public val body: ValidationError,
  ) : GetStreamingPublicLocalResponse()

  @Serializable
  public data class GetStreamingPublicLocalResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStreamingPublicLocalResponse()

  @Serializable
  public sealed class GetStreamingPublicRemoteResponse

  @Serializable
  public object GetStreamingPublicRemoteResponseSuccess : GetStreamingPublicRemoteResponse()

  @Serializable
  public data class GetStreamingPublicRemoteResponseFailure401(
    public val body: Error,
  ) : GetStreamingPublicRemoteResponse()

  @Serializable
  public object GetStreamingPublicRemoteResponseFailure410 : GetStreamingPublicRemoteResponse()

  @Serializable
  public data class GetStreamingPublicRemoteResponseFailure(
    public val body: ValidationError,
  ) : GetStreamingPublicRemoteResponse()

  @Serializable
  public data class GetStreamingPublicRemoteResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStreamingPublicRemoteResponse()

  @Serializable
  public sealed class GetStreamingUserResponse

  @Serializable
  public object GetStreamingUserResponseSuccess : GetStreamingUserResponse()

  @Serializable
  public data class GetStreamingUserResponseFailure401(
    public val body: Error,
  ) : GetStreamingUserResponse()

  @Serializable
  public object GetStreamingUserResponseFailure410 : GetStreamingUserResponse()

  @Serializable
  public data class GetStreamingUserResponseFailure(
    public val body: ValidationError,
  ) : GetStreamingUserResponse()

  @Serializable
  public data class GetStreamingUserResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStreamingUserResponse()

  @Serializable
  public sealed class GetStreamingUserNotificationResponse

  @Serializable
  public object GetStreamingUserNotificationResponseSuccess : GetStreamingUserNotificationResponse()

  @Serializable
  public data class GetStreamingUserNotificationResponseFailure401(
    public val body: Error,
  ) : GetStreamingUserNotificationResponse()

  @Serializable
  public object GetStreamingUserNotificationResponseFailure410 : GetStreamingUserNotificationResponse()

  @Serializable
  public data class GetStreamingUserNotificationResponseFailure(
    public val body: ValidationError,
  ) : GetStreamingUserNotificationResponse()

  @Serializable
  public data class GetStreamingUserNotificationResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStreamingUserNotificationResponse()
}
