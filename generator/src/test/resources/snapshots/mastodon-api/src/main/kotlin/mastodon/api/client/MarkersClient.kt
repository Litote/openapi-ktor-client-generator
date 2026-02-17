package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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
        200 -> GetMarkersResponseSuccess(response.body<Map<String, Marker>>())
        401, 404, 429, 503 -> GetMarkersResponseFailure401(response.body<Error>())
        410 -> GetMarkersResponseFailure410
        422 -> GetMarkersResponseFailure(response.body<ValidationError>())
        else -> GetMarkersResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetMarkersResponseUnknownFailure(500)
    }
  }

  /**
   * Save your position in a timeline
   */
  public suspend fun createMarker(request: JsonElement): CreateMarkerResponse {
    try {
      val response = configuration.client.post("api/v1/markers") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateMarkerResponseSuccess(response.body<Map<String, Marker>>())
        401, 404, 429, 503 -> CreateMarkerResponseFailure401(response.body<Error>())
        410 -> CreateMarkerResponseFailure410
        422 -> CreateMarkerResponseFailure(response.body<ValidationError>())
        else -> CreateMarkerResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateMarkerResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetMarkersResponse

  @Serializable
  public data class GetMarkersResponseSuccess(
    public val body: Map<String, Marker>,
  ) : GetMarkersResponse()

  @Serializable
  public data class GetMarkersResponseFailure401(
    public val body: Error,
  ) : GetMarkersResponse()

  @Serializable
  public object GetMarkersResponseFailure410 : GetMarkersResponse()

  @Serializable
  public data class GetMarkersResponseFailure(
    public val body: ValidationError,
  ) : GetMarkersResponse()

  @Serializable
  public data class GetMarkersResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetMarkersResponse()

  @Serializable
  public sealed class CreateMarkerResponse

  @Serializable
  public data class CreateMarkerResponseSuccess(
    public val body: Map<String, Marker>,
  ) : CreateMarkerResponse()

  @Serializable
  public data class CreateMarkerResponseFailure401(
    public val body: Error,
  ) : CreateMarkerResponse()

  @Serializable
  public object CreateMarkerResponseFailure410 : CreateMarkerResponse()

  @Serializable
  public data class CreateMarkerResponseFailure(
    public val body: ValidationError,
  ) : CreateMarkerResponse()

  @Serializable
  public data class CreateMarkerResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateMarkerResponse()
}
