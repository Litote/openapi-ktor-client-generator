package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class MutesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View muted accounts
   */
  public suspend fun getMutes(
    limit: Long? = 40,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetMutesResponse {
    try {
      val response = configuration.client.`get`("api/v1/mutes") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (sinceId != null) {
            parameters.append("since_id", sinceId)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetMutesResponseSuccess(response.body<List<Account>>())
        401, 404, 429, 503 -> GetMutesResponseFailure401(response.body<Error>())
        410 -> GetMutesResponseFailure410
        422 -> GetMutesResponseFailure(response.body<ValidationError>())
        else -> GetMutesResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetMutesResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetMutesResponse

  @Serializable
  public data class GetMutesResponseSuccess(
    public val body: List<Account>,
  ) : GetMutesResponse()

  @Serializable
  public data class GetMutesResponseFailure401(
    public val body: Error,
  ) : GetMutesResponse()

  @Serializable
  public object GetMutesResponseFailure410 : GetMutesResponse()

  @Serializable
  public data class GetMutesResponseFailure(
    public val body: ValidationError,
  ) : GetMutesResponse()

  @Serializable
  public data class GetMutesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetMutesResponse()
}
