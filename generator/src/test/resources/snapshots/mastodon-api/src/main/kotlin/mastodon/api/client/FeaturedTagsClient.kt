package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.FeaturedTag
import mastodon.api.model.Tag
import mastodon.api.model.ValidationError

public class FeaturedTagsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View your featured tags
   */
  public suspend fun getFeaturedTags(): GetFeaturedTagsResponse {
    try {
      val response = configuration.client.`get`("api/v1/featured_tags") {
      }
      return when (response.status.value) {
        200 -> GetFeaturedTagsResponseSuccess(response.body<List<FeaturedTag>>(), response.headers)
        401, 404, 429, 503 -> GetFeaturedTagsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetFeaturedTagsResponseFailure410(response.headers)
        422 -> GetFeaturedTagsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetFeaturedTagsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFeaturedTagsResponseUnknownFailure(500)
    }
  }

  /**
   * Feature a tag
   */
  public suspend fun createFeaturedTag(request: CreateFeaturedTagRequest): CreateFeaturedTagResponse {
    try {
      val response = configuration.client.post("api/v1/featured_tags") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateFeaturedTagResponseSuccess(response.body<FeaturedTag>(), response.headers)
        401, 404, 422, 429, 503 -> CreateFeaturedTagResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateFeaturedTagResponseFailure(response.headers)
        else -> CreateFeaturedTagResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateFeaturedTagResponseUnknownFailure(500)
    }
  }

  /**
   * Unfeature a tag
   */
  public suspend fun deleteFeaturedTag(id: String): DeleteFeaturedTagResponse {
    try {
      val response = configuration.client.delete("api/v1/featured_tags/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteFeaturedTagResponseSuccess(response.headers)
        401, 404, 429, 503 -> DeleteFeaturedTagResponseFailure401(response.body<Error>(), response.headers)
        410 -> DeleteFeaturedTagResponseFailure410(response.headers)
        422 -> DeleteFeaturedTagResponseFailure(response.body<ValidationError>(), response.headers)
        else -> DeleteFeaturedTagResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteFeaturedTagResponseUnknownFailure(500)
    }
  }

  /**
   * View suggested tags to feature
   */
  public suspend fun getFeaturedTagSuggestions(): GetFeaturedTagSuggestionsResponse {
    try {
      val response = configuration.client.`get`("api/v1/featured_tags/suggestions") {
      }
      return when (response.status.value) {
        200 -> GetFeaturedTagSuggestionsResponseSuccess(response.body<List<Tag>>(), response.headers)
        401, 404, 429, 503 -> GetFeaturedTagSuggestionsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetFeaturedTagSuggestionsResponseFailure410(response.headers)
        422 -> GetFeaturedTagSuggestionsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetFeaturedTagSuggestionsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFeaturedTagSuggestionsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetFeaturedTagsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFeaturedTagsResponseSuccess(
    public val body: List<FeaturedTag>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFeaturedTagsResponse() {
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
  public data class GetFeaturedTagsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFeaturedTagsResponse()

  @Serializable
  public data class GetFeaturedTagsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFeaturedTagsResponse()

  @Serializable
  public data class GetFeaturedTagsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFeaturedTagsResponse()

  @Serializable
  public data class GetFeaturedTagsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFeaturedTagsResponse()

  @Serializable
  public data class CreateFeaturedTagRequest(
    public val name: String,
  )

  @Serializable
  public sealed class CreateFeaturedTagResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateFeaturedTagResponseSuccess(
    public val body: FeaturedTag,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFeaturedTagResponse() {
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
  public data class CreateFeaturedTagResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFeaturedTagResponse()

  @Serializable
  public data class CreateFeaturedTagResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFeaturedTagResponse()

  @Serializable
  public data class CreateFeaturedTagResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFeaturedTagResponse()

  @Serializable
  public sealed class DeleteFeaturedTagResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteFeaturedTagResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFeaturedTagResponse() {
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
  public data class DeleteFeaturedTagResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFeaturedTagResponse()

  @Serializable
  public data class DeleteFeaturedTagResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFeaturedTagResponse()

  @Serializable
  public data class DeleteFeaturedTagResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFeaturedTagResponse()

  @Serializable
  public data class DeleteFeaturedTagResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFeaturedTagResponse()

  @Serializable
  public sealed class GetFeaturedTagSuggestionsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFeaturedTagSuggestionsResponseSuccess(
    public val body: List<Tag>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFeaturedTagSuggestionsResponse() {
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
  public data class GetFeaturedTagSuggestionsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFeaturedTagSuggestionsResponse()

  @Serializable
  public data class GetFeaturedTagSuggestionsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFeaturedTagSuggestionsResponse()

  @Serializable
  public data class GetFeaturedTagSuggestionsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFeaturedTagSuggestionsResponse()

  @Serializable
  public data class GetFeaturedTagSuggestionsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFeaturedTagSuggestionsResponse()
}
