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

public interface FeaturedTagsClient {
  /**
   * View your featured tags
   */
  public suspend fun getFeaturedTags(): GetFeaturedTagsResponse

  /**
   * Feature a tag
   */
  public suspend fun createFeaturedTag(request: CreateFeaturedTagRequest): CreateFeaturedTagResponse

  /**
   * Unfeature a tag
   */
  public suspend fun deleteFeaturedTag(id: String): DeleteFeaturedTagResponse

  /**
   * View suggested tags to feature
   */
  public suspend fun getFeaturedTagSuggestions(): GetFeaturedTagSuggestionsResponse

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

public fun FeaturedTagsClient(configuration: ClientConfiguration = defaultClientConfiguration): FeaturedTagsClient = DefaultFeaturedTagsClient(configuration)

public class DefaultFeaturedTagsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : FeaturedTagsClient {
  override suspend fun getFeaturedTags(): FeaturedTagsClient.GetFeaturedTagsResponse {
    try {
      val response = configuration.client.`get`("api/v1/featured_tags") {
      }
      return when (response.status.value) {
        200 -> FeaturedTagsClient.GetFeaturedTagsResponseSuccess(response.body<List<FeaturedTag>>(), response.headers)
        401, 404, 429, 503 -> FeaturedTagsClient.GetFeaturedTagsResponseFailure401(response.body<Error>(), response.headers)
        410 -> FeaturedTagsClient.GetFeaturedTagsResponseFailure410(response.headers)
        422 -> FeaturedTagsClient.GetFeaturedTagsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FeaturedTagsClient.GetFeaturedTagsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FeaturedTagsClient.GetFeaturedTagsResponseUnknownFailure(500)
    }
  }

  override suspend fun createFeaturedTag(request: FeaturedTagsClient.CreateFeaturedTagRequest): FeaturedTagsClient.CreateFeaturedTagResponse {
    try {
      val response = configuration.client.post("api/v1/featured_tags") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> FeaturedTagsClient.CreateFeaturedTagResponseSuccess(response.body<FeaturedTag>(), response.headers)
        401, 404, 422, 429, 503 -> FeaturedTagsClient.CreateFeaturedTagResponseFailure401(response.body<Error>(), response.headers)
        410 -> FeaturedTagsClient.CreateFeaturedTagResponseFailure(response.headers)
        else -> FeaturedTagsClient.CreateFeaturedTagResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FeaturedTagsClient.CreateFeaturedTagResponseUnknownFailure(500)
    }
  }

  override suspend fun deleteFeaturedTag(id: String): FeaturedTagsClient.DeleteFeaturedTagResponse {
    try {
      val response = configuration.client.delete("api/v1/featured_tags/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FeaturedTagsClient.DeleteFeaturedTagResponseSuccess(response.headers)
        401, 404, 429, 503 -> FeaturedTagsClient.DeleteFeaturedTagResponseFailure401(response.body<Error>(), response.headers)
        410 -> FeaturedTagsClient.DeleteFeaturedTagResponseFailure410(response.headers)
        422 -> FeaturedTagsClient.DeleteFeaturedTagResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FeaturedTagsClient.DeleteFeaturedTagResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FeaturedTagsClient.DeleteFeaturedTagResponseUnknownFailure(500)
    }
  }

  override suspend fun getFeaturedTagSuggestions(): FeaturedTagsClient.GetFeaturedTagSuggestionsResponse {
    try {
      val response = configuration.client.`get`("api/v1/featured_tags/suggestions") {
      }
      return when (response.status.value) {
        200 -> FeaturedTagsClient.GetFeaturedTagSuggestionsResponseSuccess(response.body<List<Tag>>(), response.headers)
        401, 404, 429, 503 -> FeaturedTagsClient.GetFeaturedTagSuggestionsResponseFailure401(response.body<Error>(), response.headers)
        410 -> FeaturedTagsClient.GetFeaturedTagSuggestionsResponseFailure410(response.headers)
        422 -> FeaturedTagsClient.GetFeaturedTagSuggestionsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FeaturedTagsClient.GetFeaturedTagSuggestionsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FeaturedTagsClient.GetFeaturedTagSuggestionsResponseUnknownFailure(500)
    }
  }
}
