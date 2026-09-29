package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Status
import mastodon.api.model.ValidationError

public interface BookmarksClient {
  /**
   * View bookmarked statuses
   */
  public suspend fun getBookmarks(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetBookmarksResponse

  @Serializable
  public sealed class GetBookmarksResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetBookmarksResponseSuccess(
    public val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetBookmarksResponse() {
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
  public data class GetBookmarksResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetBookmarksResponse()

  @Serializable
  public data class GetBookmarksResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetBookmarksResponse()

  @Serializable
  public data class GetBookmarksResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetBookmarksResponse()

  @Serializable
  public data class GetBookmarksResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetBookmarksResponse()
}

public fun BookmarksClient(configuration: ClientConfiguration = defaultClientConfiguration): BookmarksClient = DefaultBookmarksClient(configuration)

public class DefaultBookmarksClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : BookmarksClient {
  override suspend fun getBookmarks(
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): BookmarksClient.GetBookmarksResponse {
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
        200 -> BookmarksClient.GetBookmarksResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> BookmarksClient.GetBookmarksResponseFailure401(response.body<Error>(), response.headers)
        410 -> BookmarksClient.GetBookmarksResponseFailure410(response.headers)
        422 -> BookmarksClient.GetBookmarksResponseFailure(response.body<ValidationError>(), response.headers)
        else -> BookmarksClient.GetBookmarksResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return BookmarksClient.GetBookmarksResponseUnknownFailure(500)
    }
  }
}
