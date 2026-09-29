package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.FilterContextEnum
import mastodon.api.model.Marker
import mastodon.api.model.ValidationError

public class MarkersClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get saved timeline positions
   */
  public suspend fun getMarkers(timeline: List<FilterContextEnum>? = null): GetMarkersResponse {
    try {
      val response = configuration.client.`get`("api/v1/markers") {
        url {
          if (timeline != null) {
            parameters.append("timeline", timeline.joinToString(","))
          }
        }
      }
      return when (response.status.value) {
        200 -> GetMarkersResponseSuccess(response.body<Map<String, Marker>>(), response.headers)
        401, 404, 429, 503 -> GetMarkersResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetMarkersResponseFailure410(response.headers)
        422 -> GetMarkersResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetMarkersResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetMarkersResponseUnknownFailure(500)
    }
  }

  /**
   * Save your position in a timeline
   */
  public suspend fun createMarker(request: CreateMarkerRequest): CreateMarkerResponse {
    try {
      val response = configuration.client.post("api/v1/markers") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateMarkerResponseSuccess(response.body<Map<String, Marker>>(), response.headers)
        401, 404, 429, 503 -> CreateMarkerResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateMarkerResponseFailure410(response.headers)
        422 -> CreateMarkerResponseFailure(response.body<ValidationError>(), response.headers)
        else -> CreateMarkerResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateMarkerResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetMarkersResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetMarkersResponseSuccess(
    public val body: Map<String, Marker>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMarkersResponse() {
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
  public data class GetMarkersResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMarkersResponse()

  @Serializable
  public data class GetMarkersResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMarkersResponse()

  @Serializable
  public data class GetMarkersResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMarkersResponse()

  @Serializable
  public data class GetMarkersResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMarkersResponse()

  @Serializable
  public data class CreateMarkerRequest(
    public val home: Home? = null,
    public val notifications: Notifications? = null,
  ) {
    @Serializable
    public data class Home(
      @SerialName("last_read_id")
      public val lastReadId: String? = null,
    )

    @Serializable
    public data class Notifications(
      @SerialName("last_read_id")
      public val lastReadId: String? = null,
    )
  }

  @Serializable
  public sealed class CreateMarkerResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateMarkerResponseSuccess(
    public val body: Map<String, Marker>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMarkerResponse() {
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
  public data class CreateMarkerResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMarkerResponse()

  @Serializable
  public data class CreateMarkerResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMarkerResponse()

  @Serializable
  public data class CreateMarkerResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMarkerResponse()

  @Serializable
  public data class CreateMarkerResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMarkerResponse()
}
