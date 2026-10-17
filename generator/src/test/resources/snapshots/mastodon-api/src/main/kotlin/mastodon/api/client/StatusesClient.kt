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
import kotlin.Unit
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
import mastodon.api.model.CreateStatusResponse
import mastodon.api.model.Error
import mastodon.api.model.Status
import mastodon.api.model.StatusEdit
import mastodon.api.model.StatusSource
import mastodon.api.model.StatusVisibilityEnum
import mastodon.api.model.Translation
import mastodon.api.model.ValidationError
import io.ktor.client.request.`header` as setHeader

public interface StatusesClient {
  /**
   * View multiple statuses
   */
  public suspend fun getStatuses(id: List<String>? = null): GetStatusesResponse

  /**
   * Post a new status
   */
  public suspend fun createStatus(request: CreateStatusRequest, idempotencyKey: JsonElement? = null): CreateStatusResponse

  /**
   * View a single status
   */
  public suspend fun getStatus(id: String): GetStatusResponse

  /**
   * Edit a status
   */
  public suspend fun updateStatus(request: UpdateStatusRequest, id: String): UpdateStatusResponse

  /**
   * Delete a status
   */
  public suspend fun deleteStatus(id: String, deleteMedia: Boolean? = null): DeleteStatusResponse

  /**
   * Bookmark a status
   */
  public suspend fun postStatusBookmark(id: String): PostStatusBookmarkResponse

  /**
   * Get parent and child statuses in context
   */
  public suspend fun getStatusContext(id: String): GetStatusContextResponse

  /**
   * Favourite a status
   */
  public suspend fun postStatusFavourite(id: String): PostStatusFavouriteResponse

