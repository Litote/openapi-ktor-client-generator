package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.Context
import mastodon.api.model.CreateStatusRequest
import mastodon.api.model.Error
import mastodon.api.model.Status
import mastodon.api.model.StatusEdit
import mastodon.api.model.StatusSource
import mastodon.api.model.StatusVisibilityEnum
import mastodon.api.model.Translation
import mastodon.api.model.ValidationError
import io.ktor.client.request.`header` as setHeader

public class StatusesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View multiple statuses
   */
  public suspend fun getStatuses(id: List<String>? = null): GetStatusesResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses") {
        url {
          if (id != null) {
            parameters.append("id", id.joinToString(","))
          }
        }
      }
      return when (response.status.value) {
        200 -> GetStatusesResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> GetStatusesResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetStatusesResponseFailure410(response.headers)
        422 -> GetStatusesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetStatusesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusesResponseUnknownFailure(500)
    }
  }

  /**
   * Post a new status
   */
  public suspend fun createStatus(request: CreateStatusRequest, idempotencyKey: JsonElement? = null): CreateStatusResponse {
    try {
      val response = configuration.client.post("api/v1/statuses") {
        if (idempotencyKey != null) {
          setHeader("Idempotency-Key", idempotencyKey)
        }
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateStatusResponseSuccess(response.body<mastodon.api.model.CreateStatusResponse>(), response.headers)
        401, 404, 422, 429, 503 -> CreateStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateStatusResponseFailure(response.headers)
        else -> CreateStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateStatusResponseUnknownFailure(500)
    }
  }

  /**
   * View a single status
   */
  public suspend fun getStatus(id: String): GetStatusResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetStatusResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> GetStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetStatusResponseFailure410(response.headers)
        422 -> GetStatusResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusResponseUnknownFailure(500)
    }
  }

  /**
   * Edit a status
   */
  public suspend fun updateStatus(request: UpdateStatusRequest, id: String): UpdateStatusResponse {
    try {
      val response = configuration.client.put("api/v1/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> UpdateStatusResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 422, 429, 503 -> UpdateStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> UpdateStatusResponseFailure(response.headers)
        else -> UpdateStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return UpdateStatusResponseUnknownFailure(500)
    }
  }

  /**
   * Delete a status
   */
  public suspend fun deleteStatus(id: String, deleteMedia: Boolean? = null): DeleteStatusResponse {
    try {
      val response = configuration.client.delete("api/v1/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        url {
          if (deleteMedia != null) {
            parameters.append("delete_media", deleteMedia.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> DeleteStatusResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> DeleteStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> DeleteStatusResponseFailure410(response.headers)
        422 -> DeleteStatusResponseFailure(response.body<ValidationError>(), response.headers)
        else -> DeleteStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteStatusResponseUnknownFailure(500)
    }
  }

  /**
   * Bookmark a status
   */
  public suspend fun postStatusBookmark(id: String): PostStatusBookmarkResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/bookmark".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostStatusBookmarkResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> PostStatusBookmarkResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusBookmarkResponseFailure410(response.headers)
        422 -> PostStatusBookmarkResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusBookmarkResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusBookmarkResponseUnknownFailure(500)
    }
  }

  /**
   * Get parent and child statuses in context
   */
  public suspend fun getStatusContext(id: String): GetStatusContextResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}/context".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetStatusContextResponseSuccess(response.body<Context>(), response.headers)
        401, 404, 429, 503 -> GetStatusContextResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetStatusContextResponseFailure410(response.headers)
        422 -> GetStatusContextResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetStatusContextResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusContextResponseUnknownFailure(500)
    }
  }

  /**
   * Favourite a status
   */
  public suspend fun postStatusFavourite(id: String): PostStatusFavouriteResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/favourite".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostStatusFavouriteResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> PostStatusFavouriteResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusFavouriteResponseFailure410(response.headers)
        422 -> PostStatusFavouriteResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusFavouriteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusFavouriteResponseUnknownFailure(500)
    }
  }

  /**
   * See who favourited a status
   */
  public suspend fun getStatusFavouritedBy(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetStatusFavouritedByResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}/favourited_by".replace("/{id}", "/${id.encodeURLPathPart()}")) {
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
        200 -> GetStatusFavouritedByResponseSuccess(response.body<List<Account>>(), response.headers)
        401, 404, 429, 503 -> GetStatusFavouritedByResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetStatusFavouritedByResponseFailure410(response.headers)
        422 -> GetStatusFavouritedByResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetStatusFavouritedByResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusFavouritedByResponseUnknownFailure(500)
    }
  }

  /**
   * View edit history of a status
   */
  public suspend fun getStatusHistory(id: String): GetStatusHistoryResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}/history".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetStatusHistoryResponseSuccess(response.body<List<StatusEdit>>(), response.headers)
        401, 404, 429, 503 -> GetStatusHistoryResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetStatusHistoryResponseFailure410(response.headers)
        422 -> GetStatusHistoryResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetStatusHistoryResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusHistoryResponseUnknownFailure(500)
    }
  }

  /**
   * Edit a status' interaction policies
   */
  public suspend fun updateStatusInteractionPolicy(request: UpdateStatusInteractionPolicyRequest, id: String): UpdateStatusInteractionPolicyResponse {
    try {
      val response = configuration.client.put("api/v1/statuses/{id}/interaction_policy".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> UpdateStatusInteractionPolicyResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> UpdateStatusInteractionPolicyResponseFailure401(response.body<Error>(), response.headers)
        410 -> UpdateStatusInteractionPolicyResponseFailure410(response.headers)
        422 -> UpdateStatusInteractionPolicyResponseFailure(response.body<ValidationError>(), response.headers)
        else -> UpdateStatusInteractionPolicyResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return UpdateStatusInteractionPolicyResponseUnknownFailure(500)
    }
  }

  /**
   * Mute a conversation
   */
  public suspend fun postStatusMute(id: String): PostStatusMuteResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/mute".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostStatusMuteResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> PostStatusMuteResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusMuteResponseFailure410(response.headers)
        422 -> PostStatusMuteResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusMuteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusMuteResponseUnknownFailure(500)
    }
  }

  /**
   * Pin status to profile
   */
  public suspend fun postStatusPin(id: String): PostStatusPinResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/pin".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostStatusPinResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 422, 429, 503 -> PostStatusPinResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusPinResponseFailure(response.headers)
        else -> PostStatusPinResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusPinResponseUnknownFailure(500)
    }
  }

  /**
   * See quotes of a status
   */
  public suspend fun getStatusQuotes(
    id: String,
    limit: Long? = 20,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetStatusQuotesResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}/quotes".replace("/{id}", "/${id.encodeURLPathPart()}")) {
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
        200 -> GetStatusQuotesResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> GetStatusQuotesResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetStatusQuotesResponseFailure410(response.headers)
        422 -> GetStatusQuotesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetStatusQuotesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusQuotesResponseUnknownFailure(500)
    }
  }

  /**
   * Revoke a quote post
   */
  public suspend fun postStatusesByIdQuotesByQuotingStatusIdRevoke(id: String, quotingStatusId: String): PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/quotes/{quoting_status_id}/revoke".replace("/{id}", "/${id.encodeURLPathPart()}").replace("/{quoting_status_id}", "/${quotingStatusId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseSuccess(response.body<Status>(), response.headers)
        401, 403, 404, 429, 503 -> PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure410(response.headers)
        422 -> PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseUnknownFailure(500)
    }
  }

  /**
   * Boost a status
   */
  public suspend fun postStatusReblog(request: PostStatusReblogRequest, id: String): PostStatusReblogResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/reblog".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostStatusReblogResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> PostStatusReblogResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusReblogResponseFailure410(response.headers)
        422 -> PostStatusReblogResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusReblogResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusReblogResponseUnknownFailure(500)
    }
  }

  /**
   * See who boosted a status
   */
  public suspend fun getStatusRebloggedBy(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetStatusRebloggedByResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}/reblogged_by".replace("/{id}", "/${id.encodeURLPathPart()}")) {
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
        200 -> GetStatusRebloggedByResponseSuccess(response.body<List<Account>>(), response.headers)
        401, 404, 429, 503 -> GetStatusRebloggedByResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetStatusRebloggedByResponseFailure410(response.headers)
        422 -> GetStatusRebloggedByResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetStatusRebloggedByResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusRebloggedByResponseUnknownFailure(500)
    }
  }

  /**
   * View status source
   */
  public suspend fun getStatusSource(id: String): GetStatusSourceResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}/source".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetStatusSourceResponseSuccess(response.body<StatusSource>(), response.headers)
        401, 404, 429, 503 -> GetStatusSourceResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetStatusSourceResponseFailure410(response.headers)
        422 -> GetStatusSourceResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetStatusSourceResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusSourceResponseUnknownFailure(500)
    }
  }

  /**
   * Translate a status
   */
  public suspend fun postStatusTranslate(request: PostStatusTranslateRequest, id: String): PostStatusTranslateResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/translate".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostStatusTranslateResponseSuccess(response.body<Translation>(), response.headers)
        401, 403, 404, 429, 503 -> PostStatusTranslateResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusTranslateResponseFailure410(response.headers)
        422 -> PostStatusTranslateResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusTranslateResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusTranslateResponseUnknownFailure(500)
    }
  }

  /**
   * Undo bookmark of a status
   */
  public suspend fun postStatusUnbookmark(id: String): PostStatusUnbookmarkResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/unbookmark".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostStatusUnbookmarkResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> PostStatusUnbookmarkResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusUnbookmarkResponseFailure410(response.headers)
        422 -> PostStatusUnbookmarkResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusUnbookmarkResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusUnbookmarkResponseUnknownFailure(500)
    }
  }

  /**
   * Undo favourite of a status
   */
  public suspend fun postStatusUnfavourite(id: String): PostStatusUnfavouriteResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/unfavourite".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostStatusUnfavouriteResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> PostStatusUnfavouriteResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusUnfavouriteResponseFailure410(response.headers)
        422 -> PostStatusUnfavouriteResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusUnfavouriteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusUnfavouriteResponseUnknownFailure(500)
    }
  }

  /**
   * Unmute a conversation
   */
  public suspend fun postStatusUnmute(id: String): PostStatusUnmuteResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/unmute".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostStatusUnmuteResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> PostStatusUnmuteResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusUnmuteResponseFailure410(response.headers)
        422 -> PostStatusUnmuteResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusUnmuteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusUnmuteResponseUnknownFailure(500)
    }
  }

  /**
   * Unpin status from profile
   */
  public suspend fun postStatusUnpin(id: String): PostStatusUnpinResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/unpin".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostStatusUnpinResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> PostStatusUnpinResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusUnpinResponseFailure410(response.headers)
        422 -> PostStatusUnpinResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusUnpinResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusUnpinResponseUnknownFailure(500)
    }
  }

  /**
   * Undo boost of a status
   */
  public suspend fun postStatusUnreblog(id: String): PostStatusUnreblogResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/unreblog".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostStatusUnreblogResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> PostStatusUnreblogResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostStatusUnreblogResponseFailure410(response.headers)
        422 -> PostStatusUnreblogResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostStatusUnreblogResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusUnreblogResponseUnknownFailure(500)
    }
  }

  @Serializable
  public object Id

  @Serializable
  public object IdempotencyKey

  @Serializable
  public sealed class GetStatusesResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetStatusesResponseSuccess(
    public val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusesResponse() {
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
  public data class GetStatusesResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusesResponse()

  @Serializable
  public data class GetStatusesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusesResponse()

  @Serializable
  public data class GetStatusesResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusesResponse()

  @Serializable
  public data class GetStatusesResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusesResponse()

  @Serializable
  public sealed class CreateStatusResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateStatusResponseSuccess(
    public val body: mastodon.api.model.CreateStatusResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateStatusResponse() {
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
  public data class CreateStatusResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateStatusResponse()

  @Serializable
  public data class CreateStatusResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateStatusResponse()

  @Serializable
  public data class CreateStatusResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateStatusResponse()

  @Serializable
  public sealed class GetStatusResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetStatusResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusResponse() {
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
  public data class GetStatusResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusResponse()

  @Serializable
  public data class GetStatusResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusResponse()

  @Serializable
  public data class GetStatusResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusResponse()

  @Serializable
  public data class GetStatusResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusResponse()

  @Serializable
  public data class UpdateStatusRequest(
    public val language: String? = null,
    @SerialName("media_attributes[]")
    public val mediaAttributes: List<String>? = null,
    @SerialName("media_ids")
    public val mediaIds: List<String>? = null,
    public val poll: Poll? = null,
    @SerialName("quote_approval_policy")
    public val quoteApprovalPolicy: String? = null,
    public val sensitive: Boolean? = null,
    @SerialName("spoiler_text")
    public val spoilerText: String? = null,
    public val status: String? = null,
  ) {
    @Serializable
    public data class Poll(
      @SerialName("expires_in")
      public val expiresIn: Long? = null,
      @SerialName("hide_totals")
      public val hideTotals: Boolean? = null,
      public val multiple: Boolean? = null,
      public val options: List<String>? = null,
    )
  }

  @Serializable
  public sealed class UpdateStatusResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class UpdateStatusResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusResponse() {
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
  public data class UpdateStatusResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusResponse()

  @Serializable
  public data class UpdateStatusResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusResponse()

  @Serializable
  public data class UpdateStatusResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusResponse()

  @Serializable
  public sealed class DeleteStatusResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteStatusResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteStatusResponse() {
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
  public data class DeleteStatusResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteStatusResponse()

  @Serializable
  public data class DeleteStatusResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteStatusResponse()

  @Serializable
  public data class DeleteStatusResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteStatusResponse()

  @Serializable
  public data class DeleteStatusResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteStatusResponse()

  @Serializable
  public sealed class PostStatusBookmarkResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusBookmarkResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusBookmarkResponse() {
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
  public data class PostStatusBookmarkResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusBookmarkResponse()

  @Serializable
  public data class PostStatusBookmarkResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusBookmarkResponse()

  @Serializable
  public data class PostStatusBookmarkResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusBookmarkResponse()

  @Serializable
  public data class PostStatusBookmarkResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusBookmarkResponse()

  @Serializable
  public sealed class GetStatusContextResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetStatusContextResponseSuccess(
    public val body: Context,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusContextResponse() {
    /**
     * Indicates an async refresh is in progress. Format: id="<string>", retry=<int>, result_count=<int>. The retry value indicates seconds to wait before retrying. The result_count is optional and indicates results already fetched.
     */
    public val mastodonAsyncRefresh: String?
      get() = headers["Mastodon-Async-Refresh"]

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
  public data class GetStatusContextResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusContextResponse()

  @Serializable
  public data class GetStatusContextResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusContextResponse()

  @Serializable
  public data class GetStatusContextResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusContextResponse()

  @Serializable
  public data class GetStatusContextResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusContextResponse()

  @Serializable
  public sealed class PostStatusFavouriteResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusFavouriteResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusFavouriteResponse() {
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
  public data class PostStatusFavouriteResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusFavouriteResponse()

  @Serializable
  public data class PostStatusFavouriteResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusFavouriteResponse()

  @Serializable
  public data class PostStatusFavouriteResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusFavouriteResponse()

  @Serializable
  public data class PostStatusFavouriteResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusFavouriteResponse()

  @Serializable
  public sealed class GetStatusFavouritedByResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetStatusFavouritedByResponseSuccess(
    public val body: List<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusFavouritedByResponse() {
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
  public data class GetStatusFavouritedByResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusFavouritedByResponse()

  @Serializable
  public data class GetStatusFavouritedByResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusFavouritedByResponse()

  @Serializable
  public data class GetStatusFavouritedByResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusFavouritedByResponse()

  @Serializable
  public data class GetStatusFavouritedByResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusFavouritedByResponse()

  @Serializable
  public sealed class GetStatusHistoryResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetStatusHistoryResponseSuccess(
    public val body: List<StatusEdit>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusHistoryResponse() {
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
  public data class GetStatusHistoryResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusHistoryResponse()

  @Serializable
  public data class GetStatusHistoryResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusHistoryResponse()

  @Serializable
  public data class GetStatusHistoryResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusHistoryResponse()

  @Serializable
  public data class GetStatusHistoryResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusHistoryResponse()

  @Serializable
  public data class UpdateStatusInteractionPolicyRequest(
    @SerialName("quote_approval_policy")
    public val quoteApprovalPolicy: String? = null,
  )

  @Serializable
  public sealed class UpdateStatusInteractionPolicyResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusInteractionPolicyResponse() {
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
  public data class UpdateStatusInteractionPolicyResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusInteractionPolicyResponse()

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusInteractionPolicyResponse()

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusInteractionPolicyResponse()

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusInteractionPolicyResponse()

  @Serializable
  public sealed class PostStatusMuteResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusMuteResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusMuteResponse() {
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
  public data class PostStatusMuteResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusMuteResponse()

  @Serializable
  public data class PostStatusMuteResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusMuteResponse()

  @Serializable
  public data class PostStatusMuteResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusMuteResponse()

  @Serializable
  public data class PostStatusMuteResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusMuteResponse()

  @Serializable
  public sealed class PostStatusPinResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusPinResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusPinResponse() {
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
  public data class PostStatusPinResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusPinResponse()

  @Serializable
  public data class PostStatusPinResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusPinResponse()

  @Serializable
  public data class PostStatusPinResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusPinResponse()

  @Serializable
  public sealed class GetStatusQuotesResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetStatusQuotesResponseSuccess(
    public val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusQuotesResponse() {
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
  public data class GetStatusQuotesResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusQuotesResponse()

  @Serializable
  public data class GetStatusQuotesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusQuotesResponse()

  @Serializable
  public data class GetStatusQuotesResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusQuotesResponse()

  @Serializable
  public data class GetStatusQuotesResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusQuotesResponse()

  @Serializable
  public sealed class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse() {
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
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse()

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse()

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse()

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse()

  @Serializable
  public data class PostStatusReblogRequest(
    public val visibility: StatusVisibilityEnum? = null,
  )

  @Serializable
  public sealed class PostStatusReblogResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusReblogResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusReblogResponse() {
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
  public data class PostStatusReblogResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusReblogResponse()

  @Serializable
  public data class PostStatusReblogResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusReblogResponse()

  @Serializable
  public data class PostStatusReblogResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusReblogResponse()

  @Serializable
  public data class PostStatusReblogResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusReblogResponse()

  @Serializable
  public sealed class GetStatusRebloggedByResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetStatusRebloggedByResponseSuccess(
    public val body: List<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusRebloggedByResponse() {
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
  public data class GetStatusRebloggedByResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusRebloggedByResponse()

  @Serializable
  public data class GetStatusRebloggedByResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusRebloggedByResponse()

  @Serializable
  public data class GetStatusRebloggedByResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusRebloggedByResponse()

  @Serializable
  public data class GetStatusRebloggedByResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusRebloggedByResponse()

  @Serializable
  public sealed class GetStatusSourceResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetStatusSourceResponseSuccess(
    public val body: StatusSource,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusSourceResponse() {
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
  public data class GetStatusSourceResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusSourceResponse()

  @Serializable
  public data class GetStatusSourceResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusSourceResponse()

  @Serializable
  public data class GetStatusSourceResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusSourceResponse()

  @Serializable
  public data class GetStatusSourceResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusSourceResponse()

  @Serializable
  public data class PostStatusTranslateRequest(
    public val lang: String? = null,
  )

  @Serializable
  public sealed class PostStatusTranslateResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusTranslateResponseSuccess(
    public val body: Translation,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusTranslateResponse() {
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
  public data class PostStatusTranslateResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusTranslateResponse()

  @Serializable
  public data class PostStatusTranslateResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusTranslateResponse()

  @Serializable
  public data class PostStatusTranslateResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusTranslateResponse()

  @Serializable
  public data class PostStatusTranslateResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusTranslateResponse()

  @Serializable
  public sealed class PostStatusUnbookmarkResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusUnbookmarkResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnbookmarkResponse() {
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
  public data class PostStatusUnbookmarkResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnbookmarkResponse()

  @Serializable
  public data class PostStatusUnbookmarkResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnbookmarkResponse()

  @Serializable
  public data class PostStatusUnbookmarkResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnbookmarkResponse()

  @Serializable
  public data class PostStatusUnbookmarkResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnbookmarkResponse()

  @Serializable
  public sealed class PostStatusUnfavouriteResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusUnfavouriteResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnfavouriteResponse() {
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
  public data class PostStatusUnfavouriteResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnfavouriteResponse()

  @Serializable
  public data class PostStatusUnfavouriteResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnfavouriteResponse()

  @Serializable
  public data class PostStatusUnfavouriteResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnfavouriteResponse()

  @Serializable
  public data class PostStatusUnfavouriteResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnfavouriteResponse()

  @Serializable
  public sealed class PostStatusUnmuteResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusUnmuteResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnmuteResponse() {
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
  public data class PostStatusUnmuteResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnmuteResponse()

  @Serializable
  public data class PostStatusUnmuteResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnmuteResponse()

  @Serializable
  public data class PostStatusUnmuteResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnmuteResponse()

  @Serializable
  public data class PostStatusUnmuteResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnmuteResponse()

  @Serializable
  public sealed class PostStatusUnpinResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusUnpinResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnpinResponse() {
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
  public data class PostStatusUnpinResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnpinResponse()

  @Serializable
  public data class PostStatusUnpinResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnpinResponse()

  @Serializable
  public data class PostStatusUnpinResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnpinResponse()

  @Serializable
  public data class PostStatusUnpinResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnpinResponse()

  @Serializable
  public sealed class PostStatusUnreblogResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostStatusUnreblogResponseSuccess(
    public val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnreblogResponse() {
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
  public data class PostStatusUnreblogResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnreblogResponse()

  @Serializable
  public data class PostStatusUnreblogResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnreblogResponse()

  @Serializable
  public data class PostStatusUnreblogResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnreblogResponse()

  @Serializable
  public data class PostStatusUnreblogResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnreblogResponse()
}
