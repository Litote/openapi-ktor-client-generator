package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.AsyncRefreshResponse
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class AsyncRefreshesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get Status of Async Refresh
   */
  public suspend fun getAsyncRefreshV1Alpha(id: String): GetAsyncRefreshV1AlphaResponse {
    try {
      val response = configuration.client.`get`("api/v1_alpha/async_refreshes/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetAsyncRefreshV1AlphaResponseSuccess(response.body<AsyncRefreshResponse>())
        401, 404, 429, 503 -> GetAsyncRefreshV1AlphaResponseFailure401(response.body<Error>())
        410 -> GetAsyncRefreshV1AlphaResponseFailure410
        422 -> GetAsyncRefreshV1AlphaResponseFailure(response.body<ValidationError>())
        else -> GetAsyncRefreshV1AlphaResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAsyncRefreshV1AlphaResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetAsyncRefreshV1AlphaResponse

  @Serializable
  public data class GetAsyncRefreshV1AlphaResponseSuccess(
    public val body: AsyncRefreshResponse,
  ) : GetAsyncRefreshV1AlphaResponse()

  @Serializable
  public data class GetAsyncRefreshV1AlphaResponseFailure401(
    public val body: Error,
  ) : GetAsyncRefreshV1AlphaResponse()

  @Serializable
  public object GetAsyncRefreshV1AlphaResponseFailure410 : GetAsyncRefreshV1AlphaResponse()

  @Serializable
  public data class GetAsyncRefreshV1AlphaResponseFailure(
    public val body: ValidationError,
  ) : GetAsyncRefreshV1AlphaResponse()

  @Serializable
  public data class GetAsyncRefreshV1AlphaResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAsyncRefreshV1AlphaResponse()
}
