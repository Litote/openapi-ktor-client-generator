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

public class EndorsementsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View currently featured profiles
   */
  public suspend fun getEndorsements(
    limit: Long? = 40,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetEndorsementsResponse {
    try {
      val response = configuration.client.`get`("api/v1/endorsements") {
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
        200 -> GetEndorsementsResponseSuccess(response.body<List<Account>>())
        401, 404, 429, 503 -> GetEndorsementsResponseFailure401(response.body<Error>())
        410 -> GetEndorsementsResponseFailure410
        422 -> GetEndorsementsResponseFailure(response.body<ValidationError>())
        else -> GetEndorsementsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetEndorsementsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetEndorsementsResponse

  @Serializable
  public data class GetEndorsementsResponseSuccess(
    public val body: List<Account>,
  ) : GetEndorsementsResponse()

  @Serializable
  public data class GetEndorsementsResponseFailure401(
    public val body: Error,
  ) : GetEndorsementsResponse()

  @Serializable
  public object GetEndorsementsResponseFailure410 : GetEndorsementsResponse()

  @Serializable
  public data class GetEndorsementsResponseFailure(
    public val body: ValidationError,
  ) : GetEndorsementsResponse()

  @Serializable
  public data class GetEndorsementsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetEndorsementsResponse()
}
