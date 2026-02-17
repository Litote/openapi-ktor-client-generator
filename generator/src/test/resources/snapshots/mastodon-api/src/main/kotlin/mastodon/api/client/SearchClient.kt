package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Search
import mastodon.api.model.ValidationError

public class SearchClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Perform a search
   */
  public suspend fun getSearchV2(
    q: String,
    accountId: String? = null,
    excludeUnreviewed: Boolean? = false,
    following: Boolean? = false,
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    offset: Long? = null,
    resolve: Boolean? = null,
    type: String? = null,
  ): GetSearchV2Response {
    try {
      val response = configuration.client.`get`("api/v2/search") {
        url {
          parameters.append("q", q)
          if (accountId != null) {
            parameters.append("account_id", accountId)
          }
          if (excludeUnreviewed != null) {
            parameters.append("exclude_unreviewed", excludeUnreviewed.toString())
          }
          if (following != null) {
            parameters.append("following", following.toString())
          }
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (minId != null) {
            parameters.append("min_id", minId)
          }
          if (offset != null) {
            parameters.append("offset", offset.toString())
          }
          if (resolve != null) {
            parameters.append("resolve", resolve.toString())
          }
          if (type != null) {
            parameters.append("type", type)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetSearchV2ResponseSuccess(response.body<Search>())
        401, 404, 429, 503 -> GetSearchV2ResponseFailure401(response.body<Error>())
        410 -> GetSearchV2ResponseFailure410
        422 -> GetSearchV2ResponseFailure(response.body<ValidationError>())
        else -> GetSearchV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetSearchV2ResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetSearchV2Response

  @Serializable
  public data class GetSearchV2ResponseSuccess(
    public val body: Search,
  ) : GetSearchV2Response()

  @Serializable
  public data class GetSearchV2ResponseFailure401(
    public val body: Error,
  ) : GetSearchV2Response()

  @Serializable
  public object GetSearchV2ResponseFailure410 : GetSearchV2Response()

  @Serializable
  public data class GetSearchV2ResponseFailure(
    public val body: ValidationError,
  ) : GetSearchV2Response()

  @Serializable
  public data class GetSearchV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetSearchV2Response()
}
