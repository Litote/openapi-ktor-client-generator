package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
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
        200 -> GetConversationsResponseSuccess(response.body<List<Conversation>>())
        401, 404, 429, 503 -> GetConversationsResponseFailure401(response.body<Error>())
        410 -> GetConversationsResponseFailure410
        422 -> GetConversationsResponseFailure(response.body<ValidationError>())
        else -> GetConversationsResponseUnknownFailure(response.status.value)
      }
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
        200 -> DeleteConversationResponseSuccess
        401, 404, 429, 503 -> DeleteConversationResponseFailure401(response.body<Error>())
        410 -> DeleteConversationResponseFailure410
        422 -> DeleteConversationResponseFailure(response.body<ValidationError>())
        else -> DeleteConversationResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostConversationReadResponseSuccess(response.body<Conversation>())
        401, 404, 429, 503 -> PostConversationReadResponseFailure401(response.body<Error>())
        410 -> PostConversationReadResponseFailure410
        422 -> PostConversationReadResponseFailure(response.body<ValidationError>())
        else -> PostConversationReadResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostConversationReadResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetConversationsResponse

  @Serializable
  public data class GetConversationsResponseSuccess(
    public val body: List<Conversation>,
  ) : GetConversationsResponse()

  @Serializable
  public data class GetConversationsResponseFailure401(
    public val body: Error,
  ) : GetConversationsResponse()

  @Serializable
  public object GetConversationsResponseFailure410 : GetConversationsResponse()

  @Serializable
  public data class GetConversationsResponseFailure(
    public val body: ValidationError,
  ) : GetConversationsResponse()

  @Serializable
  public data class GetConversationsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetConversationsResponse()

  @Serializable
  public sealed class DeleteConversationResponse

  @Serializable
  public object DeleteConversationResponseSuccess : DeleteConversationResponse()

  @Serializable
  public data class DeleteConversationResponseFailure401(
    public val body: Error,
  ) : DeleteConversationResponse()

  @Serializable
  public object DeleteConversationResponseFailure410 : DeleteConversationResponse()

  @Serializable
  public data class DeleteConversationResponseFailure(
    public val body: ValidationError,
  ) : DeleteConversationResponse()

  @Serializable
  public data class DeleteConversationResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteConversationResponse()

  @Serializable
  public sealed class PostConversationReadResponse

  @Serializable
  public data class PostConversationReadResponseSuccess(
    public val body: Conversation,
  ) : PostConversationReadResponse()

  @Serializable
  public data class PostConversationReadResponseFailure401(
    public val body: Error,
  ) : PostConversationReadResponse()

  @Serializable
  public object PostConversationReadResponseFailure410 : PostConversationReadResponse()

  @Serializable
  public data class PostConversationReadResponseFailure(
    public val body: ValidationError,
  ) : PostConversationReadResponse()

  @Serializable
  public data class PostConversationReadResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostConversationReadResponse()
}
