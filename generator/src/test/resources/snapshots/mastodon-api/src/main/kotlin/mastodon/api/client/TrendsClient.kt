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
import mastodon.api.model.Tag
import mastodon.api.model.TrendsLink
import mastodon.api.model.ValidationError

public interface TrendsClient {
  /**
   * View trending links
   */
  public suspend fun getTrendLinks(limit: Long? = 10, offset: Long? = null): GetTrendLinksResponse

  /**
   * View trending statuses
   */
  public suspend fun getTrendStatuses(limit: Long? = 20, offset: Long? = null): GetTrendStatusesResponse

  /**
   * View trending tags
   */
  public suspend fun getTrendTags(limit: Long? = 10, offset: Long? = null): GetTrendTagsResponse

  @Serializable
  public sealed class GetTrendLinksResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTrendLinksResponseSuccess(
    public val body: List<TrendsLink>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendLinksResponse() {
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
  public data class GetTrendLinksResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendLinksResponse()

  @Serializable
  public data class GetTrendLinksResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendLinksResponse()

  @Serializable
  public data class GetTrendLinksResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendLinksResponse()

  @Serializable
  public data class GetTrendLinksResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendLinksResponse()

  @Serializable
  public sealed class GetTrendStatusesResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTrendStatusesResponseSuccess(
    public val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendStatusesResponse() {
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
  public data class GetTrendStatusesResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendStatusesResponse()

  @Serializable
  public data class GetTrendStatusesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendStatusesResponse()

  @Serializable
  public data class GetTrendStatusesResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendStatusesResponse()

  @Serializable
  public data class GetTrendStatusesResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendStatusesResponse()

  @Serializable
  public sealed class GetTrendTagsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTrendTagsResponseSuccess(
    public val body: List<Tag>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendTagsResponse() {
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
  public data class GetTrendTagsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendTagsResponse()

  @Serializable
  public data class GetTrendTagsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendTagsResponse()

  @Serializable
  public data class GetTrendTagsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendTagsResponse()

  @Serializable
  public data class GetTrendTagsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTrendTagsResponse()
}

public fun TrendsClient(configuration: ClientConfiguration = defaultClientConfiguration): TrendsClient = DefaultTrendsClient(configuration)

public class DefaultTrendsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : TrendsClient {
  override suspend fun getTrendLinks(limit: Long?, offset: Long?): TrendsClient.GetTrendLinksResponse {
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
        200 -> TrendsClient.GetTrendLinksResponseSuccess(response.body<List<TrendsLink>>(), response.headers)
        401, 404, 429, 503 -> TrendsClient.GetTrendLinksResponseFailure401(response.body<Error>(), response.headers)
        410 -> TrendsClient.GetTrendLinksResponseFailure410(response.headers)
        422 -> TrendsClient.GetTrendLinksResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TrendsClient.GetTrendLinksResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TrendsClient.GetTrendLinksResponseUnknownFailure(500)
    }
  }

  override suspend fun getTrendStatuses(limit: Long?, offset: Long?): TrendsClient.GetTrendStatusesResponse {
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
        200 -> TrendsClient.GetTrendStatusesResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> TrendsClient.GetTrendStatusesResponseFailure401(response.body<Error>(), response.headers)
        410 -> TrendsClient.GetTrendStatusesResponseFailure410(response.headers)
        422 -> TrendsClient.GetTrendStatusesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TrendsClient.GetTrendStatusesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TrendsClient.GetTrendStatusesResponseUnknownFailure(500)
    }
  }

  override suspend fun getTrendTags(limit: Long?, offset: Long?): TrendsClient.GetTrendTagsResponse {
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
        200 -> TrendsClient.GetTrendTagsResponseSuccess(response.body<List<Tag>>(), response.headers)
        401, 404, 429, 503 -> TrendsClient.GetTrendTagsResponseFailure401(response.body<Error>(), response.headers)
        410 -> TrendsClient.GetTrendTagsResponseFailure410(response.headers)
        422 -> TrendsClient.GetTrendTagsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TrendsClient.GetTrendTagsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TrendsClient.GetTrendTagsResponseUnknownFailure(500)
    }
  }
}
