package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.http.encodeURLPathPart
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
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
        200 -> GetNotificationsResponseSuccess(response.body<List<Notification>>())
        401, 404, 429, 503 -> GetNotificationsResponseFailure401(response.body<Error>())
        410 -> GetNotificationsResponseFailure410
        422 -> GetNotificationsResponseFailure(response.body<ValidationError>())
        else -> GetNotificationsResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetNotificationResponseSuccess(response.body<Notification>())
        401, 404, 429, 503 -> GetNotificationResponseFailure401(response.body<Error>())
        410 -> GetNotificationResponseFailure410
        422 -> GetNotificationResponseFailure(response.body<ValidationError>())
        else -> GetNotificationResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostNotificationDismissResponseSuccess
        401, 404, 429, 503 -> PostNotificationDismissResponseFailure401(response.body<Error>())
        410 -> PostNotificationDismissResponseFailure410
        422 -> PostNotificationDismissResponseFailure(response.body<ValidationError>())
        else -> PostNotificationDismissResponseUnknownFailure(response.status.value)
      }
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
        200 -> CreateNotificationClearResponseSuccess
        401, 404, 429, 503 -> CreateNotificationClearResponseFailure401(response.body<Error>())
        410 -> CreateNotificationClearResponseFailure410
        422 -> CreateNotificationClearResponseFailure(response.body<ValidationError>())
        else -> CreateNotificationClearResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetNotificationRequestsResponseSuccess(response.body<List<NotificationRequest>>())
        401, 404, 429, 503 -> GetNotificationRequestsResponseFailure401(response.body<Error>())
        410 -> GetNotificationRequestsResponseFailure410
        422 -> GetNotificationRequestsResponseFailure(response.body<ValidationError>())
        else -> GetNotificationRequestsResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetNotificationsRequestsByIdResponseSuccess(response.body<NotificationRequest>())
        401, 404, 429, 503 -> GetNotificationsRequestsByIdResponseFailure401(response.body<Error>())
        410 -> GetNotificationsRequestsByIdResponseFailure410
        422 -> GetNotificationsRequestsByIdResponseFailure(response.body<ValidationError>())
        else -> GetNotificationsRequestsByIdResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostNotificationsRequestsByIdAcceptResponseSuccess
        401, 404, 429, 503 -> PostNotificationsRequestsByIdAcceptResponseFailure401(response.body<Error>())
        410 -> PostNotificationsRequestsByIdAcceptResponseFailure410
        422 -> PostNotificationsRequestsByIdAcceptResponseFailure(response.body<ValidationError>())
        else -> PostNotificationsRequestsByIdAcceptResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostNotificationsRequestsByIdDismissResponseSuccess
        401, 404, 429, 503 -> PostNotificationsRequestsByIdDismissResponseFailure401(response.body<Error>())
        410 -> PostNotificationsRequestsByIdDismissResponseFailure410
        422 -> PostNotificationsRequestsByIdDismissResponseFailure(response.body<ValidationError>())
        else -> PostNotificationsRequestsByIdDismissResponseUnknownFailure(response.status.value)
      }
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
        200 -> CreateNotificationsRequestsAcceptResponseSuccess
        401, 404, 429, 503 -> CreateNotificationsRequestsAcceptResponseFailure401(response.body<Error>())
        410 -> CreateNotificationsRequestsAcceptResponseFailure410
        422 -> CreateNotificationsRequestsAcceptResponseFailure(response.body<ValidationError>())
        else -> CreateNotificationsRequestsAcceptResponseUnknownFailure(response.status.value)
      }
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
        200 -> CreateNotificationsRequestsDismissResponseSuccess
        401, 404, 429, 503 -> CreateNotificationsRequestsDismissResponseFailure401(response.body<Error>())
        410 -> CreateNotificationsRequestsDismissResponseFailure410
        422 -> CreateNotificationsRequestsDismissResponseFailure(response.body<ValidationError>())
        else -> CreateNotificationsRequestsDismissResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetNotificationsRequestsMergedResponseSuccess(response.body<MergedResponse>())
        401, 404, 429, 503 -> GetNotificationsRequestsMergedResponseFailure401(response.body<Error>())
        410 -> GetNotificationsRequestsMergedResponseFailure410
        422 -> GetNotificationsRequestsMergedResponseFailure(response.body<ValidationError>())
        else -> GetNotificationsRequestsMergedResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetNotificationsUnreadCountResponseSuccess(response.body<CountResponse>())
        401, 404, 429, 503 -> GetNotificationsUnreadCountResponseFailure401(response.body<Error>())
        410 -> GetNotificationsUnreadCountResponseFailure410
        422 -> GetNotificationsUnreadCountResponseFailure(response.body<ValidationError>())
        else -> GetNotificationsUnreadCountResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetNotificationsV2ResponseSuccess(response.body<GroupedNotificationsResults>())
        401, 404, 429, 503 -> GetNotificationsV2ResponseFailure401(response.body<Error>())
        410 -> GetNotificationsV2ResponseFailure410
        422 -> GetNotificationsV2ResponseFailure(response.body<ValidationError>())
        else -> GetNotificationsV2ResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetNotificationsByGroupKeyV2ResponseSuccess(response.body<GroupedNotificationsResults>())
        401, 404, 429, 503 -> GetNotificationsByGroupKeyV2ResponseFailure401(response.body<Error>())
        410 -> GetNotificationsByGroupKeyV2ResponseFailure410
        422 -> GetNotificationsByGroupKeyV2ResponseFailure(response.body<ValidationError>())
        else -> GetNotificationsByGroupKeyV2ResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetNotificationAccountsV2ResponseSuccess(response.body<List<Account>>())
        401, 404, 429, 503 -> GetNotificationAccountsV2ResponseFailure401(response.body<Error>())
        410 -> GetNotificationAccountsV2ResponseFailure410
        422 -> GetNotificationAccountsV2ResponseFailure(response.body<ValidationError>())
        else -> GetNotificationAccountsV2ResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostNotificationDismissV2ResponseSuccess
        401, 404, 429, 503 -> PostNotificationDismissV2ResponseFailure401(response.body<Error>())
        410 -> PostNotificationDismissV2ResponseFailure410
        422 -> PostNotificationDismissV2ResponseFailure(response.body<ValidationError>())
        else -> PostNotificationDismissV2ResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetNotificationPolicyV2ResponseSuccess(response.body<NotificationPolicy>())
        401, 404, 429, 503 -> GetNotificationPolicyV2ResponseFailure401(response.body<Error>())
        410 -> GetNotificationPolicyV2ResponseFailure410
        422 -> GetNotificationPolicyV2ResponseFailure(response.body<ValidationError>())
        else -> GetNotificationPolicyV2ResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetNotificationsUnreadCountV2ResponseSuccess(response.body<CountResponse>())
        401, 404, 429, 503 -> GetNotificationsUnreadCountV2ResponseFailure401(response.body<Error>())
        410 -> GetNotificationsUnreadCountV2ResponseFailure410
        422 -> GetNotificationsUnreadCountV2ResponseFailure(response.body<ValidationError>())
        else -> GetNotificationsUnreadCountV2ResponseUnknownFailure(response.status.value)
      }
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
  public sealed class GetNotificationsResponse

  @Serializable
  public data class GetNotificationsResponseSuccess(
    public val body: List<Notification>,
  ) : GetNotificationsResponse()

  @Serializable
  public data class GetNotificationsResponseFailure401(
    public val body: Error,
  ) : GetNotificationsResponse()

  @Serializable
  public object GetNotificationsResponseFailure410 : GetNotificationsResponse()

  @Serializable
  public data class GetNotificationsResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationsResponse()

  @Serializable
  public data class GetNotificationsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationsResponse()

  @Serializable
  public sealed class GetNotificationResponse

  @Serializable
  public data class GetNotificationResponseSuccess(
    public val body: Notification,
  ) : GetNotificationResponse()

  @Serializable
  public data class GetNotificationResponseFailure401(
    public val body: Error,
  ) : GetNotificationResponse()

  @Serializable
  public object GetNotificationResponseFailure410 : GetNotificationResponse()

  @Serializable
  public data class GetNotificationResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationResponse()

  @Serializable
  public data class GetNotificationResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationResponse()

  @Serializable
  public sealed class PostNotificationDismissResponse

  @Serializable
  public object PostNotificationDismissResponseSuccess : PostNotificationDismissResponse()

  @Serializable
  public data class PostNotificationDismissResponseFailure401(
    public val body: Error,
  ) : PostNotificationDismissResponse()

  @Serializable
  public object PostNotificationDismissResponseFailure410 : PostNotificationDismissResponse()

  @Serializable
  public data class PostNotificationDismissResponseFailure(
    public val body: ValidationError,
  ) : PostNotificationDismissResponse()

  @Serializable
  public data class PostNotificationDismissResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostNotificationDismissResponse()

  @Serializable
  public sealed class CreateNotificationClearResponse

  @Serializable
  public object CreateNotificationClearResponseSuccess : CreateNotificationClearResponse()

  @Serializable
  public data class CreateNotificationClearResponseFailure401(
    public val body: Error,
  ) : CreateNotificationClearResponse()

  @Serializable
  public object CreateNotificationClearResponseFailure410 : CreateNotificationClearResponse()

  @Serializable
  public data class CreateNotificationClearResponseFailure(
    public val body: ValidationError,
  ) : CreateNotificationClearResponse()

  @Serializable
  public data class CreateNotificationClearResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateNotificationClearResponse()

  @Serializable
  public sealed class GetNotificationRequestsResponse

  @Serializable
  public data class GetNotificationRequestsResponseSuccess(
    public val body: List<NotificationRequest>,
  ) : GetNotificationRequestsResponse()

  @Serializable
  public data class GetNotificationRequestsResponseFailure401(
    public val body: Error,
  ) : GetNotificationRequestsResponse()

  @Serializable
  public object GetNotificationRequestsResponseFailure410 : GetNotificationRequestsResponse()

  @Serializable
  public data class GetNotificationRequestsResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationRequestsResponse()

  @Serializable
  public data class GetNotificationRequestsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationRequestsResponse()

  @Serializable
  public sealed class GetNotificationsRequestsByIdResponse

  @Serializable
  public data class GetNotificationsRequestsByIdResponseSuccess(
    public val body: NotificationRequest,
  ) : GetNotificationsRequestsByIdResponse()

  @Serializable
  public data class GetNotificationsRequestsByIdResponseFailure401(
    public val body: Error,
  ) : GetNotificationsRequestsByIdResponse()

  @Serializable
  public object GetNotificationsRequestsByIdResponseFailure410 : GetNotificationsRequestsByIdResponse()

  @Serializable
  public data class GetNotificationsRequestsByIdResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationsRequestsByIdResponse()

  @Serializable
  public data class GetNotificationsRequestsByIdResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationsRequestsByIdResponse()

  @Serializable
  public sealed class PostNotificationsRequestsByIdAcceptResponse

  @Serializable
  public object PostNotificationsRequestsByIdAcceptResponseSuccess : PostNotificationsRequestsByIdAcceptResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseFailure401(
    public val body: Error,
  ) : PostNotificationsRequestsByIdAcceptResponse()

  @Serializable
  public object PostNotificationsRequestsByIdAcceptResponseFailure410 : PostNotificationsRequestsByIdAcceptResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseFailure(
    public val body: ValidationError,
  ) : PostNotificationsRequestsByIdAcceptResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdAcceptResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostNotificationsRequestsByIdAcceptResponse()

  @Serializable
  public sealed class PostNotificationsRequestsByIdDismissResponse

  @Serializable
  public object PostNotificationsRequestsByIdDismissResponseSuccess : PostNotificationsRequestsByIdDismissResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseFailure401(
    public val body: Error,
  ) : PostNotificationsRequestsByIdDismissResponse()

  @Serializable
  public object PostNotificationsRequestsByIdDismissResponseFailure410 : PostNotificationsRequestsByIdDismissResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseFailure(
    public val body: ValidationError,
  ) : PostNotificationsRequestsByIdDismissResponse()

  @Serializable
  public data class PostNotificationsRequestsByIdDismissResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostNotificationsRequestsByIdDismissResponse()

  @Serializable
  public sealed class CreateNotificationsRequestsAcceptResponse

  @Serializable
  public object CreateNotificationsRequestsAcceptResponseSuccess : CreateNotificationsRequestsAcceptResponse()

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseFailure401(
    public val body: Error,
  ) : CreateNotificationsRequestsAcceptResponse()

  @Serializable
  public object CreateNotificationsRequestsAcceptResponseFailure410 : CreateNotificationsRequestsAcceptResponse()

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseFailure(
    public val body: ValidationError,
  ) : CreateNotificationsRequestsAcceptResponse()

  @Serializable
  public data class CreateNotificationsRequestsAcceptResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateNotificationsRequestsAcceptResponse()

  @Serializable
  public sealed class CreateNotificationsRequestsDismissResponse

  @Serializable
  public object CreateNotificationsRequestsDismissResponseSuccess : CreateNotificationsRequestsDismissResponse()

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseFailure401(
    public val body: Error,
  ) : CreateNotificationsRequestsDismissResponse()

  @Serializable
  public object CreateNotificationsRequestsDismissResponseFailure410 : CreateNotificationsRequestsDismissResponse()

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseFailure(
    public val body: ValidationError,
  ) : CreateNotificationsRequestsDismissResponse()

  @Serializable
  public data class CreateNotificationsRequestsDismissResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateNotificationsRequestsDismissResponse()

  @Serializable
  public sealed class GetNotificationsRequestsMergedResponse

  @Serializable
  public data class GetNotificationsRequestsMergedResponseSuccess(
    public val body: MergedResponse,
  ) : GetNotificationsRequestsMergedResponse()

  @Serializable
  public data class GetNotificationsRequestsMergedResponseFailure401(
    public val body: Error,
  ) : GetNotificationsRequestsMergedResponse()

  @Serializable
  public object GetNotificationsRequestsMergedResponseFailure410 : GetNotificationsRequestsMergedResponse()

  @Serializable
  public data class GetNotificationsRequestsMergedResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationsRequestsMergedResponse()

  @Serializable
  public data class GetNotificationsRequestsMergedResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationsRequestsMergedResponse()

  @Serializable
  public sealed class GetNotificationsUnreadCountResponse

  @Serializable
  public data class GetNotificationsUnreadCountResponseSuccess(
    public val body: CountResponse,
  ) : GetNotificationsUnreadCountResponse()

  @Serializable
  public data class GetNotificationsUnreadCountResponseFailure401(
    public val body: Error,
  ) : GetNotificationsUnreadCountResponse()

  @Serializable
  public object GetNotificationsUnreadCountResponseFailure410 : GetNotificationsUnreadCountResponse()

  @Serializable
  public data class GetNotificationsUnreadCountResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationsUnreadCountResponse()

  @Serializable
  public data class GetNotificationsUnreadCountResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationsUnreadCountResponse()

  @Serializable
  public sealed class GetNotificationsV2Response

  @Serializable
  public data class GetNotificationsV2ResponseSuccess(
    public val body: GroupedNotificationsResults,
  ) : GetNotificationsV2Response()

  @Serializable
  public data class GetNotificationsV2ResponseFailure401(
    public val body: Error,
  ) : GetNotificationsV2Response()

  @Serializable
  public object GetNotificationsV2ResponseFailure410 : GetNotificationsV2Response()

  @Serializable
  public data class GetNotificationsV2ResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationsV2Response()

  @Serializable
  public data class GetNotificationsV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationsV2Response()

  @Serializable
  public sealed class GetNotificationsByGroupKeyV2Response

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseSuccess(
    public val body: GroupedNotificationsResults,
  ) : GetNotificationsByGroupKeyV2Response()

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseFailure401(
    public val body: Error,
  ) : GetNotificationsByGroupKeyV2Response()

  @Serializable
  public object GetNotificationsByGroupKeyV2ResponseFailure410 : GetNotificationsByGroupKeyV2Response()

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationsByGroupKeyV2Response()

  @Serializable
  public data class GetNotificationsByGroupKeyV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationsByGroupKeyV2Response()

  @Serializable
  public sealed class GetNotificationAccountsV2Response

  @Serializable
  public data class GetNotificationAccountsV2ResponseSuccess(
    public val body: List<Account>,
  ) : GetNotificationAccountsV2Response()

  @Serializable
  public data class GetNotificationAccountsV2ResponseFailure401(
    public val body: Error,
  ) : GetNotificationAccountsV2Response()

  @Serializable
  public object GetNotificationAccountsV2ResponseFailure410 : GetNotificationAccountsV2Response()

  @Serializable
  public data class GetNotificationAccountsV2ResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationAccountsV2Response()

  @Serializable
  public data class GetNotificationAccountsV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationAccountsV2Response()

  @Serializable
  public sealed class PostNotificationDismissV2Response

  @Serializable
  public object PostNotificationDismissV2ResponseSuccess : PostNotificationDismissV2Response()

  @Serializable
  public data class PostNotificationDismissV2ResponseFailure401(
    public val body: Error,
  ) : PostNotificationDismissV2Response()

  @Serializable
  public object PostNotificationDismissV2ResponseFailure410 : PostNotificationDismissV2Response()

  @Serializable
  public data class PostNotificationDismissV2ResponseFailure(
    public val body: ValidationError,
  ) : PostNotificationDismissV2Response()

  @Serializable
  public data class PostNotificationDismissV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostNotificationDismissV2Response()

  @Serializable
  public sealed class GetNotificationPolicyV2Response

  @Serializable
  public data class GetNotificationPolicyV2ResponseSuccess(
    public val body: NotificationPolicy,
  ) : GetNotificationPolicyV2Response()

  @Serializable
  public data class GetNotificationPolicyV2ResponseFailure401(
    public val body: Error,
  ) : GetNotificationPolicyV2Response()

  @Serializable
  public object GetNotificationPolicyV2ResponseFailure410 : GetNotificationPolicyV2Response()

  @Serializable
  public data class GetNotificationPolicyV2ResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationPolicyV2Response()

  @Serializable
  public data class GetNotificationPolicyV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationPolicyV2Response()

  @Serializable
  public sealed class GetNotificationsUnreadCountV2Response

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseSuccess(
    public val body: CountResponse,
  ) : GetNotificationsUnreadCountV2Response()

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseFailure401(
    public val body: Error,
  ) : GetNotificationsUnreadCountV2Response()

  @Serializable
  public object GetNotificationsUnreadCountV2ResponseFailure410 : GetNotificationsUnreadCountV2Response()

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseFailure(
    public val body: ValidationError,
  ) : GetNotificationsUnreadCountV2Response()

  @Serializable
  public data class GetNotificationsUnreadCountV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetNotificationsUnreadCountV2Response()
}
