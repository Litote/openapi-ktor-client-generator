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

public class NotificationsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
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
  ): GetNotificationsResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications") {
        url {
          if (accountId != null) {
            parameters.append("account_id", accountId)
          }
          if (excludeTypes != null) {
            parameters.append("exclude_types", excludeTypes.joinToString(","))
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
            parameters.append("types", types.joinToString(","))
          }
        }
      }
      return when (response.status.value) {
        200 -> GetNotificationsResponseSuccess(response.body<List<Notification>>(), response.headers)
        401, 404, 429, 503 -> GetNotificationsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationsResponseFailure410(response.headers)
        422 -> GetNotificationsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationsResponseUnknownFailure(500)
    }
  }

  /**
   * Get a single notification
   */
  public suspend fun getNotification(id: String): GetNotificationResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetNotificationResponseSuccess(response.body<Notification>(), response.headers)
        401, 404, 429, 503 -> GetNotificationResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationResponseFailure410(response.headers)
        422 -> GetNotificationResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationResponseUnknownFailure(500)
    }
  }

  /**
   * Dismiss a single notification
   */
  public suspend fun postNotificationDismiss(id: String): PostNotificationDismissResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/{id}/dismiss".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostNotificationDismissResponseSuccess(response.headers)
        401, 404, 429, 503 -> PostNotificationDismissResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostNotificationDismissResponseFailure410(response.headers)
        422 -> PostNotificationDismissResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostNotificationDismissResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostNotificationDismissResponseUnknownFailure(500)
    }
  }

  /**
   * Dismiss all notifications
   */
  public suspend fun createNotificationClear(): CreateNotificationClearResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/clear") {
      }
      return when (response.status.value) {
        200 -> CreateNotificationClearResponseSuccess(response.headers)
        401, 404, 429, 503 -> CreateNotificationClearResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateNotificationClearResponseFailure410(response.headers)
        422 -> CreateNotificationClearResponseFailure(response.body<ValidationError>(), response.headers)
        else -> CreateNotificationClearResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateNotificationClearResponseUnknownFailure(500)
    }
  }

  /**
   * Get all notification requests
   */
  public suspend fun getNotificationRequests(
    limit: Long? = 40,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetNotificationRequestsResponse {
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
        200 -> GetNotificationRequestsResponseSuccess(response.body<List<NotificationRequest>>(), response.headers)
        401, 404, 429, 503 -> GetNotificationRequestsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationRequestsResponseFailure410(response.headers)
        422 -> GetNotificationRequestsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationRequestsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationRequestsResponseUnknownFailure(500)
    }
  }

  /**
   * Get a single notification request
   */
  public suspend fun getNotificationsRequestsById(id: String): GetNotificationsRequestsByIdResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications/requests/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetNotificationsRequestsByIdResponseSuccess(response.body<NotificationRequest>(), response.headers)
        401, 404, 429, 503 -> GetNotificationsRequestsByIdResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationsRequestsByIdResponseFailure410(response.headers)
        422 -> GetNotificationsRequestsByIdResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationsRequestsByIdResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationsRequestsByIdResponseUnknownFailure(500)
    }
  }

  /**
   * Accept a single notification request
   */
  public suspend fun postNotificationsRequestsByIdAccept(id: String): PostNotificationsRequestsByIdAcceptResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/requests/{id}/accept".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostNotificationsRequestsByIdAcceptResponseSuccess(response.headers)
        401, 404, 429, 503 -> PostNotificationsRequestsByIdAcceptResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostNotificationsRequestsByIdAcceptResponseFailure410(response.headers)
        422 -> PostNotificationsRequestsByIdAcceptResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostNotificationsRequestsByIdAcceptResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostNotificationsRequestsByIdAcceptResponseUnknownFailure(500)
    }
  }

  /**
   * Dismiss a single notification request
   */
  public suspend fun postNotificationsRequestsByIdDismiss(id: String): PostNotificationsRequestsByIdDismissResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/requests/{id}/dismiss".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostNotificationsRequestsByIdDismissResponseSuccess(response.headers)
        401, 404, 429, 503 -> PostNotificationsRequestsByIdDismissResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostNotificationsRequestsByIdDismissResponseFailure410(response.headers)
        422 -> PostNotificationsRequestsByIdDismissResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostNotificationsRequestsByIdDismissResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostNotificationsRequestsByIdDismissResponseUnknownFailure(500)
    }
  }

  /**
   * Accept multiple notification requests
   */
  public suspend fun createNotificationsRequestsAccept(): CreateNotificationsRequestsAcceptResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/requests/accept") {
      }
      return when (response.status.value) {
        200 -> CreateNotificationsRequestsAcceptResponseSuccess(response.headers)
        401, 404, 429, 503 -> CreateNotificationsRequestsAcceptResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateNotificationsRequestsAcceptResponseFailure410(response.headers)
        422 -> CreateNotificationsRequestsAcceptResponseFailure(response.body<ValidationError>(), response.headers)
        else -> CreateNotificationsRequestsAcceptResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateNotificationsRequestsAcceptResponseUnknownFailure(500)
    }
  }

  /**
   * Dismiss multiple notification requests
   */
  public suspend fun createNotificationsRequestsDismiss(): CreateNotificationsRequestsDismissResponse {
    try {
      val response = configuration.client.post("api/v1/notifications/requests/dismiss") {
      }
      return when (response.status.value) {
        200 -> CreateNotificationsRequestsDismissResponseSuccess(response.headers)
        401, 404, 429, 503 -> CreateNotificationsRequestsDismissResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateNotificationsRequestsDismissResponseFailure410(response.headers)
        422 -> CreateNotificationsRequestsDismissResponseFailure(response.body<ValidationError>(), response.headers)
        else -> CreateNotificationsRequestsDismissResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateNotificationsRequestsDismissResponseUnknownFailure(500)
    }
  }

  /**
   * Check if accepted notification requests have been merged
   */
  public suspend fun getNotificationsRequestsMerged(): GetNotificationsRequestsMergedResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications/requests/merged") {
      }
      return when (response.status.value) {
        200 -> GetNotificationsRequestsMergedResponseSuccess(response.body<MergedResponse>(), response.headers)
        401, 404, 429, 503 -> GetNotificationsRequestsMergedResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationsRequestsMergedResponseFailure410(response.headers)
        422 -> GetNotificationsRequestsMergedResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationsRequestsMergedResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationsRequestsMergedResponseUnknownFailure(500)
    }
  }

  /**
   * Get the number of unread notifications
   */
  public suspend fun getNotificationsUnreadCount(
    accountId: String? = null,
    excludeTypes: List<GetNotificationsUnreadCountExcludeTypes>? = null,
    limit: Long? = 100,
    types: List<GetNotificationsUnreadCountTypes>? = null,
  ): GetNotificationsUnreadCountResponse {
    try {
      val response = configuration.client.`get`("api/v1/notifications/unread_count") {
        url {
          if (accountId != null) {
            parameters.append("account_id", accountId)
          }
          if (excludeTypes != null) {
            parameters.append("exclude_types", excludeTypes.joinToString(","))
          }
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (types != null) {
            parameters.append("types", types.joinToString(","))
          }
        }
      }
      return when (response.status.value) {
        200 -> GetNotificationsUnreadCountResponseSuccess(response.body<CountResponse>(), response.headers)
        401, 404, 429, 503 -> GetNotificationsUnreadCountResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationsUnreadCountResponseFailure410(response.headers)
        422 -> GetNotificationsUnreadCountResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationsUnreadCountResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationsUnreadCountResponseUnknownFailure(500)
    }
  }

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
  ): GetNotificationsV2Response {
    try {
      val response = configuration.client.`get`("api/v2/notifications") {
        url {
          if (accountId != null) {
            parameters.append("account_id", accountId)
          }
          if (excludeTypes != null) {
            parameters.append("exclude_types", excludeTypes.joinToString(","))
          }
          if (expandAccounts != null) {
            parameters.append("expand_accounts", expandAccounts)
          }
          if (groupedTypes != null) {
            parameters.append("grouped_types", groupedTypes.joinToString(","))
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
            parameters.append("types", types.joinToString(","))
          }
        }
      }
      return when (response.status.value) {
        200 -> GetNotificationsV2ResponseSuccess(response.body<GroupedNotificationsResults>(), response.headers)
        401, 404, 429, 503 -> GetNotificationsV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationsV2ResponseFailure410(response.headers)
        422 -> GetNotificationsV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationsV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationsV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Get a single notification group
   */
  public suspend fun getNotificationsByGroupKeyV2(groupKey: String): GetNotificationsByGroupKeyV2Response {
    try {
      val response = configuration.client.`get`("api/v2/notifications/{group_key}".replace("/{group_key}", "/${groupKey.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetNotificationsByGroupKeyV2ResponseSuccess(response.body<GroupedNotificationsResults>(), response.headers)
        401, 404, 429, 503 -> GetNotificationsByGroupKeyV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationsByGroupKeyV2ResponseFailure410(response.headers)
        422 -> GetNotificationsByGroupKeyV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationsByGroupKeyV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationsByGroupKeyV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Get accounts of all notifications in a notification group
   */
  public suspend fun getNotificationAccountsV2(groupKey: String): GetNotificationAccountsV2Response {
    try {
      val response = configuration.client.`get`("api/v2/notifications/{group_key}/accounts".replace("/{group_key}", "/${groupKey.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetNotificationAccountsV2ResponseSuccess(response.body<List<Account>>(), response.headers)
        401, 404, 429, 503 -> GetNotificationAccountsV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationAccountsV2ResponseFailure410(response.headers)
        422 -> GetNotificationAccountsV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationAccountsV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationAccountsV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Dismiss a single notification group
   */
  public suspend fun postNotificationDismissV2(groupKey: String): PostNotificationDismissV2Response {
    try {
      val response = configuration.client.post("api/v2/notifications/{group_key}/dismiss".replace("/{group_key}", "/${groupKey.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostNotificationDismissV2ResponseSuccess(response.headers)
        401, 404, 429, 503 -> PostNotificationDismissV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostNotificationDismissV2ResponseFailure410(response.headers)
        422 -> PostNotificationDismissV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostNotificationDismissV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostNotificationDismissV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Get the filtering policy for notifications
   */
  public suspend fun getNotificationPolicyV2(): GetNotificationPolicyV2Response {
    try {
      val response = configuration.client.`get`("api/v2/notifications/policy") {
      }
      return when (response.status.value) {
        200 -> GetNotificationPolicyV2ResponseSuccess(response.body<NotificationPolicy>(), response.headers)
        401, 404, 429, 503 -> GetNotificationPolicyV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationPolicyV2ResponseFailure410(response.headers)
        422 -> GetNotificationPolicyV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationPolicyV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationPolicyV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Get the number of unread notifications
   */
  public suspend fun getNotificationsUnreadCountV2(
    accountId: String? = null,
    excludeTypes: List<GetNotificationsUnreadCountV2ExcludeTypes>? = null,
    groupedTypes: List<String>? = null,
    limit: Long? = 100,
    types: List<GetNotificationsUnreadCountV2Types>? = null,
  ): GetNotificationsUnreadCountV2Response {
    try {
      val response = configuration.client.`get`("api/v2/notifications/unread_count") {
        url {
          if (accountId != null) {
            parameters.append("account_id", accountId)
          }
          if (excludeTypes != null) {
            parameters.append("exclude_types", excludeTypes.joinToString(","))
          }
          if (groupedTypes != null) {
            parameters.append("grouped_types", groupedTypes.joinToString(","))
          }
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (types != null) {
            parameters.append("types", types.joinToString(","))
          }
        }
      }
      return when (response.status.value) {
        200 -> GetNotificationsUnreadCountV2ResponseSuccess(response.body<CountResponse>(), response.headers)
        401, 404, 429, 503 -> GetNotificationsUnreadCountV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetNotificationsUnreadCountV2ResponseFailure410(response.headers)
        422 -> GetNotificationsUnreadCountV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetNotificationsUnreadCountV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetNotificationsUnreadCountV2ResponseUnknownFailure(500)
    }
  }

  @Serializable
  public object GetNotificationsUnreadCountExcludeTypes

  @Serializable
  public object GetNotificationsUnreadCountV2ExcludeTypes

  @Serializable
  public object GetNotificationsUnreadCountTypes

  @Serializable
  public object GetNotificationsUnreadCountV2Types

  @Serializable
  public object GroupedTypes

  @Serializable
  public sealed class GetNotificationsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationsResponseSuccess(
    public val body: List<Notification>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsResponse()

  @Serializable
  public data class GetNotificationsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsResponse()

  @Serializable
  public data class GetNotificationsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsResponse()

  @Serializable
  public data class GetNotificationsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsResponse()

  @Serializable
  public sealed class GetNotificationResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationResponseSuccess(
    public val body: Notification,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationResponse()

  @Serializable
  public data class GetNotificationResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationResponse()

  @Serializable
  public data class GetNotificationResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationResponse()

  @Serializable
  public data class GetNotificationResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationResponse()

  @Serializable
  public sealed class PostNotificationDismissResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostNotificationDismissResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissResponse()

  @Serializable
  public data class PostNotificationDismissResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissResponse()

  @Serializable
  public data class PostNotificationDismissResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissResponse()

  @Serializable
  public data class PostNotificationDismissResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissResponse()

  @Serializable
  public sealed class CreateNotificationClearResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateNotificationClearResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationClearResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationClearResponse()

  @Serializable
  public data class CreateNotificationClearResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationClearResponse()

  @Serializable
  public data class CreateNotificationClearResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationClearResponse()

  @Serializable
  public data class CreateNotificationClearResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationClearResponse()

  @Serializable
  public sealed class GetNotificationRequestsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationRequestsResponseSuccess(
    public val body: List<NotificationRequest>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationRequestsResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationRequestsResponse()

  @Serializable
  public data class GetNotificationRequestsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationRequestsResponse()

  @Serializable
  public data class GetNotificationRequestsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationRequestsResponse()

  @Serializable
  public data class GetNotificationRequestsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationRequestsResponse()

  @Serializable
  public sealed class GetNotificationsRequestsByIdResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationsRequestsByIdResponseSuccess(
    public val body: NotificationRequest,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsByIdResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsByIdResponse()

  @Serializable
  public data class GetNotificationsRequestsByIdResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsByIdResponse()

  @Serializable
  public data class GetNotificationsRequestsByIdResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsByIdResponse()

  @Serializable
  public data class GetNotificationsRequestsByIdResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsByIdResponse()

  @Serializable
  public sealed class PostNotificationsRequestsByIdAcceptResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdAcceptResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdAcceptResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdAcceptResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdAcceptResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdAcceptResponse()

  @Serializable
  public sealed class PostNotificationsRequestsByIdDismissResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdDismissResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdDismissResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdDismissResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdDismissResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationsRequestsByIdDismissResponse()

  @Serializable
  public sealed class CreateNotificationsRequestsAcceptResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsAcceptResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsAcceptResponse()

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsAcceptResponse()

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsAcceptResponse()

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsAcceptResponse()

  @Serializable
  public sealed class CreateNotificationsRequestsDismissResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsDismissResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsDismissResponse()

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsDismissResponse()

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsDismissResponse()

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateNotificationsRequestsDismissResponse()

  @Serializable
  public sealed class GetNotificationsRequestsMergedResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationsRequestsMergedResponseSuccess(
    public val body: MergedResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsMergedResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsMergedResponse()

  @Serializable
  public data class GetNotificationsRequestsMergedResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsMergedResponse()

  @Serializable
  public data class GetNotificationsRequestsMergedResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsMergedResponse()

  @Serializable
  public data class GetNotificationsRequestsMergedResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsRequestsMergedResponse()

  @Serializable
  public sealed class GetNotificationsUnreadCountResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationsUnreadCountResponseSuccess(
    public val body: CountResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountResponse()

  @Serializable
  public data class GetNotificationsUnreadCountResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountResponse()

  @Serializable
  public data class GetNotificationsUnreadCountResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountResponse()

  @Serializable
  public data class GetNotificationsUnreadCountResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountResponse()

  @Serializable
  public sealed class GetNotificationsV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationsV2ResponseSuccess(
    public val body: GroupedNotificationsResults,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsV2Response() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsV2Response()

  @Serializable
  public data class GetNotificationsV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsV2Response()

  @Serializable
  public data class GetNotificationsV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsV2Response()

  @Serializable
  public data class GetNotificationsV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsV2Response()

  @Serializable
  public sealed class GetNotificationsByGroupKeyV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseSuccess(
    public val body: GroupedNotificationsResults,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsByGroupKeyV2Response() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsByGroupKeyV2Response()

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsByGroupKeyV2Response()

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsByGroupKeyV2Response()

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsByGroupKeyV2Response()

  @Serializable
  public sealed class GetNotificationAccountsV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationAccountsV2ResponseSuccess(
    public val body: List<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationAccountsV2Response() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationAccountsV2Response()

  @Serializable
  public data class GetNotificationAccountsV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationAccountsV2Response()

  @Serializable
  public data class GetNotificationAccountsV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationAccountsV2Response()

  @Serializable
  public data class GetNotificationAccountsV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationAccountsV2Response()

  @Serializable
  public sealed class PostNotificationDismissV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostNotificationDismissV2ResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissV2Response() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissV2Response()

  @Serializable
  public data class PostNotificationDismissV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissV2Response()

  @Serializable
  public data class PostNotificationDismissV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissV2Response()

  @Serializable
  public data class PostNotificationDismissV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostNotificationDismissV2Response()

  @Serializable
  public sealed class GetNotificationPolicyV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationPolicyV2ResponseSuccess(
    public val body: NotificationPolicy,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationPolicyV2Response() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationPolicyV2Response()

  @Serializable
  public data class GetNotificationPolicyV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationPolicyV2Response()

  @Serializable
  public data class GetNotificationPolicyV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationPolicyV2Response()

  @Serializable
  public data class GetNotificationPolicyV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationPolicyV2Response()

  @Serializable
  public sealed class GetNotificationsUnreadCountV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseSuccess(
    public val body: CountResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountV2Response() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountV2Response()

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountV2Response()

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountV2Response()

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetNotificationsUnreadCountV2Response()
}
