package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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
        200 -> GetFeaturedTagsResponseSuccess(response.body<List<FeaturedTag>>())
        401, 404, 429, 503 -> GetFeaturedTagsResponseFailure401(response.body<Error>())
        410 -> GetFeaturedTagsResponseFailure410
        422 -> GetFeaturedTagsResponseFailure(response.body<ValidationError>())
        else -> GetFeaturedTagsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFeaturedTagsResponseUnknownFailure(500)
    }
  }

  /**
   * Feature a tag
   */
  public suspend fun createFeaturedTag(request: JsonElement): CreateFeaturedTagResponse {
    try {
      val response = configuration.client.post("api/v1/featured_tags") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateFeaturedTagResponseSuccess(response.body<FeaturedTag>())
        401, 404, 422, 429, 503 -> CreateFeaturedTagResponseFailure401(response.body<Error>())
        410 -> CreateFeaturedTagResponseFailure
        else -> CreateFeaturedTagResponseUnknownFailure(response.status.value)
      }
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
        200 -> DeleteFeaturedTagResponseSuccess
        401, 404, 429, 503 -> DeleteFeaturedTagResponseFailure401(response.body<Error>())
        410 -> DeleteFeaturedTagResponseFailure410
        422 -> DeleteFeaturedTagResponseFailure(response.body<ValidationError>())
        else -> DeleteFeaturedTagResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetFeaturedTagSuggestionsResponseSuccess(response.body<List<Tag>>())
        401, 404, 429, 503 -> GetFeaturedTagSuggestionsResponseFailure401(response.body<Error>())
        410 -> GetFeaturedTagSuggestionsResponseFailure410
        422 -> GetFeaturedTagSuggestionsResponseFailure(response.body<ValidationError>())
        else -> GetFeaturedTagSuggestionsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFeaturedTagSuggestionsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetFeaturedTagsResponse

  @Serializable
  public data class GetFeaturedTagsResponseSuccess(
    public val body: List<FeaturedTag>,
  ) : GetFeaturedTagsResponse()

  @Serializable
  public data class GetFeaturedTagsResponseFailure401(
    public val body: Error,
  ) : GetFeaturedTagsResponse()

  @Serializable
  public object GetFeaturedTagsResponseFailure410 : GetFeaturedTagsResponse()

  @Serializable
  public data class GetFeaturedTagsResponseFailure(
    public val body: ValidationError,
  ) : GetFeaturedTagsResponse()

  @Serializable
  public data class GetFeaturedTagsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFeaturedTagsResponse()

  @Serializable
  public sealed class CreateFeaturedTagResponse

  @Serializable
  public data class CreateFeaturedTagResponseSuccess(
    public val body: FeaturedTag,
  ) : CreateFeaturedTagResponse()

  @Serializable
  public data class CreateFeaturedTagResponseFailure401(
    public val body: Error,
  ) : CreateFeaturedTagResponse()

  @Serializable
  public object CreateFeaturedTagResponseFailure : CreateFeaturedTagResponse()

  @Serializable
  public data class CreateFeaturedTagResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateFeaturedTagResponse()

  @Serializable
  public sealed class DeleteFeaturedTagResponse

  @Serializable
  public object DeleteFeaturedTagResponseSuccess : DeleteFeaturedTagResponse()

  @Serializable
  public data class DeleteFeaturedTagResponseFailure401(
    public val body: Error,
  ) : DeleteFeaturedTagResponse()

  @Serializable
  public object DeleteFeaturedTagResponseFailure410 : DeleteFeaturedTagResponse()

  @Serializable
  public data class DeleteFeaturedTagResponseFailure(
    public val body: ValidationError,
  ) : DeleteFeaturedTagResponse()

  @Serializable
  public data class DeleteFeaturedTagResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteFeaturedTagResponse()

  @Serializable
  public sealed class GetFeaturedTagSuggestionsResponse

  @Serializable
  public data class GetFeaturedTagSuggestionsResponseSuccess(
    public val body: List<Tag>,
  ) : GetFeaturedTagSuggestionsResponse()

  @Serializable
  public data class GetFeaturedTagSuggestionsResponseFailure401(
    public val body: Error,
  ) : GetFeaturedTagSuggestionsResponse()

  @Serializable
  public object GetFeaturedTagSuggestionsResponseFailure410 : GetFeaturedTagSuggestionsResponse()

  @Serializable
  public data class GetFeaturedTagSuggestionsResponseFailure(
    public val body: ValidationError,
  ) : GetFeaturedTagSuggestionsResponse()

  @Serializable
  public data class GetFeaturedTagSuggestionsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFeaturedTagSuggestionsResponse()
}
