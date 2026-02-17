package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Int
import kotlin.Long
import kotlin.collections.List
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Status
import mastodon.api.model.Tag
import mastodon.api.model.TrendsLink
import mastodon.api.model.ValidationError

public class TrendsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View trending links
   */
  public suspend fun getTrendLinks(limit: Long? = 10, offset: Long? = null): GetTrendLinksResponse {
    try {
      val response = configuration.client.`get`("api/v1/trends/links") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (offset != null) {
            parameters.append("offset", offset.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetTrendLinksResponseSuccess(response.body<List<TrendsLink>>())
        401, 404, 429, 503 -> GetTrendLinksResponseFailure401(response.body<Error>())
        410 -> GetTrendLinksResponseFailure410
        422 -> GetTrendLinksResponseFailure(response.body<ValidationError>())
        else -> GetTrendLinksResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTrendLinksResponseUnknownFailure(500)
    }
  }

  /**
   * View trending statuses
   */
  public suspend fun getTrendStatuses(limit: Long? = 20, offset: Long? = null): GetTrendStatusesResponse {
    try {
      val response = configuration.client.`get`("api/v1/trends/statuses") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (offset != null) {
            parameters.append("offset", offset.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetTrendStatusesResponseSuccess(response.body<List<Status>>())
        401, 404, 429, 503 -> GetTrendStatusesResponseFailure401(response.body<Error>())
        410 -> GetTrendStatusesResponseFailure410
        422 -> GetTrendStatusesResponseFailure(response.body<ValidationError>())
        else -> GetTrendStatusesResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTrendStatusesResponseUnknownFailure(500)
    }
  }

  /**
   * View trending tags
   */
  public suspend fun getTrendTags(limit: Long? = 10, offset: Long? = null): GetTrendTagsResponse {
    try {
      val response = configuration.client.`get`("api/v1/trends/tags") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (offset != null) {
            parameters.append("offset", offset.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetTrendTagsResponseSuccess(response.body<List<Tag>>())
        401, 404, 429, 503 -> GetTrendTagsResponseFailure401(response.body<Error>())
        410 -> GetTrendTagsResponseFailure410
        422 -> GetTrendTagsResponseFailure(response.body<ValidationError>())
        else -> GetTrendTagsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTrendTagsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetTrendLinksResponse

  @Serializable
  public data class GetTrendLinksResponseSuccess(
    public val body: List<TrendsLink>,
  ) : GetTrendLinksResponse()

  @Serializable
  public data class GetTrendLinksResponseFailure401(
    public val body: Error,
  ) : GetTrendLinksResponse()

  @Serializable
  public object GetTrendLinksResponseFailure410 : GetTrendLinksResponse()

  @Serializable
  public data class GetTrendLinksResponseFailure(
    public val body: ValidationError,
  ) : GetTrendLinksResponse()

  @Serializable
  public data class GetTrendLinksResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTrendLinksResponse()

  @Serializable
  public sealed class GetTrendStatusesResponse

  @Serializable
  public data class GetTrendStatusesResponseSuccess(
    public val body: List<Status>,
  ) : GetTrendStatusesResponse()

  @Serializable
  public data class GetTrendStatusesResponseFailure401(
    public val body: Error,
  ) : GetTrendStatusesResponse()

  @Serializable
  public object GetTrendStatusesResponseFailure410 : GetTrendStatusesResponse()

  @Serializable
  public data class GetTrendStatusesResponseFailure(
    public val body: ValidationError,
  ) : GetTrendStatusesResponse()

  @Serializable
  public data class GetTrendStatusesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTrendStatusesResponse()

  @Serializable
  public sealed class GetTrendTagsResponse

  @Serializable
  public data class GetTrendTagsResponseSuccess(
    public val body: List<Tag>,
  ) : GetTrendTagsResponse()

  @Serializable
  public data class GetTrendTagsResponseFailure401(
    public val body: Error,
  ) : GetTrendTagsResponse()

  @Serializable
  public object GetTrendTagsResponseFailure410 : GetTrendTagsResponse()

  @Serializable
  public data class GetTrendTagsResponseFailure(
    public val body: ValidationError,
  ) : GetTrendTagsResponse()

  @Serializable
  public data class GetTrendTagsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTrendTagsResponse()
}
