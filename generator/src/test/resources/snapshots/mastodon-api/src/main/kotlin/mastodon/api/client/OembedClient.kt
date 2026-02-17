package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.OEmbedResponse
import mastodon.api.model.ValidationError

public class OembedClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get OEmbed info as JSON
   */
  public suspend fun getOembed(
    url: String,
    maxheight: Long? = null,
    maxwidth: Long? = 400,
  ): GetOembedResponse {
    try {
      val response = configuration.client.`get`("api/oembed") {
        url {
          parameters.append("url", url)
          if (maxheight != null) {
            parameters.append("maxheight", maxheight.toString())
          }
          if (maxwidth != null) {
            parameters.append("maxwidth", maxwidth.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetOembedResponseSuccess(response.body<OEmbedResponse>())
        401, 404, 429, 503 -> GetOembedResponseFailure401(response.body<Error>())
        410 -> GetOembedResponseFailure410
        422 -> GetOembedResponseFailure(response.body<ValidationError>())
        else -> GetOembedResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetOembedResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetOembedResponse

  @Serializable
  public data class GetOembedResponseSuccess(
    public val body: OEmbedResponse,
  ) : GetOembedResponse()

  @Serializable
  public data class GetOembedResponseFailure401(
    public val body: Error,
  ) : GetOembedResponse()

  @Serializable
  public object GetOembedResponseFailure410 : GetOembedResponse()

  @Serializable
  public data class GetOembedResponseFailure(
    public val body: ValidationError,
  ) : GetOembedResponse()

  @Serializable
  public data class GetOembedResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetOembedResponse()
}
