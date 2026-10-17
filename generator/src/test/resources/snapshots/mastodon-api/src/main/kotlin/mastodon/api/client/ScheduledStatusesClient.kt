package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Unit
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.ScheduledStatus
import mastodon.api.model.ValidationError

public interface ScheduledStatusesClient {
  /**
   * View scheduled statuses
   */
  public suspend fun getScheduledStatuses(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetScheduledStatusesResponse

  /**
   * View a single scheduled status
   */
  public suspend fun getScheduledStatus(id: String): GetScheduledStatusResponse

  /**
   * Update a scheduled status's publishing date
   */
  public suspend fun updateScheduledStatus(request: UpdateScheduledStatusRequest, id: String): UpdateScheduledStatusResponse

  /**
   * Cancel a scheduled status
   */
  public suspend fun deleteScheduledStatus(id: String): DeleteScheduledStatusResponse

  @Serializable
  public sealed class GetScheduledStatusesResponse : OperationResponse<List<ScheduledStatus>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetScheduledStatusesResponseSuccess(
    override val body: List<ScheduledStatus>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetScheduledStatusesResponse(),
      OperationResponseSuccess<List<ScheduledStatus>> {
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
  public data class GetScheduledStatusesResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetScheduledStatusesResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetScheduledStatusesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetScheduledStatusesResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetScheduledStatusesResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetScheduledStatusesResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetScheduledStatusesResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetScheduledStatusesResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetScheduledStatusResponse : OperationResponse<ScheduledStatus> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetScheduledStatusResponseSuccess(
    override val body: ScheduledStatus,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetScheduledStatusResponse(),
      OperationResponseSuccess<ScheduledStatus> {
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
  public data class GetScheduledStatusResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetScheduledStatusResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetScheduledStatusResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetScheduledStatusResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetScheduledStatusResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetScheduledStatusResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetScheduledStatusResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetScheduledStatusResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public data class UpdateScheduledStatusRequest(
    @SerialName("scheduled_at")
    public val scheduledAt: String? = null,
  )

  @Serializable
  public sealed class UpdateScheduledStatusResponse : OperationResponse<ScheduledStatus> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class UpdateScheduledStatusResponseSuccess(
    override val body: ScheduledStatus,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateScheduledStatusResponse(),
      OperationResponseSuccess<ScheduledStatus> {
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
  public data class UpdateScheduledStatusResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateScheduledStatusResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class UpdateScheduledStatusResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateScheduledStatusResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class UpdateScheduledStatusResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateScheduledStatusResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class DeleteScheduledStatusResponse : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class DeleteScheduledStatusResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteScheduledStatusResponse(),
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
  public data class DeleteScheduledStatusResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteScheduledStatusResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class DeleteScheduledStatusResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteScheduledStatusResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class DeleteScheduledStatusResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteScheduledStatusResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class DeleteScheduledStatusResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteScheduledStatusResponse(),
      OperationResponseUnknownFailure
}

public fun ScheduledStatusesClient(configuration: ClientConfiguration = defaultClientConfiguration): ScheduledStatusesClient = DefaultScheduledStatusesClient(configuration)

public class DefaultScheduledStatusesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : ScheduledStatusesClient {
  override suspend fun getScheduledStatuses(
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): ScheduledStatusesClient.GetScheduledStatusesResponse {
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
        200 -> ScheduledStatusesClient.GetScheduledStatusesResponseSuccess(response.body<List<ScheduledStatus>>(), response.headers)
        401, 404, 429, 503 -> ScheduledStatusesClient.GetScheduledStatusesResponseFailure401(response.body<Error>(), response.headers)
        410 -> ScheduledStatusesClient.GetScheduledStatusesResponseFailure410(response.headers)
        422 -> ScheduledStatusesClient.GetScheduledStatusesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> ScheduledStatusesClient.GetScheduledStatusesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return ScheduledStatusesClient.GetScheduledStatusesResponseUnknownFailure(500)
    }
  }

  override suspend fun getScheduledStatus(id: String): ScheduledStatusesClient.GetScheduledStatusResponse {
    try {
      val response = configuration.client.`get`("api/v1/scheduled_statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> ScheduledStatusesClient.GetScheduledStatusResponseSuccess(response.body<ScheduledStatus>(), response.headers)
        401, 404, 429, 503 -> ScheduledStatusesClient.GetScheduledStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> ScheduledStatusesClient.GetScheduledStatusResponseFailure410(response.headers)
        422 -> ScheduledStatusesClient.GetScheduledStatusResponseFailure(response.body<ValidationError>(), response.headers)
        else -> ScheduledStatusesClient.GetScheduledStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return ScheduledStatusesClient.GetScheduledStatusResponseUnknownFailure(500)
    }
  }

  override suspend fun updateScheduledStatus(request: ScheduledStatusesClient.UpdateScheduledStatusRequest, id: String): ScheduledStatusesClient.UpdateScheduledStatusResponse {
    try {
      val response = configuration.client.put("api/v1/scheduled_statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> ScheduledStatusesClient.UpdateScheduledStatusResponseSuccess(response.body<ScheduledStatus>(), response.headers)
        401, 404, 422, 429, 503 -> ScheduledStatusesClient.UpdateScheduledStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> ScheduledStatusesClient.UpdateScheduledStatusResponseFailure(response.headers)
        else -> ScheduledStatusesClient.UpdateScheduledStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return ScheduledStatusesClient.UpdateScheduledStatusResponseUnknownFailure(500)
    }
  }

  override suspend fun deleteScheduledStatus(id: String): ScheduledStatusesClient.DeleteScheduledStatusResponse {
    try {
      val response = configuration.client.delete("api/v1/scheduled_statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> ScheduledStatusesClient.DeleteScheduledStatusResponseSuccess(response.headers)
        401, 404, 429, 503 -> ScheduledStatusesClient.DeleteScheduledStatusResponseFailure401(response.body<Error>(), response.headers)
        410 -> ScheduledStatusesClient.DeleteScheduledStatusResponseFailure410(response.headers)
        422 -> ScheduledStatusesClient.DeleteScheduledStatusResponseFailure(response.body<ValidationError>(), response.headers)
        else -> ScheduledStatusesClient.DeleteScheduledStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return ScheduledStatusesClient.DeleteScheduledStatusResponseUnknownFailure(500)
    }
  }
}
