package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Status
import mastodon.api.model.ValidationError

public class FavouritesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View favourited statuses
   */
  public suspend fun getFavourites(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetFavouritesResponse {
    try {
      val response = configuration.client.`get`("api/v1/favourites") {
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
        200 -> GetFavouritesResponseSuccess(response.body<List<Status>>())
        401, 404, 429, 503 -> GetFavouritesResponseFailure401(response.body<Error>())
        410 -> GetFavouritesResponseFailure410
        422 -> GetFavouritesResponseFailure(response.body<ValidationError>())
        else -> GetFavouritesResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFavouritesResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetFavouritesResponse

  @Serializable
  public data class GetFavouritesResponseSuccess(
    public val body: List<Status>,
  ) : GetFavouritesResponse()

  @Serializable
  public data class GetFavouritesResponseFailure401(
    public val body: Error,
  ) : GetFavouritesResponse()

  @Serializable
  public object GetFavouritesResponseFailure410 : GetFavouritesResponse()

  @Serializable
  public data class GetFavouritesResponseFailure(
    public val body: ValidationError,
  ) : GetFavouritesResponse()

  @Serializable
  public data class GetFavouritesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFavouritesResponse()
}
