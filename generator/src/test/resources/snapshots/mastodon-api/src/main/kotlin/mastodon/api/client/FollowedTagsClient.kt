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
import mastodon.api.model.Tag
import mastodon.api.model.ValidationError

public class FollowedTagsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View all followed tags
   */
  public suspend fun getFollowedTags(
    limit: Long? = 100,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetFollowedTagsResponse {
    try {
      val response = configuration.client.`get`("api/v1/followed_tags") {
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
        200 -> GetFollowedTagsResponseSuccess(response.body<List<Tag>>())
        401, 404, 429, 503 -> GetFollowedTagsResponseFailure401(response.body<Error>())
        410 -> GetFollowedTagsResponseFailure410
        422 -> GetFollowedTagsResponseFailure(response.body<ValidationError>())
        else -> GetFollowedTagsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFollowedTagsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetFollowedTagsResponse

  @Serializable
  public data class GetFollowedTagsResponseSuccess(
    public val body: List<Tag>,
  ) : GetFollowedTagsResponse()

  @Serializable
  public data class GetFollowedTagsResponseFailure401(
    public val body: Error,
  ) : GetFollowedTagsResponse()

  @Serializable
  public object GetFollowedTagsResponseFailure410 : GetFollowedTagsResponse()

  @Serializable
  public data class GetFollowedTagsResponseFailure(
    public val body: ValidationError,
  ) : GetFollowedTagsResponse()

  @Serializable
  public data class GetFollowedTagsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFollowedTagsResponse()
}
