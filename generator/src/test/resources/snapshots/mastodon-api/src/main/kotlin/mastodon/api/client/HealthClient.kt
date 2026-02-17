package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Int
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class HealthClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get basic health status as JSON
   */
  public suspend fun getHealth(): GetHealthResponse {
    try {
      val response = configuration.client.`get`("health") {
      }
      return when (response.status.value) {
        200 -> GetHealthResponseSuccess
        401, 404, 429, 503 -> GetHealthResponseFailure401(response.body<Error>())
        410 -> GetHealthResponseFailure410
        422 -> GetHealthResponseFailure(response.body<ValidationError>())
        else -> GetHealthResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetHealthResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetHealthResponse

  @Serializable
  public object GetHealthResponseSuccess : GetHealthResponse()

  @Serializable
  public data class GetHealthResponseFailure401(
    public val body: Error,
  ) : GetHealthResponse()

  @Serializable
  public object GetHealthResponseFailure410 : GetHealthResponse()

  @Serializable
  public data class GetHealthResponseFailure(
    public val body: ValidationError,
  ) : GetHealthResponse()

  @Serializable
  public data class GetHealthResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetHealthResponse()
}
