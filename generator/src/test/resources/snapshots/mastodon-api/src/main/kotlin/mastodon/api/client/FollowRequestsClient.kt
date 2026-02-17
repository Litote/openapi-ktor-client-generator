package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.Error
import mastodon.api.model.Relationship
import mastodon.api.model.ValidationError

public class FollowRequestsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View pending follow requests
   */
  public suspend fun getFollowRequests(
    limit: Long? = 40,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetFollowRequestsResponse {
    try {
      val response = configuration.client.`get`("api/v1/follow_requests") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (sinceId != null) {
            parameters.append("since_id", sinceId)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetFollowRequestsResponseSuccess(response.body<List<Account>>())
        401, 404, 429, 503 -> GetFollowRequestsResponseFailure401(response.body<Error>())
        410 -> GetFollowRequestsResponseFailure410
        422 -> GetFollowRequestsResponseFailure(response.body<ValidationError>())
        else -> GetFollowRequestsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFollowRequestsResponseUnknownFailure(500)
    }
  }

  /**
   * Accept follow request
   */
  public suspend fun postFollowRequestAuthorize(accountId: String): PostFollowRequestAuthorizeResponse {
    try {
      val response = configuration.client.post("api/v1/follow_requests/{account_id}/authorize".replace("/{account_id}", "/${accountId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostFollowRequestAuthorizeResponseSuccess(response.body<Relationship>())
        401, 404, 429, 503 -> PostFollowRequestAuthorizeResponseFailure401(response.body<Error>())
        410 -> PostFollowRequestAuthorizeResponseFailure410
        422 -> PostFollowRequestAuthorizeResponseFailure(response.body<ValidationError>())
        else -> PostFollowRequestAuthorizeResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostFollowRequestAuthorizeResponseUnknownFailure(500)
    }
  }

  /**
   * Reject follow request
   */
  public suspend fun postFollowRequestReject(accountId: String): PostFollowRequestRejectResponse {
    try {
      val response = configuration.client.post("api/v1/follow_requests/{account_id}/reject".replace("/{account_id}", "/${accountId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostFollowRequestRejectResponseSuccess(response.body<Relationship>())
        401, 404, 429, 503 -> PostFollowRequestRejectResponseFailure401(response.body<Error>())
        410 -> PostFollowRequestRejectResponseFailure410
        422 -> PostFollowRequestRejectResponseFailure(response.body<ValidationError>())
        else -> PostFollowRequestRejectResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostFollowRequestRejectResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetFollowRequestsResponse

  @Serializable
  public data class GetFollowRequestsResponseSuccess(
    public val body: List<Account>,
  ) : GetFollowRequestsResponse()

  @Serializable
  public data class GetFollowRequestsResponseFailure401(
    public val body: Error,
  ) : GetFollowRequestsResponse()

  @Serializable
  public object GetFollowRequestsResponseFailure410 : GetFollowRequestsResponse()

  @Serializable
  public data class GetFollowRequestsResponseFailure(
    public val body: ValidationError,
  ) : GetFollowRequestsResponse()

  @Serializable
  public data class GetFollowRequestsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFollowRequestsResponse()

  @Serializable
  public sealed class PostFollowRequestAuthorizeResponse

  @Serializable
  public data class PostFollowRequestAuthorizeResponseSuccess(
    public val body: Relationship,
  ) : PostFollowRequestAuthorizeResponse()

  @Serializable
  public data class PostFollowRequestAuthorizeResponseFailure401(
    public val body: Error,
  ) : PostFollowRequestAuthorizeResponse()

  @Serializable
  public object PostFollowRequestAuthorizeResponseFailure410 : PostFollowRequestAuthorizeResponse()

  @Serializable
  public data class PostFollowRequestAuthorizeResponseFailure(
    public val body: ValidationError,
  ) : PostFollowRequestAuthorizeResponse()

  @Serializable
  public data class PostFollowRequestAuthorizeResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostFollowRequestAuthorizeResponse()

  @Serializable
  public sealed class PostFollowRequestRejectResponse

  @Serializable
  public data class PostFollowRequestRejectResponseSuccess(
    public val body: Relationship,
  ) : PostFollowRequestRejectResponse()

  @Serializable
  public data class PostFollowRequestRejectResponseFailure401(
    public val body: Error,
  ) : PostFollowRequestRejectResponse()

  @Serializable
  public object PostFollowRequestRejectResponseFailure410 : PostFollowRequestRejectResponse()

  @Serializable
  public data class PostFollowRequestRejectResponseFailure(
    public val body: ValidationError,
  ) : PostFollowRequestRejectResponse()

  @Serializable
  public data class PostFollowRequestRejectResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostFollowRequestRejectResponse()
}
