package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Conversation
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class ConversationsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View all conversations
   */
  public suspend fun getConversations(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetConversationsResponse {
    try {
      val response = configuration.client.`get`("api/v1/conversations") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (minId != null) {
            parameters.append("min_id", minId)
          }
          if (sinceId != null) {
            parameters.append("since_id", sinceId)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetConversationsResponseSuccess(response.body<List<Conversation>>(), response.headers)
        401, 404, 429, 503 -> GetConversationsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetConversationsResponseFailure410(response.headers)
        422 -> GetConversationsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetConversationsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetConversationsResponseUnknownFailure(500)
    }
  }

  /**
   * Remove a conversation
   */
  public suspend fun deleteConversation(id: String): DeleteConversationResponse {
    try {
      val response = configuration.client.delete("api/v1/conversations/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteConversationResponseSuccess(response.headers)
        401, 404, 429, 503 -> DeleteConversationResponseFailure401(response.body<Error>(), response.headers)
        410 -> DeleteConversationResponseFailure410(response.headers)
        422 -> DeleteConversationResponseFailure(response.body<ValidationError>(), response.headers)
        else -> DeleteConversationResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteConversationResponseUnknownFailure(500)
    }
  }

  /**
   * Mark a conversation as read
   */
  public suspend fun postConversationRead(id: String): PostConversationReadResponse {
    try {
      val response = configuration.client.post("api/v1/conversations/{id}/read".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostConversationReadResponseSuccess(response.body<Conversation>(), response.headers)
        401, 404, 429, 503 -> PostConversationReadResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostConversationReadResponseFailure410(response.headers)
        422 -> PostConversationReadResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostConversationReadResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostConversationReadResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetConversationsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetConversationsResponseSuccess(
    public val body: List<Conversation>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetConversationsResponse() {
    /**
     * Pagination links for browsing older or newer results. Format: Link: <https://mastodon.example/api/v1/endpoint?max_id=7163058>; rel="next", <https://mastodon.example/api/v1/endpoint?min_id=7275607>; rel="prev". See [RFC 8288](https://www.rfc-editor.org/rfc/rfc8288) for more information.
     */
    public val link: String?
      get() = headers["Link"]

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
  public data class GetConversationsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetConversationsResponse()

  @Serializable
  public data class GetConversationsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetConversationsResponse()

  @Serializable
  public data class GetConversationsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetConversationsResponse()

  @Serializable
  public data class GetConversationsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetConversationsResponse()

  @Serializable
  public sealed class DeleteConversationResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteConversationResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteConversationResponse() {
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
  public data class DeleteConversationResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteConversationResponse()

  @Serializable
  public data class DeleteConversationResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteConversationResponse()

  @Serializable
  public data class DeleteConversationResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteConversationResponse()

  @Serializable
  public data class DeleteConversationResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteConversationResponse()

  @Serializable
  public sealed class PostConversationReadResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostConversationReadResponseSuccess(
    public val body: Conversation,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostConversationReadResponse() {
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
  public data class PostConversationReadResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostConversationReadResponse()

  @Serializable
  public data class PostConversationReadResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostConversationReadResponse()

  @Serializable
  public data class PostConversationReadResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostConversationReadResponse()

  @Serializable
  public data class PostConversationReadResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostConversationReadResponse()
}
