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

public class BookmarksClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View bookmarked statuses
   */
  public suspend fun getBookmarks(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetBookmarksResponse {
    try {
      val response = configuration.client.`get`("api/v1/bookmarks") {
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
        200 -> GetBookmarksResponseSuccess(response.body<List<Status>>())
        401, 404, 429, 503 -> GetBookmarksResponseFailure401(response.body<Error>())
        410 -> GetBookmarksResponseFailure410
        422 -> GetBookmarksResponseFailure(response.body<ValidationError>())
        else -> GetBookmarksResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetBookmarksResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetBookmarksResponse

  @Serializable
  public data class GetBookmarksResponseSuccess(
    public val body: List<Status>,
  ) : GetBookmarksResponse()

  @Serializable
  public data class GetBookmarksResponseFailure401(
    public val body: Error,
  ) : GetBookmarksResponse()

  @Serializable
  public object GetBookmarksResponseFailure410 : GetBookmarksResponse()

  @Serializable
  public data class GetBookmarksResponseFailure(
    public val body: ValidationError,
  ) : GetBookmarksResponse()

  @Serializable
  public data class GetBookmarksResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetBookmarksResponse()
}
