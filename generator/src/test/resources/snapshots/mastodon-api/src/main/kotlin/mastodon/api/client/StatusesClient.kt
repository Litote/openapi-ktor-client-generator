package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.Context
import mastodon.api.model.Error
import mastodon.api.model.Status
import mastodon.api.model.StatusEdit
import mastodon.api.model.StatusSource
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
        200 -> GetStatusesResponseSuccess(response.body<List<Status>>())
        401, 404, 429, 503 -> GetStatusesResponseFailure401(response.body<Error>())
        410 -> GetStatusesResponseFailure410
        422 -> GetStatusesResponseFailure(response.body<ValidationError>())
        else -> GetStatusesResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusesResponseUnknownFailure(500)
    }
  }

  /**
   * Post a new status
   */
  public suspend fun createStatus(request: JsonElement, idempotencyKey: JsonElement? = null): CreateStatusResponse {
    try {
      val response = configuration.client.post("api/v1/statuses") {
        if (idempotencyKey != null) {
          setHeader("Idempotency-Key", idempotencyKey)
        }
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateStatusResponseSuccess(response.body<JsonElement>())
        401, 404, 422, 429, 503 -> CreateStatusResponseFailure401(response.body<Error>())
        410 -> CreateStatusResponseFailure
        else -> CreateStatusResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetStatusResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> GetStatusResponseFailure401(response.body<Error>())
        410 -> GetStatusResponseFailure410
        422 -> GetStatusResponseFailure(response.body<ValidationError>())
        else -> GetStatusResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusResponseUnknownFailure(500)
    }
  }

  /**
   * Edit a status
   */
  public suspend fun updateStatus(request: JsonElement, id: String): UpdateStatusResponse {
    try {
      val response = configuration.client.put("api/v1/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> UpdateStatusResponseSuccess(response.body<Status>())
        401, 404, 422, 429, 503 -> UpdateStatusResponseFailure401(response.body<Error>())
        410 -> UpdateStatusResponseFailure
        else -> UpdateStatusResponseUnknownFailure(response.status.value)
      }
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
        200 -> DeleteStatusResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> DeleteStatusResponseFailure401(response.body<Error>())
        410 -> DeleteStatusResponseFailure410
        422 -> DeleteStatusResponseFailure(response.body<ValidationError>())
        else -> DeleteStatusResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostStatusBookmarkResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> PostStatusBookmarkResponseFailure401(response.body<Error>())
        410 -> PostStatusBookmarkResponseFailure410
        422 -> PostStatusBookmarkResponseFailure(response.body<ValidationError>())
        else -> PostStatusBookmarkResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetStatusContextResponseSuccess(response.body<Context>())
        401, 404, 429, 503 -> GetStatusContextResponseFailure401(response.body<Error>())
        410 -> GetStatusContextResponseFailure410
        422 -> GetStatusContextResponseFailure(response.body<ValidationError>())
        else -> GetStatusContextResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostStatusFavouriteResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> PostStatusFavouriteResponseFailure401(response.body<Error>())
        410 -> PostStatusFavouriteResponseFailure410
        422 -> PostStatusFavouriteResponseFailure(response.body<ValidationError>())
        else -> PostStatusFavouriteResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetStatusFavouritedByResponseSuccess(response.body<List<Account>>())
        401, 404, 429, 503 -> GetStatusFavouritedByResponseFailure401(response.body<Error>())
        410 -> GetStatusFavouritedByResponseFailure410
        422 -> GetStatusFavouritedByResponseFailure(response.body<ValidationError>())
        else -> GetStatusFavouritedByResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetStatusHistoryResponseSuccess(response.body<List<StatusEdit>>())
        401, 404, 429, 503 -> GetStatusHistoryResponseFailure401(response.body<Error>())
        410 -> GetStatusHistoryResponseFailure410
        422 -> GetStatusHistoryResponseFailure(response.body<ValidationError>())
        else -> GetStatusHistoryResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusHistoryResponseUnknownFailure(500)
    }
  }

  /**
   * Edit a status' interaction policies
   */
  public suspend fun updateStatusInteractionPolicy(request: JsonElement, id: String): UpdateStatusInteractionPolicyResponse {
    try {
      val response = configuration.client.put("api/v1/statuses/{id}/interaction_policy".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> UpdateStatusInteractionPolicyResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> UpdateStatusInteractionPolicyResponseFailure401(response.body<Error>())
        410 -> UpdateStatusInteractionPolicyResponseFailure410
        422 -> UpdateStatusInteractionPolicyResponseFailure(response.body<ValidationError>())
        else -> UpdateStatusInteractionPolicyResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostStatusMuteResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> PostStatusMuteResponseFailure401(response.body<Error>())
        410 -> PostStatusMuteResponseFailure410
        422 -> PostStatusMuteResponseFailure(response.body<ValidationError>())
        else -> PostStatusMuteResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostStatusPinResponseSuccess(response.body<Status>())
        401, 404, 422, 429, 503 -> PostStatusPinResponseFailure401(response.body<Error>())
        410 -> PostStatusPinResponseFailure
        else -> PostStatusPinResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetStatusQuotesResponseSuccess(response.body<List<Status>>())
        401, 404, 429, 503 -> GetStatusQuotesResponseFailure401(response.body<Error>())
        410 -> GetStatusQuotesResponseFailure410
        422 -> GetStatusQuotesResponseFailure(response.body<ValidationError>())
        else -> GetStatusQuotesResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseSuccess(response.body<Status>())
        401, 403, 404, 429, 503 -> PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure401(response.body<Error>())
        410 -> PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure410
        422 -> PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure(response.body<ValidationError>())
        else -> PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseUnknownFailure(500)
    }
  }

  /**
   * Boost a status
   */
  public suspend fun postStatusReblog(request: JsonElement, id: String): PostStatusReblogResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/reblog".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostStatusReblogResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> PostStatusReblogResponseFailure401(response.body<Error>())
        410 -> PostStatusReblogResponseFailure410
        422 -> PostStatusReblogResponseFailure(response.body<ValidationError>())
        else -> PostStatusReblogResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetStatusRebloggedByResponseSuccess(response.body<List<Account>>())
        401, 404, 429, 503 -> GetStatusRebloggedByResponseFailure401(response.body<Error>())
        410 -> GetStatusRebloggedByResponseFailure410
        422 -> GetStatusRebloggedByResponseFailure(response.body<ValidationError>())
        else -> GetStatusRebloggedByResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetStatusSourceResponseSuccess(response.body<StatusSource>())
        401, 404, 429, 503 -> GetStatusSourceResponseFailure401(response.body<Error>())
        410 -> GetStatusSourceResponseFailure410
        422 -> GetStatusSourceResponseFailure(response.body<ValidationError>())
        else -> GetStatusSourceResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetStatusSourceResponseUnknownFailure(500)
    }
  }

  /**
   * Translate a status
   */
  public suspend fun postStatusTranslate(request: JsonElement, id: String): PostStatusTranslateResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/translate".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostStatusTranslateResponseSuccess(response.body<Translation>())
        401, 403, 404, 429, 503 -> PostStatusTranslateResponseFailure401(response.body<Error>())
        410 -> PostStatusTranslateResponseFailure410
        422 -> PostStatusTranslateResponseFailure(response.body<ValidationError>())
        else -> PostStatusTranslateResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostStatusUnbookmarkResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> PostStatusUnbookmarkResponseFailure401(response.body<Error>())
        410 -> PostStatusUnbookmarkResponseFailure410
        422 -> PostStatusUnbookmarkResponseFailure(response.body<ValidationError>())
        else -> PostStatusUnbookmarkResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostStatusUnfavouriteResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> PostStatusUnfavouriteResponseFailure401(response.body<Error>())
        410 -> PostStatusUnfavouriteResponseFailure410
        422 -> PostStatusUnfavouriteResponseFailure(response.body<ValidationError>())
        else -> PostStatusUnfavouriteResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostStatusUnmuteResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> PostStatusUnmuteResponseFailure401(response.body<Error>())
        410 -> PostStatusUnmuteResponseFailure410
        422 -> PostStatusUnmuteResponseFailure(response.body<ValidationError>())
        else -> PostStatusUnmuteResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostStatusUnpinResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> PostStatusUnpinResponseFailure401(response.body<Error>())
        410 -> PostStatusUnpinResponseFailure410
        422 -> PostStatusUnpinResponseFailure(response.body<ValidationError>())
        else -> PostStatusUnpinResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostStatusUnreblogResponseSuccess(response.body<Status>())
        401, 404, 429, 503 -> PostStatusUnreblogResponseFailure401(response.body<Error>())
        410 -> PostStatusUnreblogResponseFailure410
        422 -> PostStatusUnreblogResponseFailure(response.body<ValidationError>())
        else -> PostStatusUnreblogResponseUnknownFailure(response.status.value)
      }
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
  public sealed class GetStatusesResponse

  @Serializable
  public data class GetStatusesResponseSuccess(
    public val body: List<Status>,
  ) : GetStatusesResponse()

  @Serializable
  public data class GetStatusesResponseFailure401(
    public val body: Error,
  ) : GetStatusesResponse()

  @Serializable
  public object GetStatusesResponseFailure410 : GetStatusesResponse()

  @Serializable
  public data class GetStatusesResponseFailure(
    public val body: ValidationError,
  ) : GetStatusesResponse()

  @Serializable
  public data class GetStatusesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStatusesResponse()

  @Serializable
  public sealed class CreateStatusResponse

  @Serializable
  public data class CreateStatusResponseSuccess(
    public val body: JsonElement,
  ) : CreateStatusResponse()

  @Serializable
  public data class CreateStatusResponseFailure401(
    public val body: Error,
  ) : CreateStatusResponse()

  @Serializable
  public object CreateStatusResponseFailure : CreateStatusResponse()

  @Serializable
  public data class CreateStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateStatusResponse()

  @Serializable
  public sealed class GetStatusResponse

  @Serializable
  public data class GetStatusResponseSuccess(
    public val body: Status,
  ) : GetStatusResponse()

  @Serializable
  public data class GetStatusResponseFailure401(
    public val body: Error,
  ) : GetStatusResponse()

  @Serializable
  public object GetStatusResponseFailure410 : GetStatusResponse()

  @Serializable
  public data class GetStatusResponseFailure(
    public val body: ValidationError,
  ) : GetStatusResponse()

  @Serializable
  public data class GetStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStatusResponse()

  @Serializable
  public sealed class UpdateStatusResponse

  @Serializable
  public data class UpdateStatusResponseSuccess(
    public val body: Status,
  ) : UpdateStatusResponse()

  @Serializable
  public data class UpdateStatusResponseFailure401(
    public val body: Error,
  ) : UpdateStatusResponse()

  @Serializable
  public object UpdateStatusResponseFailure : UpdateStatusResponse()

  @Serializable
  public data class UpdateStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : UpdateStatusResponse()

  @Serializable
  public sealed class DeleteStatusResponse

  @Serializable
  public data class DeleteStatusResponseSuccess(
    public val body: Status,
  ) : DeleteStatusResponse()

  @Serializable
  public data class DeleteStatusResponseFailure401(
    public val body: Error,
  ) : DeleteStatusResponse()

  @Serializable
  public object DeleteStatusResponseFailure410 : DeleteStatusResponse()

  @Serializable
  public data class DeleteStatusResponseFailure(
    public val body: ValidationError,
  ) : DeleteStatusResponse()

  @Serializable
  public data class DeleteStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteStatusResponse()

  @Serializable
  public sealed class PostStatusBookmarkResponse

  @Serializable
  public data class PostStatusBookmarkResponseSuccess(
    public val body: Status,
  ) : PostStatusBookmarkResponse()

  @Serializable
  public data class PostStatusBookmarkResponseFailure401(
    public val body: Error,
  ) : PostStatusBookmarkResponse()

  @Serializable
  public object PostStatusBookmarkResponseFailure410 : PostStatusBookmarkResponse()

  @Serializable
  public data class PostStatusBookmarkResponseFailure(
    public val body: ValidationError,
  ) : PostStatusBookmarkResponse()

  @Serializable
  public data class PostStatusBookmarkResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusBookmarkResponse()

  @Serializable
  public sealed class GetStatusContextResponse

  @Serializable
  public data class GetStatusContextResponseSuccess(
    public val body: Context,
  ) : GetStatusContextResponse()

  @Serializable
  public data class GetStatusContextResponseFailure401(
    public val body: Error,
  ) : GetStatusContextResponse()

  @Serializable
  public object GetStatusContextResponseFailure410 : GetStatusContextResponse()

  @Serializable
  public data class GetStatusContextResponseFailure(
    public val body: ValidationError,
  ) : GetStatusContextResponse()

  @Serializable
  public data class GetStatusContextResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStatusContextResponse()

  @Serializable
  public sealed class PostStatusFavouriteResponse

  @Serializable
  public data class PostStatusFavouriteResponseSuccess(
    public val body: Status,
  ) : PostStatusFavouriteResponse()

  @Serializable
  public data class PostStatusFavouriteResponseFailure401(
    public val body: Error,
  ) : PostStatusFavouriteResponse()

  @Serializable
  public object PostStatusFavouriteResponseFailure410 : PostStatusFavouriteResponse()

  @Serializable
  public data class PostStatusFavouriteResponseFailure(
    public val body: ValidationError,
  ) : PostStatusFavouriteResponse()

  @Serializable
  public data class PostStatusFavouriteResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusFavouriteResponse()

  @Serializable
  public sealed class GetStatusFavouritedByResponse

  @Serializable
  public data class GetStatusFavouritedByResponseSuccess(
    public val body: List<Account>,
  ) : GetStatusFavouritedByResponse()

  @Serializable
  public data class GetStatusFavouritedByResponseFailure401(
    public val body: Error,
  ) : GetStatusFavouritedByResponse()

  @Serializable
  public object GetStatusFavouritedByResponseFailure410 : GetStatusFavouritedByResponse()

  @Serializable
  public data class GetStatusFavouritedByResponseFailure(
    public val body: ValidationError,
  ) : GetStatusFavouritedByResponse()

  @Serializable
  public data class GetStatusFavouritedByResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStatusFavouritedByResponse()

  @Serializable
  public sealed class GetStatusHistoryResponse

  @Serializable
  public data class GetStatusHistoryResponseSuccess(
    public val body: List<StatusEdit>,
  ) : GetStatusHistoryResponse()

  @Serializable
  public data class GetStatusHistoryResponseFailure401(
    public val body: Error,
  ) : GetStatusHistoryResponse()

  @Serializable
  public object GetStatusHistoryResponseFailure410 : GetStatusHistoryResponse()

  @Serializable
  public data class GetStatusHistoryResponseFailure(
    public val body: ValidationError,
  ) : GetStatusHistoryResponse()

  @Serializable
  public data class GetStatusHistoryResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStatusHistoryResponse()

  @Serializable
  public sealed class UpdateStatusInteractionPolicyResponse

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseSuccess(
    public val body: Status,
  ) : UpdateStatusInteractionPolicyResponse()

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseFailure401(
    public val body: Error,
  ) : UpdateStatusInteractionPolicyResponse()

  @Serializable
  public object UpdateStatusInteractionPolicyResponseFailure410 : UpdateStatusInteractionPolicyResponse()

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseFailure(
    public val body: ValidationError,
  ) : UpdateStatusInteractionPolicyResponse()

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseUnknownFailure(
    public val statusCode: Int,
  ) : UpdateStatusInteractionPolicyResponse()

  @Serializable
  public sealed class PostStatusMuteResponse

  @Serializable
  public data class PostStatusMuteResponseSuccess(
    public val body: Status,
  ) : PostStatusMuteResponse()

  @Serializable
  public data class PostStatusMuteResponseFailure401(
    public val body: Error,
  ) : PostStatusMuteResponse()

  @Serializable
  public object PostStatusMuteResponseFailure410 : PostStatusMuteResponse()

  @Serializable
  public data class PostStatusMuteResponseFailure(
    public val body: ValidationError,
  ) : PostStatusMuteResponse()

  @Serializable
  public data class PostStatusMuteResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusMuteResponse()

  @Serializable
  public sealed class PostStatusPinResponse

  @Serializable
  public data class PostStatusPinResponseSuccess(
    public val body: Status,
  ) : PostStatusPinResponse()

  @Serializable
  public data class PostStatusPinResponseFailure401(
    public val body: Error,
  ) : PostStatusPinResponse()

  @Serializable
  public object PostStatusPinResponseFailure : PostStatusPinResponse()

  @Serializable
  public data class PostStatusPinResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusPinResponse()

  @Serializable
  public sealed class GetStatusQuotesResponse

  @Serializable
  public data class GetStatusQuotesResponseSuccess(
    public val body: List<Status>,
  ) : GetStatusQuotesResponse()

  @Serializable
  public data class GetStatusQuotesResponseFailure401(
    public val body: Error,
  ) : GetStatusQuotesResponse()

  @Serializable
  public object GetStatusQuotesResponseFailure410 : GetStatusQuotesResponse()

  @Serializable
  public data class GetStatusQuotesResponseFailure(
    public val body: ValidationError,
  ) : GetStatusQuotesResponse()

  @Serializable
  public data class GetStatusQuotesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStatusQuotesResponse()

  @Serializable
  public sealed class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseSuccess(
    public val body: Status,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse()

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure401(
    public val body: Error,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse()

  @Serializable
  public object PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure410 : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse()

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure(
    public val body: ValidationError,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse()

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse()

  @Serializable
  public sealed class PostStatusReblogResponse

  @Serializable
  public data class PostStatusReblogResponseSuccess(
    public val body: Status,
  ) : PostStatusReblogResponse()

  @Serializable
  public data class PostStatusReblogResponseFailure401(
    public val body: Error,
  ) : PostStatusReblogResponse()

  @Serializable
  public object PostStatusReblogResponseFailure410 : PostStatusReblogResponse()

  @Serializable
  public data class PostStatusReblogResponseFailure(
    public val body: ValidationError,
  ) : PostStatusReblogResponse()

  @Serializable
  public data class PostStatusReblogResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusReblogResponse()

  @Serializable
  public sealed class GetStatusRebloggedByResponse

  @Serializable
  public data class GetStatusRebloggedByResponseSuccess(
    public val body: List<Account>,
  ) : GetStatusRebloggedByResponse()

  @Serializable
  public data class GetStatusRebloggedByResponseFailure401(
    public val body: Error,
  ) : GetStatusRebloggedByResponse()

  @Serializable
  public object GetStatusRebloggedByResponseFailure410 : GetStatusRebloggedByResponse()

  @Serializable
  public data class GetStatusRebloggedByResponseFailure(
    public val body: ValidationError,
  ) : GetStatusRebloggedByResponse()

  @Serializable
  public data class GetStatusRebloggedByResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStatusRebloggedByResponse()

  @Serializable
  public sealed class GetStatusSourceResponse

  @Serializable
  public data class GetStatusSourceResponseSuccess(
    public val body: StatusSource,
  ) : GetStatusSourceResponse()

  @Serializable
  public data class GetStatusSourceResponseFailure401(
    public val body: Error,
  ) : GetStatusSourceResponse()

  @Serializable
  public object GetStatusSourceResponseFailure410 : GetStatusSourceResponse()

  @Serializable
  public data class GetStatusSourceResponseFailure(
    public val body: ValidationError,
  ) : GetStatusSourceResponse()

  @Serializable
  public data class GetStatusSourceResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetStatusSourceResponse()

  @Serializable
  public sealed class PostStatusTranslateResponse

  @Serializable
  public data class PostStatusTranslateResponseSuccess(
    public val body: Translation,
  ) : PostStatusTranslateResponse()

  @Serializable
  public data class PostStatusTranslateResponseFailure401(
    public val body: Error,
  ) : PostStatusTranslateResponse()

  @Serializable
  public object PostStatusTranslateResponseFailure410 : PostStatusTranslateResponse()

  @Serializable
  public data class PostStatusTranslateResponseFailure(
    public val body: ValidationError,
  ) : PostStatusTranslateResponse()

  @Serializable
  public data class PostStatusTranslateResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusTranslateResponse()

  @Serializable
  public sealed class PostStatusUnbookmarkResponse

  @Serializable
  public data class PostStatusUnbookmarkResponseSuccess(
    public val body: Status,
  ) : PostStatusUnbookmarkResponse()

  @Serializable
  public data class PostStatusUnbookmarkResponseFailure401(
    public val body: Error,
  ) : PostStatusUnbookmarkResponse()

  @Serializable
  public object PostStatusUnbookmarkResponseFailure410 : PostStatusUnbookmarkResponse()

  @Serializable
  public data class PostStatusUnbookmarkResponseFailure(
    public val body: ValidationError,
  ) : PostStatusUnbookmarkResponse()

  @Serializable
  public data class PostStatusUnbookmarkResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusUnbookmarkResponse()

  @Serializable
  public sealed class PostStatusUnfavouriteResponse

  @Serializable
  public data class PostStatusUnfavouriteResponseSuccess(
    public val body: Status,
  ) : PostStatusUnfavouriteResponse()

  @Serializable
  public data class PostStatusUnfavouriteResponseFailure401(
    public val body: Error,
  ) : PostStatusUnfavouriteResponse()

  @Serializable
  public object PostStatusUnfavouriteResponseFailure410 : PostStatusUnfavouriteResponse()

  @Serializable
  public data class PostStatusUnfavouriteResponseFailure(
    public val body: ValidationError,
  ) : PostStatusUnfavouriteResponse()

  @Serializable
  public data class PostStatusUnfavouriteResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusUnfavouriteResponse()

  @Serializable
  public sealed class PostStatusUnmuteResponse

  @Serializable
  public data class PostStatusUnmuteResponseSuccess(
    public val body: Status,
  ) : PostStatusUnmuteResponse()

  @Serializable
  public data class PostStatusUnmuteResponseFailure401(
    public val body: Error,
  ) : PostStatusUnmuteResponse()

  @Serializable
  public object PostStatusUnmuteResponseFailure410 : PostStatusUnmuteResponse()

  @Serializable
  public data class PostStatusUnmuteResponseFailure(
    public val body: ValidationError,
  ) : PostStatusUnmuteResponse()

  @Serializable
  public data class PostStatusUnmuteResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusUnmuteResponse()

  @Serializable
  public sealed class PostStatusUnpinResponse

  @Serializable
  public data class PostStatusUnpinResponseSuccess(
    public val body: Status,
  ) : PostStatusUnpinResponse()

  @Serializable
  public data class PostStatusUnpinResponseFailure401(
    public val body: Error,
  ) : PostStatusUnpinResponse()

  @Serializable
  public object PostStatusUnpinResponseFailure410 : PostStatusUnpinResponse()

  @Serializable
  public data class PostStatusUnpinResponseFailure(
    public val body: ValidationError,
  ) : PostStatusUnpinResponse()

  @Serializable
  public data class PostStatusUnpinResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusUnpinResponse()

  @Serializable
  public sealed class PostStatusUnreblogResponse

  @Serializable
  public data class PostStatusUnreblogResponseSuccess(
    public val body: Status,
  ) : PostStatusUnreblogResponse()

  @Serializable
  public data class PostStatusUnreblogResponseFailure401(
    public val body: Error,
  ) : PostStatusUnreblogResponse()

  @Serializable
  public object PostStatusUnreblogResponseFailure410 : PostStatusUnreblogResponse()

  @Serializable
  public data class PostStatusUnreblogResponseFailure(
    public val body: ValidationError,
  ) : PostStatusUnreblogResponse()

  @Serializable
  public data class PostStatusUnreblogResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostStatusUnreblogResponse()
}