  /**
   * See who favourited a status
   */
  public suspend fun getStatusFavouritedBy(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetStatusFavouritedByResponse

  /**
   * View edit history of a status
   */
  public suspend fun getStatusHistory(id: String): GetStatusHistoryResponse

  /**
   * Edit a status' interaction policies
   */
  public suspend fun updateStatusInteractionPolicy(request: UpdateStatusInteractionPolicyRequest, id: String): UpdateStatusInteractionPolicyResponse

  /**
   * Mute a conversation
   */
  public suspend fun postStatusMute(id: String): PostStatusMuteResponse

  /**
   * Pin status to profile
   */
  public suspend fun postStatusPin(id: String): PostStatusPinResponse

  /**
   * See quotes of a status
   */
  public suspend fun getStatusQuotes(
    id: String,
    limit: Long? = 20,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetStatusQuotesResponse

  /**
   * Revoke a quote post
   */
  public suspend fun postStatusesByIdQuotesByQuotingStatusIdRevoke(id: String, quotingStatusId: String): PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse

  /**
   * Boost a status
   */
  public suspend fun postStatusReblog(request: PostStatusReblogRequest, id: String): PostStatusReblogResponse

  /**
   * See who boosted a status
   */
  public suspend fun getStatusRebloggedBy(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetStatusRebloggedByResponse

  /**
   * View status source
   */
  public suspend fun getStatusSource(id: String): GetStatusSourceResponse

  /**
   * Translate a status
   */
  public suspend fun postStatusTranslate(request: PostStatusTranslateRequest, id: String): PostStatusTranslateResponse

  /**
   * Undo bookmark of a status
   */
  public suspend fun postStatusUnbookmark(id: String): PostStatusUnbookmarkResponse

  /**
   * Undo favourite of a status
   */
  public suspend fun postStatusUnfavourite(id: String): PostStatusUnfavouriteResponse

  /**
   * Unmute a conversation
   */
  public suspend fun postStatusUnmute(id: String): PostStatusUnmuteResponse

  /**
   * Unpin status from profile
   */
  public suspend fun postStatusUnpin(id: String): PostStatusUnpinResponse

  /**
   * Undo boost of a status
   */
  public suspend fun postStatusUnreblog(id: String): PostStatusUnreblogResponse

  @Serializable
  public sealed class GetStatusesResponse : OperationResponse<List<Status>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetStatusesResponseSuccess(
    override val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusesResponse(),
      OperationResponseSuccess<List<Status>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusesResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetStatusesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusesResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetStatusesResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusesResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetStatusesResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusesResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class CreateStatusResponse : OperationResponse<mastodon.api.model.CreateStatusResponse> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class CreateStatusResponseSuccess(
    override val body: mastodon.api.model.CreateStatusResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateStatusResponse(),
      OperationResponseSuccess<mastodon.api.model.CreateStatusResponse> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateStatusResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class CreateStatusResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateStatusResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class CreateStatusResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateStatusResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetStatusResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetStatusResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetStatusResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetStatusResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetStatusResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusResponse(),
      OperationResponseUnknownFailure

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
  public sealed class UpdateStatusResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class UpdateStatusResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class UpdateStatusResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class UpdateStatusResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class DeleteStatusResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class DeleteStatusResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteStatusResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteStatusResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class DeleteStatusResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteStatusResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class DeleteStatusResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteStatusResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class DeleteStatusResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteStatusResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostStatusBookmarkResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusBookmarkResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusBookmarkResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusBookmarkResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusBookmarkResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusBookmarkResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusBookmarkResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusBookmarkResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusBookmarkResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusBookmarkResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetStatusContextResponse : OperationResponse<Context> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetStatusContextResponseSuccess(
    override val body: Context,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusContextResponse(),
      OperationResponseSuccess<Context> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusContextResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetStatusContextResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusContextResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetStatusContextResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusContextResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetStatusContextResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusContextResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostStatusFavouriteResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusFavouriteResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusFavouriteResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusFavouriteResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusFavouriteResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusFavouriteResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusFavouriteResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusFavouriteResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusFavouriteResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusFavouriteResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetStatusFavouritedByResponse : OperationResponse<List<Account>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetStatusFavouritedByResponseSuccess(
    override val body: List<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusFavouritedByResponse(),
      OperationResponseSuccess<List<Account>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusFavouritedByResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetStatusFavouritedByResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusFavouritedByResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetStatusFavouritedByResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusFavouritedByResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetStatusFavouritedByResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusFavouritedByResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetStatusHistoryResponse : OperationResponse<List<StatusEdit>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetStatusHistoryResponseSuccess(
    override val body: List<StatusEdit>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusHistoryResponse(),
      OperationResponseSuccess<List<StatusEdit>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusHistoryResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetStatusHistoryResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusHistoryResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetStatusHistoryResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusHistoryResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetStatusHistoryResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusHistoryResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public data class UpdateStatusInteractionPolicyRequest(
    @SerialName("quote_approval_policy")
    public val quoteApprovalPolicy: String? = null,
  )

  @Serializable
  public sealed class UpdateStatusInteractionPolicyResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusInteractionPolicyResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusInteractionPolicyResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusInteractionPolicyResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusInteractionPolicyResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class UpdateStatusInteractionPolicyResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateStatusInteractionPolicyResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostStatusMuteResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusMuteResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusMuteResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusMuteResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusMuteResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusMuteResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusMuteResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusMuteResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusMuteResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusMuteResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostStatusPinResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusPinResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusPinResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusPinResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusPinResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusPinResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusPinResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusPinResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetStatusQuotesResponse : OperationResponse<List<Status>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetStatusQuotesResponseSuccess(
    override val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusQuotesResponse(),
      OperationResponseSuccess<List<Status>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusQuotesResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetStatusQuotesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusQuotesResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetStatusQuotesResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusQuotesResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetStatusQuotesResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusQuotesResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public data class PostStatusReblogRequest(
    public val visibility: StatusVisibilityEnum? = null,
  )

  @Serializable
  public sealed class PostStatusReblogResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusReblogResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusReblogResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusReblogResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusReblogResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusReblogResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusReblogResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusReblogResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusReblogResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusReblogResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetStatusRebloggedByResponse : OperationResponse<List<Account>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetStatusRebloggedByResponseSuccess(
    override val body: List<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusRebloggedByResponse(),
      OperationResponseSuccess<List<Account>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusRebloggedByResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetStatusRebloggedByResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusRebloggedByResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetStatusRebloggedByResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusRebloggedByResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetStatusRebloggedByResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusRebloggedByResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetStatusSourceResponse : OperationResponse<StatusSource> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetStatusSourceResponseSuccess(
    override val body: StatusSource,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusSourceResponse(),
      OperationResponseSuccess<StatusSource> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusSourceResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetStatusSourceResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusSourceResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetStatusSourceResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusSourceResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetStatusSourceResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetStatusSourceResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public data class PostStatusTranslateRequest(
    public val lang: String? = null,
  )

  @Serializable
  public sealed class PostStatusTranslateResponse : OperationResponse<Translation> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusTranslateResponseSuccess(
    override val body: Translation,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusTranslateResponse(),
      OperationResponseSuccess<Translation> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusTranslateResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusTranslateResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusTranslateResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusTranslateResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusTranslateResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusTranslateResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusTranslateResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostStatusUnbookmarkResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusUnbookmarkResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnbookmarkResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnbookmarkResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusUnbookmarkResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnbookmarkResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusUnbookmarkResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnbookmarkResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusUnbookmarkResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnbookmarkResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostStatusUnfavouriteResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusUnfavouriteResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnfavouriteResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnfavouriteResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusUnfavouriteResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnfavouriteResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusUnfavouriteResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnfavouriteResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusUnfavouriteResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnfavouriteResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostStatusUnmuteResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusUnmuteResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnmuteResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnmuteResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusUnmuteResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnmuteResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusUnmuteResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnmuteResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusUnmuteResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnmuteResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostStatusUnpinResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusUnpinResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnpinResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnpinResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusUnpinResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnpinResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusUnpinResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnpinResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusUnpinResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnpinResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostStatusUnreblogResponse : OperationResponse<Status> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostStatusUnreblogResponseSuccess(
    override val body: Status,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnreblogResponse(),
      OperationResponseSuccess<Status> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnreblogResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostStatusUnreblogResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnreblogResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostStatusUnreblogResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnreblogResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostStatusUnreblogResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostStatusUnreblogResponse(),
      OperationResponseUnknownFailure
}

public fun StatusesClient(configuration: ClientConfiguration = defaultClientConfiguration): StatusesClient = DefaultStatusesClient(configuration)

public class DefaultStatusesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : StatusesClient {
  override suspend fun getStatuses(id: List<String>?): StatusesClient.GetStatusesResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses") {
        url {
          if (id != null) {
            parameters.appendAll("id", id)
          }
        }
      }
      return when (response.status.value) {
        200 -> StatusesClient.GetStatusesResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.GetStatusesResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.GetStatusesResponseFailure410(response.headers)
        422 -> StatusesClient.GetStatusesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.GetStatusesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.GetStatusesResponseUnknownFailure(500)
    }
  }

  override suspend fun createStatus(request: CreateStatusRequest, idempotencyKey: JsonElement?): StatusesClient.CreateStatusResponse {
    try {
      val response = configuration.client.post("api/v1/statuses") {
        if (idempotencyKey != null) {
          setHeader("Idempotency-Key", idempotencyKey)
        }
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> StatusesClient.CreateStatusResponseSuccess(response.body<CreateStatusResponse>(), response.headers)
        401, 404, 422, 429, 503 -> StatusesClient.CreateStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.CreateStatusResponseFailure(response.headers)
        else -> StatusesClient.CreateStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.CreateStatusResponseUnknownFailure(500)
    }
  }

  override suspend fun getStatus(id: String): StatusesClient.GetStatusResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.GetStatusResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.GetStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.GetStatusResponseFailure410(response.headers)
        422 -> StatusesClient.GetStatusResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.GetStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.GetStatusResponseUnknownFailure(500)
    }
  }

  override suspend fun updateStatus(request: StatusesClient.UpdateStatusRequest, id: String): StatusesClient.UpdateStatusResponse {
    try {
      val response = configuration.client.put("api/v1/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> StatusesClient.UpdateStatusResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 422, 429, 503 -> StatusesClient.UpdateStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.UpdateStatusResponseFailure(response.headers)
        else -> StatusesClient.UpdateStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.UpdateStatusResponseUnknownFailure(500)
    }
  }

  override suspend fun deleteStatus(id: String, deleteMedia: Boolean?): StatusesClient.DeleteStatusResponse {
    try {
      val response = configuration.client.delete("api/v1/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        url {
          if (deleteMedia != null) {
            parameters.append("delete_media", deleteMedia.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> StatusesClient.DeleteStatusResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.DeleteStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.DeleteStatusResponseFailure410(response.headers)
        422 -> StatusesClient.DeleteStatusResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.DeleteStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.DeleteStatusResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusBookmark(id: String): StatusesClient.PostStatusBookmarkResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/bookmark".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusBookmarkResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.PostStatusBookmarkResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusBookmarkResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusBookmarkResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusBookmarkResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusBookmarkResponseUnknownFailure(500)
    }
  }

  override suspend fun getStatusContext(id: String): StatusesClient.GetStatusContextResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}/context".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.GetStatusContextResponseSuccess(response.body<Context>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.GetStatusContextResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.GetStatusContextResponseFailure410(response.headers)
        422 -> StatusesClient.GetStatusContextResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.GetStatusContextResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.GetStatusContextResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusFavourite(id: String): StatusesClient.PostStatusFavouriteResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/favourite".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusFavouriteResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.PostStatusFavouriteResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusFavouriteResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusFavouriteResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusFavouriteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusFavouriteResponseUnknownFailure(500)
    }
  }

  override suspend fun getStatusFavouritedBy(
    id: String,
    limit: Long?,
    maxId: String?,
    sinceId: String?,
  ): StatusesClient.GetStatusFavouritedByResponse {
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
        200 -> StatusesClient.GetStatusFavouritedByResponseSuccess(response.body<List<Account>>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.GetStatusFavouritedByResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.GetStatusFavouritedByResponseFailure410(response.headers)
        422 -> StatusesClient.GetStatusFavouritedByResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.GetStatusFavouritedByResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.GetStatusFavouritedByResponseUnknownFailure(500)
    }
  }

  override suspend fun getStatusHistory(id: String): StatusesClient.GetStatusHistoryResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}/history".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.GetStatusHistoryResponseSuccess(response.body<List<StatusEdit>>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.GetStatusHistoryResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.GetStatusHistoryResponseFailure410(response.headers)
        422 -> StatusesClient.GetStatusHistoryResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.GetStatusHistoryResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.GetStatusHistoryResponseUnknownFailure(500)
    }
  }

  override suspend fun updateStatusInteractionPolicy(request: StatusesClient.UpdateStatusInteractionPolicyRequest, id: String): StatusesClient.UpdateStatusInteractionPolicyResponse {
    try {
      val response = configuration.client.put("api/v1/statuses/{id}/interaction_policy".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> StatusesClient.UpdateStatusInteractionPolicyResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.UpdateStatusInteractionPolicyResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.UpdateStatusInteractionPolicyResponseFailure410(response.headers)
        422 -> StatusesClient.UpdateStatusInteractionPolicyResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.UpdateStatusInteractionPolicyResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.UpdateStatusInteractionPolicyResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusMute(id: String): StatusesClient.PostStatusMuteResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/mute".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusMuteResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.PostStatusMuteResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusMuteResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusMuteResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusMuteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusMuteResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusPin(id: String): StatusesClient.PostStatusPinResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/pin".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusPinResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 422, 429, 503 -> StatusesClient.PostStatusPinResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusPinResponseFailure(response.headers)
        else -> StatusesClient.PostStatusPinResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusPinResponseUnknownFailure(500)
    }
  }

  override suspend fun getStatusQuotes(
    id: String,
    limit: Long?,
    maxId: String?,
    sinceId: String?,
  ): StatusesClient.GetStatusQuotesResponse {
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
        200 -> StatusesClient.GetStatusQuotesResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.GetStatusQuotesResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.GetStatusQuotesResponseFailure410(response.headers)
        422 -> StatusesClient.GetStatusQuotesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.GetStatusQuotesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.GetStatusQuotesResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusesByIdQuotesByQuotingStatusIdRevoke(id: String, quotingStatusId: String): StatusesClient.PostStatusesByIdQuotesByQuotingStatusIdRevokeResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/quotes/{quoting_status_id}/revoke".replace("/{id}", "/${id.encodeURLPathPart()}").replace("/{quoting_status_id}", "/${quotingStatusId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseSuccess(response.body<Status>(), response.headers)
        401, 403, 404, 429, 503 -> StatusesClient.PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusesByIdQuotesByQuotingStatusIdRevokeResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusReblog(request: StatusesClient.PostStatusReblogRequest, id: String): StatusesClient.PostStatusReblogResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/reblog".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusReblogResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.PostStatusReblogResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusReblogResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusReblogResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusReblogResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusReblogResponseUnknownFailure(500)
    }
  }

  override suspend fun getStatusRebloggedBy(
    id: String,
    limit: Long?,
    maxId: String?,
    sinceId: String?,
  ): StatusesClient.GetStatusRebloggedByResponse {
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
        200 -> StatusesClient.GetStatusRebloggedByResponseSuccess(response.body<List<Account>>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.GetStatusRebloggedByResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.GetStatusRebloggedByResponseFailure410(response.headers)
        422 -> StatusesClient.GetStatusRebloggedByResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.GetStatusRebloggedByResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.GetStatusRebloggedByResponseUnknownFailure(500)
    }
  }

  override suspend fun getStatusSource(id: String): StatusesClient.GetStatusSourceResponse {
    try {
      val response = configuration.client.`get`("api/v1/statuses/{id}/source".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.GetStatusSourceResponseSuccess(response.body<StatusSource>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.GetStatusSourceResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.GetStatusSourceResponseFailure410(response.headers)
        422 -> StatusesClient.GetStatusSourceResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.GetStatusSourceResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.GetStatusSourceResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusTranslate(request: StatusesClient.PostStatusTranslateRequest, id: String): StatusesClient.PostStatusTranslateResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/translate".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusTranslateResponseSuccess(response.body<Translation>(), response.headers)
        401, 403, 404, 429, 503 -> StatusesClient.PostStatusTranslateResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusTranslateResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusTranslateResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusTranslateResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusTranslateResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusUnbookmark(id: String): StatusesClient.PostStatusUnbookmarkResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/unbookmark".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusUnbookmarkResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.PostStatusUnbookmarkResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusUnbookmarkResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusUnbookmarkResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusUnbookmarkResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusUnbookmarkResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusUnfavourite(id: String): StatusesClient.PostStatusUnfavouriteResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/unfavourite".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusUnfavouriteResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.PostStatusUnfavouriteResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusUnfavouriteResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusUnfavouriteResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusUnfavouriteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusUnfavouriteResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusUnmute(id: String): StatusesClient.PostStatusUnmuteResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/unmute".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusUnmuteResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.PostStatusUnmuteResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusUnmuteResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusUnmuteResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusUnmuteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusUnmuteResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusUnpin(id: String): StatusesClient.PostStatusUnpinResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/unpin".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusUnpinResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.PostStatusUnpinResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusUnpinResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusUnpinResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusUnpinResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusUnpinResponseUnknownFailure(500)
    }
  }

  override suspend fun postStatusUnreblog(id: String): StatusesClient.PostStatusUnreblogResponse {
    try {
      val response = configuration.client.post("api/v1/statuses/{id}/unreblog".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> StatusesClient.PostStatusUnreblogResponseSuccess(response.body<Status>(), response.headers)
        401, 404, 429, 503 -> StatusesClient.PostStatusUnreblogResponseFailure401(response.body<Error>(), response.headers)
        410 -> StatusesClient.PostStatusUnreblogResponseFailure410(response.headers)
        422 -> StatusesClient.PostStatusUnreblogResponseFailure(response.body<ValidationError>(), response.headers)
        else -> StatusesClient.PostStatusUnreblogResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return StatusesClient.PostStatusUnreblogResponseUnknownFailure(500)
    }
  }
}
