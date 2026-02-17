package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.ScheduledStatus
import mastodon.api.model.ValidationError

public class ScheduledStatusesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View scheduled statuses
   */
  public suspend fun getScheduledStatuses(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetScheduledStatusesResponse {
    try {
      val response = configuration.client.`get`("api/v1/scheduled_statuses") {
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
        200 -> GetScheduledStatusesResponseSuccess(response.body<List<ScheduledStatus>>())
        401, 404, 429, 503 -> GetScheduledStatusesResponseFailure401(response.body<Error>())
        410 -> GetScheduledStatusesResponseFailure410
        422 -> GetScheduledStatusesResponseFailure(response.body<ValidationError>())
        else -> GetScheduledStatusesResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetScheduledStatusesResponseUnknownFailure(500)
    }
  }

  /**
   * View a single scheduled status
   */
  public suspend fun getScheduledStatus(id: String): GetScheduledStatusResponse {
    try {
      val response = configuration.client.`get`("api/v1/scheduled_statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetScheduledStatusResponseSuccess(response.body<ScheduledStatus>())
        401, 404, 429, 503 -> GetScheduledStatusResponseFailure401(response.body<Error>())
        410 -> GetScheduledStatusResponseFailure410
        422 -> GetScheduledStatusResponseFailure(response.body<ValidationError>())
        else -> GetScheduledStatusResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetScheduledStatusResponseUnknownFailure(500)
    }
  }

  /**
   * Update a scheduled status's publishing date
   */
  public suspend fun updateScheduledStatus(request: JsonElement, id: String): UpdateScheduledStatusResponse {
    try {
      val response = configuration.client.put("api/v1/scheduled_statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> UpdateScheduledStatusResponseSuccess(response.body<ScheduledStatus>())
        401, 404, 422, 429, 503 -> UpdateScheduledStatusResponseFailure401(response.body<Error>())
        410 -> UpdateScheduledStatusResponseFailure
        else -> UpdateScheduledStatusResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return UpdateScheduledStatusResponseUnknownFailure(500)
    }
  }

  /**
   * Cancel a scheduled status
   */
  public suspend fun deleteScheduledStatus(id: String): DeleteScheduledStatusResponse {
    try {
      val response = configuration.client.delete("api/v1/scheduled_statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteScheduledStatusResponseSuccess
        401, 404, 429, 503 -> DeleteScheduledStatusResponseFailure401(response.body<Error>())
        410 -> DeleteScheduledStatusResponseFailure410
        422 -> DeleteScheduledStatusResponseFailure(response.body<ValidationError>())
        else -> DeleteScheduledStatusResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteScheduledStatusResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetScheduledStatusesResponse

  @Serializable
  public data class GetScheduledStatusesResponseSuccess(
    public val body: List<ScheduledStatus>,
  ) : GetScheduledStatusesResponse()

  @Serializable
  public data class GetScheduledStatusesResponseFailure401(
    public val body: Error,
  ) : GetScheduledStatusesResponse()

  @Serializable
  public object GetScheduledStatusesResponseFailure410 : GetScheduledStatusesResponse()

  @Serializable
  public data class GetScheduledStatusesResponseFailure(
    public val body: ValidationError,
  ) : GetScheduledStatusesResponse()

  @Serializable
  public data class GetScheduledStatusesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetScheduledStatusesResponse()

  @Serializable
  public sealed class GetScheduledStatusResponse

  @Serializable
  public data class GetScheduledStatusResponseSuccess(
    public val body: ScheduledStatus,
  ) : GetScheduledStatusResponse()

  @Serializable
  public data class GetScheduledStatusResponseFailure401(
    public val body: Error,
  ) : GetScheduledStatusResponse()

  @Serializable
  public object GetScheduledStatusResponseFailure410 : GetScheduledStatusResponse()

  @Serializable
  public data class GetScheduledStatusResponseFailure(
    public val body: ValidationError,
  ) : GetScheduledStatusResponse()

  @Serializable
  public data class GetScheduledStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetScheduledStatusResponse()

  @Serializable
  public sealed class UpdateScheduledStatusResponse

  @Serializable
  public data class UpdateScheduledStatusResponseSuccess(
    public val body: ScheduledStatus,
  ) : UpdateScheduledStatusResponse()

  @Serializable
  public data class UpdateScheduledStatusResponseFailure401(
    public val body: Error,
  ) : UpdateScheduledStatusResponse()

  @Serializable
  public object UpdateScheduledStatusResponseFailure : UpdateScheduledStatusResponse()

  @Serializable
  public data class UpdateScheduledStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : UpdateScheduledStatusResponse()

  @Serializable
  public sealed class DeleteScheduledStatusResponse

  @Serializable
  public object DeleteScheduledStatusResponseSuccess : DeleteScheduledStatusResponse()

  @Serializable
  public data class DeleteScheduledStatusResponseFailure401(
    public val body: Error,
  ) : DeleteScheduledStatusResponse()

  @Serializable
  public object DeleteScheduledStatusResponseFailure410 : DeleteScheduledStatusResponse()

  @Serializable
  public data class DeleteScheduledStatusResponseFailure(
    public val body: ValidationError,
  ) : DeleteScheduledStatusResponse()

  @Serializable
  public data class DeleteScheduledStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteScheduledStatusResponse()
}
