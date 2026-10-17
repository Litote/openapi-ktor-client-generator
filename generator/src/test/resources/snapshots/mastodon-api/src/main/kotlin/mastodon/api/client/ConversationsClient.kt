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
import kotlin.Unit
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Conversation
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public interface ConversationsClient {
  /**
   * View all conversations
   */
  public suspend fun getConversations(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetConversationsResponse

  /**
   * Remove a conversation
   */
  public suspend fun deleteConversation(id: String): DeleteConversationResponse

  /**
   * Mark a conversation as read
   */
  public suspend fun postConversationRead(id: String): PostConversationReadResponse

  @Serializable
  public sealed class GetConversationsResponse : OperationResponse<List<Conversation>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetConversationsResponseSuccess(
    override val body: List<Conversation>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetConversationsResponse(),
      OperationResponseSuccess<List<Conversation>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetConversationsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetConversationsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetConversationsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetConversationsResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetConversationsResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetConversationsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetConversationsResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class DeleteConversationResponse : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class DeleteConversationResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteConversationResponse(),
      OperationResponseSuccess<Unit> {
    override val body: Unit
      get() = Unit

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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteConversationResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class DeleteConversationResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteConversationResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class DeleteConversationResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteConversationResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class DeleteConversationResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteConversationResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostConversationReadResponse : OperationResponse<Conversation> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostConversationReadResponseSuccess(
    override val body: Conversation,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostConversationReadResponse(),
      OperationResponseSuccess<Conversation> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostConversationReadResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostConversationReadResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostConversationReadResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostConversationReadResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostConversationReadResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostConversationReadResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostConversationReadResponse(),
      OperationResponseUnknownFailure
}

public fun ConversationsClient(configuration: ClientConfiguration = defaultClientConfiguration): ConversationsClient = DefaultConversationsClient(configuration)

public class DefaultConversationsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : ConversationsClient {
  override suspend fun getConversations(
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): ConversationsClient.GetConversationsResponse {
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
        200 -> ConversationsClient.GetConversationsResponseSuccess(response.body<List<Conversation>>(), response.headers)
        401, 404, 429, 503 -> ConversationsClient.GetConversationsResponseFailure401(response.body<Error>(), response.headers)
        410 -> ConversationsClient.GetConversationsResponseFailure410(response.headers)
        422 -> ConversationsClient.GetConversationsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> ConversationsClient.GetConversationsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return ConversationsClient.GetConversationsResponseUnknownFailure(500)
    }
  }

  override suspend fun deleteConversation(id: String): ConversationsClient.DeleteConversationResponse {
    try {
      val response = configuration.client.delete("api/v1/conversations/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> ConversationsClient.DeleteConversationResponseSuccess(response.headers)
        401, 404, 429, 503 -> ConversationsClient.DeleteConversationResponseFailure401(response.body<Error>(), response.headers)
        410 -> ConversationsClient.DeleteConversationResponseFailure410(response.headers)
        422 -> ConversationsClient.DeleteConversationResponseFailure(response.body<ValidationError>(), response.headers)
        else -> ConversationsClient.DeleteConversationResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return ConversationsClient.DeleteConversationResponseUnknownFailure(500)
    }
  }

  override suspend fun postConversationRead(id: String): ConversationsClient.PostConversationReadResponse {
    try {
      val response = configuration.client.post("api/v1/conversations/{id}/read".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> ConversationsClient.PostConversationReadResponseSuccess(response.body<Conversation>(), response.headers)
        401, 404, 429, 503 -> ConversationsClient.PostConversationReadResponseFailure401(response.body<Error>(), response.headers)
        410 -> ConversationsClient.PostConversationReadResponseFailure410(response.headers)
        422 -> ConversationsClient.PostConversationReadResponseFailure(response.body<ValidationError>(), response.headers)
        else -> ConversationsClient.PostConversationReadResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return ConversationsClient.PostConversationReadResponseUnknownFailure(500)
    }
  }
}
