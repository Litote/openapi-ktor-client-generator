package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Search
import mastodon.api.model.ValidationError

public interface SearchClient {
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
  ): GetSearchV2Response

  @Serializable
  public sealed class GetSearchV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetSearchV2ResponseSuccess(
    public val body: Search,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSearchV2Response() {
    /**
     * Pagination links for browsing older or newer results. Format: Link: <https://mastodon.example/api/v1/endpoint?max_id=7163058>; rel="next", <https://mastodon.example/api/v1/endpoint?min_id=7275607>; rel="prev". See [RFC 8288](https://www.rfc-editor.org/rfc/rfc8288) for more information.
     */
    public val link: String?
      get() = headers["Link"]

    /**
     * Number of requests permitted per time period
     */
    public val xRateLimitLimit: Int?
      get() = headers["X-RateLimit-Limit"]?.toIntOrNull()

    /**
     * Number of requests you can still make
     */
    public val xRateLimitRemaining: Int?
      get() = headers["X-RateLimit-Remaining"]?.toIntOrNull()

    /**
     * Timestamp when your rate limit will reset
     */
    public val xRateLimitReset: String?
      get() = headers["X-RateLimit-Reset"]
  }

  @Serializable
  public data class GetSearchV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSearchV2Response()

  @Serializable
  public data class GetSearchV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSearchV2Response()

  @Serializable
  public data class GetSearchV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSearchV2Response()

  @Serializable
  public data class GetSearchV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSearchV2Response()
}

public fun SearchClient(configuration: ClientConfiguration = defaultClientConfiguration): SearchClient = DefaultSearchClient(configuration)

public class DefaultSearchClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : SearchClient {
  override suspend fun getSearchV2(
    q: String,
    accountId: String?,
    excludeUnreviewed: Boolean?,
    following: Boolean?,
    limit: Long?,
    maxId: String?,
    minId: String?,
    offset: Long?,
    resolve: Boolean?,
    type: String?,
  ): SearchClient.GetSearchV2Response {
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
        200 -> SearchClient.GetSearchV2ResponseSuccess(response.body<Search>(), response.headers)
        401, 404, 429, 503 -> SearchClient.GetSearchV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> SearchClient.GetSearchV2ResponseFailure410(response.headers)
        422 -> SearchClient.GetSearchV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> SearchClient.GetSearchV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return SearchClient.GetSearchV2ResponseUnknownFailure(500)
    }
  }
}
