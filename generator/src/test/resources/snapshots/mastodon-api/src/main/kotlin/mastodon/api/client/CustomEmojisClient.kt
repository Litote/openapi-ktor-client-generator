package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Int
import kotlin.collections.List
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.CustomEmoji
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class CustomEmojisClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View all custom emoji
   */
  public suspend fun getCustomEmojis(): GetCustomEmojisResponse {
    try {
      val response = configuration.client.`get`("api/v1/custom_emojis") {
      }
      return when (response.status.value) {
        200 -> GetCustomEmojisResponseSuccess(response.body<List<CustomEmoji>>())
        401, 404, 429, 503 -> GetCustomEmojisResponseFailure401(response.body<Error>())
        410 -> GetCustomEmojisResponseFailure410
        422 -> GetCustomEmojisResponseFailure(response.body<ValidationError>())
        else -> GetCustomEmojisResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetCustomEmojisResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetCustomEmojisResponse

  @Serializable
  public data class GetCustomEmojisResponseSuccess(
    public val body: List<CustomEmoji>,
  ) : GetCustomEmojisResponse()

  @Serializable
  public data class GetCustomEmojisResponseFailure401(
    public val body: Error,
  ) : GetCustomEmojisResponse()

  @Serializable
  public object GetCustomEmojisResponseFailure410 : GetCustomEmojisResponse()

  @Serializable
  public data class GetCustomEmojisResponseFailure(
    public val body: ValidationError,
  ) : GetCustomEmojisResponse()

  @Serializable
  public data class GetCustomEmojisResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetCustomEmojisResponse()
}
