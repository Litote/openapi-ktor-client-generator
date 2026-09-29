package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.Error
import mastodon.api.model.Relationship
import mastodon.api.model.ValidationError

public interface FollowRequestsClient {
  /**
   * View pending follow requests
   */
  public suspend fun getFollowRequests(
    limit: Long? = 40,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetFollowRequestsResponse

  /**
   * Accept follow request
   */
  public suspend fun postFollowRequestAuthorize(accountId: String): PostFollowRequestAuthorizeResponse

  /**
   * Reject follow request
   */
  public suspend fun postFollowRequestReject(accountId: String): PostFollowRequestRejectResponse

  @Serializable
  public sealed class GetFollowRequestsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFollowRequestsResponseSuccess(
    public val body: List<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFollowRequestsResponse() {
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
  public data class GetFollowRequestsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFollowRequestsResponse()

  @Serializable
  public data class GetFollowRequestsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFollowRequestsResponse()

  @Serializable
  public data class GetFollowRequestsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFollowRequestsResponse()

  @Serializable
  public data class GetFollowRequestsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFollowRequestsResponse()

  @Serializable
  public sealed class PostFollowRequestAuthorizeResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostFollowRequestAuthorizeResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFollowRequestAuthorizeResponse() {
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
  public data class PostFollowRequestAuthorizeResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFollowRequestAuthorizeResponse()

  @Serializable
  public data class PostFollowRequestAuthorizeResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFollowRequestAuthorizeResponse()

  @Serializable
  public data class PostFollowRequestAuthorizeResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFollowRequestAuthorizeResponse()

  @Serializable
  public data class PostFollowRequestAuthorizeResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFollowRequestAuthorizeResponse()

  @Serializable
  public sealed class PostFollowRequestRejectResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostFollowRequestRejectResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFollowRequestRejectResponse() {
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
  public data class PostFollowRequestRejectResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFollowRequestRejectResponse()

  @Serializable
  public data class PostFollowRequestRejectResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFollowRequestRejectResponse()

  @Serializable
  public data class PostFollowRequestRejectResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFollowRequestRejectResponse()

  @Serializable
  public data class PostFollowRequestRejectResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFollowRequestRejectResponse()
}

public fun FollowRequestsClient(configuration: ClientConfiguration = defaultClientConfiguration): FollowRequestsClient = DefaultFollowRequestsClient(configuration)

public class DefaultFollowRequestsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : FollowRequestsClient {
  override suspend fun getFollowRequests(
    limit: Long?,
    maxId: String?,
    sinceId: String?,
  ): FollowRequestsClient.GetFollowRequestsResponse {
    try {
      val response = configuration.client.`get`("api/v1/follow_requests") {
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
        200 -> FollowRequestsClient.GetFollowRequestsResponseSuccess(response.body<List<Account>>(), response.headers)
        401, 404, 429, 503 -> FollowRequestsClient.GetFollowRequestsResponseFailure401(response.body<Error>(), response.headers)
        410 -> FollowRequestsClient.GetFollowRequestsResponseFailure410(response.headers)
        422 -> FollowRequestsClient.GetFollowRequestsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FollowRequestsClient.GetFollowRequestsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FollowRequestsClient.GetFollowRequestsResponseUnknownFailure(500)
    }
  }

  override suspend fun postFollowRequestAuthorize(accountId: String): FollowRequestsClient.PostFollowRequestAuthorizeResponse {
    try {
      val response = configuration.client.post("api/v1/follow_requests/{account_id}/authorize".replace("/{account_id}", "/${accountId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FollowRequestsClient.PostFollowRequestAuthorizeResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 429, 503 -> FollowRequestsClient.PostFollowRequestAuthorizeResponseFailure401(response.body<Error>(), response.headers)
        410 -> FollowRequestsClient.PostFollowRequestAuthorizeResponseFailure410(response.headers)
        422 -> FollowRequestsClient.PostFollowRequestAuthorizeResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FollowRequestsClient.PostFollowRequestAuthorizeResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FollowRequestsClient.PostFollowRequestAuthorizeResponseUnknownFailure(500)
    }
  }

  override suspend fun postFollowRequestReject(accountId: String): FollowRequestsClient.PostFollowRequestRejectResponse {
    try {
      val response = configuration.client.post("api/v1/follow_requests/{account_id}/reject".replace("/{account_id}", "/${accountId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FollowRequestsClient.PostFollowRequestRejectResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 429, 503 -> FollowRequestsClient.PostFollowRequestRejectResponseFailure401(response.body<Error>(), response.headers)
        410 -> FollowRequestsClient.PostFollowRequestRejectResponseFailure410(response.headers)
        422 -> FollowRequestsClient.PostFollowRequestRejectResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FollowRequestsClient.PostFollowRequestRejectResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FollowRequestsClient.PostFollowRequestRejectResponseUnknownFailure(500)
    }
  }
}
