package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Tag
import mastodon.api.model.ValidationError

public class TagsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Feature a hashtag
   */
  public suspend fun postTagFeature(id: String): PostTagFeatureResponse {
    try {
      val response = configuration.client.post("api/v1/tags/{id}/feature".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostTagFeatureResponseSuccess(response.body<Tag>(), response.headers)
        401, 404, 429, 503 -> PostTagFeatureResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostTagFeatureResponseFailure410(response.headers)
        422 -> PostTagFeatureResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostTagFeatureResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostTagFeatureResponseUnknownFailure(500)
    }
  }

  /**
   * Unfeature a hashtag
   */
  public suspend fun postTagUnfeature(id: String): PostTagUnfeatureResponse {
    try {
      val response = configuration.client.post("api/v1/tags/{id}/unfeature".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostTagUnfeatureResponseSuccess(response.body<Tag>(), response.headers)
        401, 404, 429, 503 -> PostTagUnfeatureResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostTagUnfeatureResponseFailure410(response.headers)
        422 -> PostTagUnfeatureResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostTagUnfeatureResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostTagUnfeatureResponseUnknownFailure(500)
    }
  }

  /**
   * View information about a single tag
   */
  public suspend fun getTagsByName(name: String): GetTagsByNameResponse {
    try {
      val response = configuration.client.`get`("api/v1/tags/{name}".replace("/{name}", "/${name.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetTagsByNameResponseSuccess(response.body<Tag>(), response.headers)
        401, 404, 429, 503 -> GetTagsByNameResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetTagsByNameResponseFailure410(response.headers)
        422 -> GetTagsByNameResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetTagsByNameResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTagsByNameResponseUnknownFailure(500)
    }
  }

  /**
   * Follow a hashtag
   */
  public suspend fun postTagFollow(name: String): PostTagFollowResponse {
    try {
      val response = configuration.client.post("api/v1/tags/{name}/follow".replace("/{name}", "/${name.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostTagFollowResponseSuccess(response.body<Tag>(), response.headers)
        401, 404, 422, 429, 503 -> PostTagFollowResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostTagFollowResponseFailure(response.headers)
        else -> PostTagFollowResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostTagFollowResponseUnknownFailure(500)
    }
  }

  /**
   * Unfollow a hashtag
   */
  public suspend fun postTagUnfollow(name: String): PostTagUnfollowResponse {
    try {
      val response = configuration.client.post("api/v1/tags/{name}/unfollow".replace("/{name}", "/${name.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostTagUnfollowResponseSuccess(response.body<Tag>(), response.headers)
        401, 404, 429, 503 -> PostTagUnfollowResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostTagUnfollowResponseFailure410(response.headers)
        422 -> PostTagUnfollowResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostTagUnfollowResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostTagUnfollowResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class PostTagFeatureResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostTagFeatureResponseSuccess(
    public val body: Tag,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagFeatureResponse() {
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
  public data class PostTagFeatureResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagFeatureResponse()

  @Serializable
  public data class PostTagFeatureResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagFeatureResponse()

  @Serializable
  public data class PostTagFeatureResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagFeatureResponse()

  @Serializable
  public data class PostTagFeatureResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagFeatureResponse()

  @Serializable
  public sealed class PostTagUnfeatureResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostTagUnfeatureResponseSuccess(
    public val body: Tag,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagUnfeatureResponse() {
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
  public data class PostTagUnfeatureResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagUnfeatureResponse()

  @Serializable
  public data class PostTagUnfeatureResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagUnfeatureResponse()

  @Serializable
  public data class PostTagUnfeatureResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagUnfeatureResponse()

  @Serializable
  public data class PostTagUnfeatureResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagUnfeatureResponse()

  @Serializable
  public sealed class GetTagsByNameResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTagsByNameResponseSuccess(
    public val body: Tag,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTagsByNameResponse() {
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
  public data class GetTagsByNameResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTagsByNameResponse()

  @Serializable
  public data class GetTagsByNameResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTagsByNameResponse()

  @Serializable
  public data class GetTagsByNameResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTagsByNameResponse()

  @Serializable
  public data class GetTagsByNameResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTagsByNameResponse()

  @Serializable
  public sealed class PostTagFollowResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostTagFollowResponseSuccess(
    public val body: Tag,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagFollowResponse() {
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
  public data class PostTagFollowResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagFollowResponse()

  @Serializable
  public data class PostTagFollowResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagFollowResponse()

  @Serializable
  public data class PostTagFollowResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagFollowResponse()

  @Serializable
  public sealed class PostTagUnfollowResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostTagUnfollowResponseSuccess(
    public val body: Tag,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagUnfollowResponse() {
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
  public data class PostTagUnfollowResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagUnfollowResponse()

  @Serializable
  public data class PostTagUnfollowResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagUnfollowResponse()

  @Serializable
  public data class PostTagUnfollowResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagUnfollowResponse()

  @Serializable
  public data class PostTagUnfollowResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTagUnfollowResponse()
}
