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

public interface TagsClient {
  /**
   * Feature a hashtag
   */
  public suspend fun postTagFeature(id: String): PostTagFeatureResponse

  /**
   * Unfeature a hashtag
   */
  public suspend fun postTagUnfeature(id: String): PostTagUnfeatureResponse

  /**
   * View information about a single tag
   */
  public suspend fun getTagsByName(name: String): GetTagsByNameResponse

  /**
   * Follow a hashtag
   */
  public suspend fun postTagFollow(name: String): PostTagFollowResponse

  /**
   * Unfollow a hashtag
   */
  public suspend fun postTagUnfollow(name: String): PostTagUnfollowResponse

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

public fun TagsClient(configuration: ClientConfiguration = defaultClientConfiguration): TagsClient = DefaultTagsClient(configuration)

public class DefaultTagsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : TagsClient {
  override suspend fun postTagFeature(id: String): TagsClient.PostTagFeatureResponse {
    try {
      val response = configuration.client.post("api/v1/tags/{id}/feature".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> TagsClient.PostTagFeatureResponseSuccess(response.body<Tag>(), response.headers)
        401, 404, 429, 503 -> TagsClient.PostTagFeatureResponseFailure401(response.body<Error>(), response.headers)
        410 -> TagsClient.PostTagFeatureResponseFailure410(response.headers)
        422 -> TagsClient.PostTagFeatureResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TagsClient.PostTagFeatureResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TagsClient.PostTagFeatureResponseUnknownFailure(500)
    }
  }

  override suspend fun postTagUnfeature(id: String): TagsClient.PostTagUnfeatureResponse {
    try {
      val response = configuration.client.post("api/v1/tags/{id}/unfeature".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> TagsClient.PostTagUnfeatureResponseSuccess(response.body<Tag>(), response.headers)
        401, 404, 429, 503 -> TagsClient.PostTagUnfeatureResponseFailure401(response.body<Error>(), response.headers)
        410 -> TagsClient.PostTagUnfeatureResponseFailure410(response.headers)
        422 -> TagsClient.PostTagUnfeatureResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TagsClient.PostTagUnfeatureResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TagsClient.PostTagUnfeatureResponseUnknownFailure(500)
    }
  }

  override suspend fun getTagsByName(name: String): TagsClient.GetTagsByNameResponse {
    try {
      val response = configuration.client.`get`("api/v1/tags/{name}".replace("/{name}", "/${name.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> TagsClient.GetTagsByNameResponseSuccess(response.body<Tag>(), response.headers)
        401, 404, 429, 503 -> TagsClient.GetTagsByNameResponseFailure401(response.body<Error>(), response.headers)
        410 -> TagsClient.GetTagsByNameResponseFailure410(response.headers)
        422 -> TagsClient.GetTagsByNameResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TagsClient.GetTagsByNameResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TagsClient.GetTagsByNameResponseUnknownFailure(500)
    }
  }

  override suspend fun postTagFollow(name: String): TagsClient.PostTagFollowResponse {
    try {
      val response = configuration.client.post("api/v1/tags/{name}/follow".replace("/{name}", "/${name.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> TagsClient.PostTagFollowResponseSuccess(response.body<Tag>(), response.headers)
        401, 404, 422, 429, 503 -> TagsClient.PostTagFollowResponseFailure401(response.body<Error>(), response.headers)
        410 -> TagsClient.PostTagFollowResponseFailure(response.headers)
        else -> TagsClient.PostTagFollowResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TagsClient.PostTagFollowResponseUnknownFailure(500)
    }
  }

  override suspend fun postTagUnfollow(name: String): TagsClient.PostTagUnfollowResponse {
    try {
      val response = configuration.client.post("api/v1/tags/{name}/unfollow".replace("/{name}", "/${name.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> TagsClient.PostTagUnfollowResponseSuccess(response.body<Tag>(), response.headers)
        401, 404, 429, 503 -> TagsClient.PostTagUnfollowResponseFailure401(response.body<Error>(), response.headers)
        410 -> TagsClient.PostTagUnfollowResponseFailure410(response.headers)
        422 -> TagsClient.PostTagUnfollowResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TagsClient.PostTagUnfollowResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TagsClient.PostTagUnfollowResponseUnknownFailure(500)
    }
  }
}
