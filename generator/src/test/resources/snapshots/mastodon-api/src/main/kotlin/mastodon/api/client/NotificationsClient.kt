package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.CountResponse
import mastodon.api.model.Error
import mastodon.api.model.GroupedNotificationsResults
import mastodon.api.model.MergedResponse
import mastodon.api.model.Notification
import mastodon.api.model.NotificationPolicy
import mastodon.api.model.NotificationRequest
import mastodon.api.model.NotificationTypeEnum
import mastodon.api.model.ValidationError

public interface NotificationsClient {
  /**
   * Get all notifications
   */
  public suspend fun getNotifications(
    accountId: String? = null,
    excludeTypes: List<NotificationTypeEnum>? = null,
    includeFiltered: Boolean? = false,
    limit: Long? = 40,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
    types: List<NotificationTypeEnum>? = null,
  ): GetNotificationsResponse

  /**
   * Get a single notification
   */
  public suspend fun getNotification(id: String): GetNotificationResponse

  /**
   * Dismiss a single notification
   */
  public suspend fun postNotificationDismiss(id: String): PostNotificationDismissResponse

  /**
   * Dismiss all notifications
   */
  public suspend fun createNotificationClear(): CreateNotificationClearResponse

  /**
   * Get all notification requests
   */
  public suspend fun getNotificationRequests(
    limit: Long? = 40,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetNotificationRequestsResponse

  /**
   * Get a single notification request
   */
  public suspend fun getNotificationsRequestsById(id: String): GetNotificationsRequestsByIdResponse

  /**
   * Accept a single notification request
   */
  public suspend fun postNotificationsRequestsByIdAccept(id: String): PostNotificationsRequestsByIdAcceptResponse

  /**
   * Dismiss a single notification request
   */
  public suspend fun postNotificationsRequestsByIdDismiss(id: String): PostNotificationsRequestsByIdDismissResponse

  /**
   * Accept multiple notification requests
   */
  public suspend fun createNotificationsRequestsAccept(): CreateNotificationsRequestsAcceptResponse

  /**
   * Dismiss multiple notification requests
   */
  public suspend fun createNotificationsRequestsDismiss(): CreateNotificationsRequestsDismissResponse

  /**
   * Check if accepted notification requests have been merged
   */
  public suspend fun getNotificationsRequestsMerged(): GetNotificationsRequestsMergedResponse

  /**
   * Get the number of unread notifications
   */
  public suspend fun getNotificationsUnreadCount(
    accountId: String? = null,
    excludeTypes: List<String>? = null,
    limit: Long? = 100,
    types: List<String>? = null,
  ): GetNotificationsUnreadCountResponse

  /**
   * Get all grouped notifications
   */
  public suspend fun getNotificationsV2(
    accountId: String? = null,
    excludeTypes: List<NotificationTypeEnum>? = null,
    expandAccounts: String? = null,
    groupedTypes: List<NotificationTypeEnum>? = null,
    includeFiltered: Boolean? = false,
    limit: Long? = 40,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
    types: List<NotificationTypeEnum>? = null,
  ): GetNotificationsV2Response

  /**
   * Get a single notification group
   */
  public suspend fun getNotificationsByGroupKeyV2(groupKey: String): GetNotificationsByGroupKeyV2Response

  /**
   * Get accounts of all notifications in a notification group
   */
  public suspend fun getNotificationAccountsV2(groupKey: String): GetNotificationAccountsV2Response

  /**
   * Dismiss a single notification group
   */
  public suspend fun postNotificationDismissV2(groupKey: String): PostNotificationDismissV2Response

  /**
   * Get the filtering policy for notifications
   */
  public suspend fun getNotificationPolicyV2(): GetNotificationPolicyV2Response

  /**
   * Get the number of unread notifications
   */
  public suspend fun getNotificationsUnreadCountV2(
    accountId: String? = null,
    excludeTypes: List<String>? = null,
    groupedTypes: List<String>? = null,
    limit: Long? = 100,
    types: List<String>? = null,
  ): GetNotificationsUnreadCountV2Response

  @Serializable
  public sealed class GetNotificationsResponse : OperationResponse<List<Notification>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationsResponseSuccess(
    override val body: List<Notification>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsResponse(),
      OperationResponseSuccess<List<Notification>> {
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
  public data class GetNotificationsResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationsResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetNotificationResponse : OperationResponse<Notification> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationResponseSuccess(
    override val body: Notification,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationResponse(),
      OperationResponseSuccess<Notification> {
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
  public data class GetNotificationResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostNotificationDismissResponse : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostNotificationDismissResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissResponse(),
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
  public data class PostNotificationDismissResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostNotificationDismissResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostNotificationDismissResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostNotificationDismissResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class CreateNotificationClearResponse : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class CreateNotificationClearResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationClearResponse(),
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
  public data class CreateNotificationClearResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationClearResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class CreateNotificationClearResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationClearResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class CreateNotificationClearResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationClearResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class CreateNotificationClearResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationClearResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetNotificationRequestsResponse : OperationResponse<List<NotificationRequest>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationRequestsResponseSuccess(
    override val body: List<NotificationRequest>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationRequestsResponse(),
      OperationResponseSuccess<List<NotificationRequest>> {
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
  public data class GetNotificationRequestsResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationRequestsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationRequestsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationRequestsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationRequestsResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationRequestsResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationRequestsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationRequestsResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetNotificationsRequestsByIdResponse : OperationResponse<NotificationRequest> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationsRequestsByIdResponseSuccess(
    override val body: NotificationRequest,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsByIdResponse(),
      OperationResponseSuccess<NotificationRequest> {
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
  public data class GetNotificationsRequestsByIdResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsByIdResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationsRequestsByIdResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsByIdResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationsRequestsByIdResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsByIdResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationsRequestsByIdResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsByIdResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostNotificationsRequestsByIdAcceptResponse : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdAcceptResponse(),
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
  public data class PostNotificationsRequestsByIdAcceptResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdAcceptResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdAcceptResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdAcceptResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdAcceptResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostNotificationsRequestsByIdDismissResponse : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdDismissResponse(),
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
  public data class PostNotificationsRequestsByIdDismissResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdDismissResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdDismissResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdDismissResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdDismissResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class CreateNotificationsRequestsAcceptResponse : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsAcceptResponse(),
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
  public data class CreateNotificationsRequestsAcceptResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsAcceptResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsAcceptResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsAcceptResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsAcceptResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class CreateNotificationsRequestsDismissResponse : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsDismissResponse(),
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
  public data class CreateNotificationsRequestsDismissResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsDismissResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsDismissResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsDismissResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsDismissResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetNotificationsRequestsMergedResponse : OperationResponse<MergedResponse> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationsRequestsMergedResponseSuccess(
    override val body: MergedResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsMergedResponse(),
      OperationResponseSuccess<MergedResponse> {
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
  public data class GetNotificationsRequestsMergedResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsMergedResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationsRequestsMergedResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsMergedResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationsRequestsMergedResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsMergedResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationsRequestsMergedResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsMergedResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetNotificationsUnreadCountResponse : OperationResponse<CountResponse> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationsUnreadCountResponseSuccess(
    override val body: CountResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountResponse(),
      OperationResponseSuccess<CountResponse> {
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
  public data class GetNotificationsUnreadCountResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationsUnreadCountResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationsUnreadCountResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationsUnreadCountResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetNotificationsV2Response : OperationResponse<GroupedNotificationsResults> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationsV2ResponseSuccess(
    override val body: GroupedNotificationsResults,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsV2Response(),
      OperationResponseSuccess<GroupedNotificationsResults> {
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
  public data class GetNotificationsV2ResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsV2Response(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationsV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsV2Response(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationsV2ResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsV2Response(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationsV2ResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsV2Response(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetNotificationsByGroupKeyV2Response : OperationResponse<GroupedNotificationsResults> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseSuccess(
    override val body: GroupedNotificationsResults,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsByGroupKeyV2Response(),
      OperationResponseSuccess<GroupedNotificationsResults> {
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
  public data class GetNotificationsByGroupKeyV2ResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsByGroupKeyV2Response(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsByGroupKeyV2Response(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsByGroupKeyV2Response(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsByGroupKeyV2Response(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetNotificationAccountsV2Response : OperationResponse<List<Account>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationAccountsV2ResponseSuccess(
    override val body: List<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationAccountsV2Response(),
      OperationResponseSuccess<List<Account>> {
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
  public data class GetNotificationAccountsV2ResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationAccountsV2Response(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationAccountsV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationAccountsV2Response(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationAccountsV2ResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationAccountsV2Response(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationAccountsV2ResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationAccountsV2Response(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostNotificationDismissV2Response : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostNotificationDismissV2ResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissV2Response(),
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
  public data class PostNotificationDismissV2ResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissV2Response(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostNotificationDismissV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissV2Response(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostNotificationDismissV2ResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissV2Response(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class PostNotificationDismissV2ResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissV2Response(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetNotificationPolicyV2Response : OperationResponse<NotificationPolicy> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationPolicyV2ResponseSuccess(
    override val body: NotificationPolicy,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationPolicyV2Response(),
      OperationResponseSuccess<NotificationPolicy> {
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
  public data class GetNotificationPolicyV2ResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationPolicyV2Response(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationPolicyV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationPolicyV2Response(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationPolicyV2ResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationPolicyV2Response(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationPolicyV2ResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationPolicyV2Response(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetNotificationsUnreadCountV2Response : OperationResponse<CountResponse> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseSuccess(
    override val body: CountResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountV2Response(),
      OperationResponseSuccess<CountResponse> {
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
  public data class GetNotificationsUnreadCountV2ResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountV2Response(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountV2Response(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountV2Response(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountV2Response(),
      OperationResponseUnknownFailure
}

public fun NotificationsClient(configuration: ClientConfiguration = defaultClientConfiguration): NotificationsClient = DefaultNotificationsClient(configuration)

public class DefaultNotificationsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : NotificationsClient {
  override suspend fun getNotifications(
    accountId: String?,
    excludeTypes: List<NotificationTypeEnum>?,
    includeFiltered: Boolean?,
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
    types: List<NotificationTypeEnum>?,
  ): NotificationsClient.GetNotificationsResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications") {
        url {
          if (accountId != null) {
            parameters.append("account_id", accountId)
          }
          if (excludeTypes != null) {
            parameters.appendAll("exclude_types", excludeTypes.map { it.serialName() })
          }
          if (includeFiltered != null) {
            parameters.append("include_filtered", includeFiltered.toString())
          }
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
          if (types != null) {
            parameters.appendAll("types", types.map { it.serialName() })
          }
        }
      }
      return when (response.status.value) {
        200 -> NotificationsClient.GetNotificationsResponseSuccess(response.body<List<Notification>>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationsResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationsResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationsResponseUnknownFailure(500)
    }
  }

  override suspend fun getNotification(id: String): NotificationsClient.GetNotificationResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.GetNotificationResponseSuccess(response.body<Notification>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationResponseUnknownFailure(500)
    }
  }

  override suspend fun postNotificationDismiss(id: String): NotificationsClient.PostNotificationDismissResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/{id}/dismiss".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.PostNotificationDismissResponseSuccess(response.headers)
        401, 404, 429, 503 -> NotificationsClient.PostNotificationDismissResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.PostNotificationDismissResponseFailure410(response.headers)
        422 -> NotificationsClient.PostNotificationDismissResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.PostNotificationDismissResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.PostNotificationDismissResponseUnknownFailure(500)
    }
  }

  override suspend fun createNotificationClear(): NotificationsClient.CreateNotificationClearResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/clear") {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.CreateNotificationClearResponseSuccess(response.headers)
        401, 404, 429, 503 -> NotificationsClient.CreateNotificationClearResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.CreateNotificationClearResponseFailure410(response.headers)
        422 -> NotificationsClient.CreateNotificationClearResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.CreateNotificationClearResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.CreateNotificationClearResponseUnknownFailure(500)
    }
  }

  override suspend fun getNotificationRequests(
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): NotificationsClient.GetNotificationRequestsResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications/requests") {
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
        200 -> NotificationsClient.GetNotificationRequestsResponseSuccess(response.body<List<NotificationRequest>>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationRequestsResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationRequestsResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationRequestsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationRequestsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationRequestsResponseUnknownFailure(500)
    }
  }

  override suspend fun getNotificationsRequestsById(id: String): NotificationsClient.GetNotificationsRequestsByIdResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications/requests/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.GetNotificationsRequestsByIdResponseSuccess(response.body<NotificationRequest>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationsRequestsByIdResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationsRequestsByIdResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationsRequestsByIdResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationsRequestsByIdResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationsRequestsByIdResponseUnknownFailure(500)
    }
  }

  override suspend fun postNotificationsRequestsByIdAccept(id: String): NotificationsClient.PostNotificationsRequestsByIdAcceptResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/requests/{id}/accept".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.PostNotificationsRequestsByIdAcceptResponseSuccess(response.headers)
        401, 404, 429, 503 -> NotificationsClient.PostNotificationsRequestsByIdAcceptResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.PostNotificationsRequestsByIdAcceptResponseFailure410(response.headers)
        422 -> NotificationsClient.PostNotificationsRequestsByIdAcceptResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.PostNotificationsRequestsByIdAcceptResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.PostNotificationsRequestsByIdAcceptResponseUnknownFailure(500)
    }
  }

  override suspend fun postNotificationsRequestsByIdDismiss(id: String): NotificationsClient.PostNotificationsRequestsByIdDismissResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/requests/{id}/dismiss".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.PostNotificationsRequestsByIdDismissResponseSuccess(response.headers)
        401, 404, 429, 503 -> NotificationsClient.PostNotificationsRequestsByIdDismissResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.PostNotificationsRequestsByIdDismissResponseFailure410(response.headers)
        422 -> NotificationsClient.PostNotificationsRequestsByIdDismissResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.PostNotificationsRequestsByIdDismissResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.PostNotificationsRequestsByIdDismissResponseUnknownFailure(500)
    }
  }

  override suspend fun createNotificationsRequestsAccept(): NotificationsClient.CreateNotificationsRequestsAcceptResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/requests/accept") {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.CreateNotificationsRequestsAcceptResponseSuccess(response.headers)
        401, 404, 429, 503 -> NotificationsClient.CreateNotificationsRequestsAcceptResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.CreateNotificationsRequestsAcceptResponseFailure410(response.headers)
        422 -> NotificationsClient.CreateNotificationsRequestsAcceptResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.CreateNotificationsRequestsAcceptResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.CreateNotificationsRequestsAcceptResponseUnknownFailure(500)
    }
  }

  override suspend fun createNotificationsRequestsDismiss(): NotificationsClient.CreateNotificationsRequestsDismissResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/requests/dismiss") {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.CreateNotificationsRequestsDismissResponseSuccess(response.headers)
        401, 404, 429, 503 -> NotificationsClient.CreateNotificationsRequestsDismissResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.CreateNotificationsRequestsDismissResponseFailure410(response.headers)
        422 -> NotificationsClient.CreateNotificationsRequestsDismissResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.CreateNotificationsRequestsDismissResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.CreateNotificationsRequestsDismissResponseUnknownFailure(500)
    }
  }

  override suspend fun getNotificationsRequestsMerged(): NotificationsClient.GetNotificationsRequestsMergedResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications/requests/merged") {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.GetNotificationsRequestsMergedResponseSuccess(response.body<MergedResponse>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationsRequestsMergedResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationsRequestsMergedResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationsRequestsMergedResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationsRequestsMergedResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationsRequestsMergedResponseUnknownFailure(500)
    }
  }

  override suspend fun getNotificationsUnreadCount(
    accountId: String?,
    excludeTypes: List<String>?,
    limit: Long?,
    types: List<String>?,
  ): NotificationsClient.GetNotificationsUnreadCountResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications/unread_count") {
        url {
          if (accountId != null) {
            parameters.append("account_id", accountId)
          }
          if (excludeTypes != null) {
            parameters.appendAll("exclude_types", excludeTypes)
          }
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (types != null) {
            parameters.appendAll("types", types)
          }
        }
      }
      return when (response.status.value) {
        200 -> NotificationsClient.GetNotificationsUnreadCountResponseSuccess(response.body<CountResponse>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationsUnreadCountResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationsUnreadCountResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationsUnreadCountResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationsUnreadCountResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationsUnreadCountResponseUnknownFailure(500)
    }
  }

  override suspend fun getNotificationsV2(
    accountId: String?,
    excludeTypes: List<NotificationTypeEnum>?,
    expandAccounts: String?,
    groupedTypes: List<NotificationTypeEnum>?,
    includeFiltered: Boolean?,
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
    types: List<NotificationTypeEnum>?,
  ): NotificationsClient.GetNotificationsV2Response {
    try {
      val response = configuration.client.`get`("api/v2/notifications") {
        url {
          if (accountId != null) {
            parameters.append("account_id", accountId)
          }
          if (excludeTypes != null) {
            parameters.appendAll("exclude_types", excludeTypes.map { it.serialName() })
          }
          if (expandAccounts != null) {
            parameters.append("expand_accounts", expandAccounts)
          }
          if (groupedTypes != null) {
            parameters.appendAll("grouped_types", groupedTypes.map { it.serialName() })
          }
          if (includeFiltered != null) {
            parameters.append("include_filtered", includeFiltered.toString())
          }
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
          if (types != null) {
            parameters.appendAll("types", types.map { it.serialName() })
          }
        }
      }
      return when (response.status.value) {
        200 -> NotificationsClient.GetNotificationsV2ResponseSuccess(response.body<GroupedNotificationsResults>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationsV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationsV2ResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationsV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationsV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationsV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun getNotificationsByGroupKeyV2(groupKey: String): NotificationsClient.GetNotificationsByGroupKeyV2Response {
    try {
      val response = configuration.client.`get`("api/v2/notifications/{group_key}".replace("/{group_key}", "/${groupKey.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.GetNotificationsByGroupKeyV2ResponseSuccess(response.body<GroupedNotificationsResults>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationsByGroupKeyV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationsByGroupKeyV2ResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationsByGroupKeyV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationsByGroupKeyV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationsByGroupKeyV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun getNotificationAccountsV2(groupKey: String): NotificationsClient.GetNotificationAccountsV2Response {
    try {
      val response = configuration.client.`get`("api/v2/notifications/{group_key}/accounts".replace("/{group_key}", "/${groupKey.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.GetNotificationAccountsV2ResponseSuccess(response.body<List<Account>>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationAccountsV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationAccountsV2ResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationAccountsV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationAccountsV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationAccountsV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun postNotificationDismissV2(groupKey: String): NotificationsClient.PostNotificationDismissV2Response {
    try {
      val response = configuration.client.post("api/v2/notifications/{group_key}/dismiss".replace("/{group_key}", "/${groupKey.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.PostNotificationDismissV2ResponseSuccess(response.headers)
        401, 404, 429, 503 -> NotificationsClient.PostNotificationDismissV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.PostNotificationDismissV2ResponseFailure410(response.headers)
        422 -> NotificationsClient.PostNotificationDismissV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.PostNotificationDismissV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.PostNotificationDismissV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun getNotificationPolicyV2(): NotificationsClient.GetNotificationPolicyV2Response {
    try {
      val response = configuration.client.`get`("api/v2/notifications/policy") {
      }
      return when (response.status.value) {
        200 -> NotificationsClient.GetNotificationPolicyV2ResponseSuccess(response.body<NotificationPolicy>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationPolicyV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationPolicyV2ResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationPolicyV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationPolicyV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationPolicyV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun getNotificationsUnreadCountV2(
    accountId: String?,
    excludeTypes: List<String>?,
    groupedTypes: List<String>?,
    limit: Long?,
    types: List<String>?,
  ): NotificationsClient.GetNotificationsUnreadCountV2Response {
    try {
      val response = configuration.client.`get`("api/v2/notifications/unread_count") {
        url {
          if (accountId != null) {
            parameters.append("account_id", accountId)
          }
          if (excludeTypes != null) {
            parameters.appendAll("exclude_types", excludeTypes)
          }
          if (groupedTypes != null) {
            parameters.appendAll("grouped_types", groupedTypes)
          }
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (types != null) {
            parameters.appendAll("types", types)
          }
        }
      }
      return when (response.status.value) {
        200 -> NotificationsClient.GetNotificationsUnreadCountV2ResponseSuccess(response.body<CountResponse>(), response.headers)
        401, 404, 429, 503 -> NotificationsClient.GetNotificationsUnreadCountV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> NotificationsClient.GetNotificationsUnreadCountV2ResponseFailure410(response.headers)
        422 -> NotificationsClient.GetNotificationsUnreadCountV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> NotificationsClient.GetNotificationsUnreadCountV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return NotificationsClient.GetNotificationsUnreadCountV2ResponseUnknownFailure(500)
    }
  }
}
