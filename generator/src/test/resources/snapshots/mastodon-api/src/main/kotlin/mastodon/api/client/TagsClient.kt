package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlinx.serialization.Serializable
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
        200 -> PostTagFeatureResponseSuccess(response.body<Tag>())
        401, 404, 429, 503 -> PostTagFeatureResponseFailure401(response.body<Error>())
        410 -> PostTagFeatureResponseFailure410
        422 -> PostTagFeatureResponseFailure(response.body<ValidationError>())
        else -> PostTagFeatureResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostTagUnfeatureResponseSuccess(response.body<Tag>())
        401, 404, 429, 503 -> PostTagUnfeatureResponseFailure401(response.body<Error>())
        410 -> PostTagUnfeatureResponseFailure410
        422 -> PostTagUnfeatureResponseFailure(response.body<ValidationError>())
        else -> PostTagUnfeatureResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetTagsByNameResponseSuccess(response.body<Tag>())
        401, 404, 429, 503 -> GetTagsByNameResponseFailure401(response.body<Error>())
        410 -> GetTagsByNameResponseFailure410
        422 -> GetTagsByNameResponseFailure(response.body<ValidationError>())
        else -> GetTagsByNameResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostTagFollowResponseSuccess(response.body<Tag>())
        401, 404, 422, 429, 503 -> PostTagFollowResponseFailure401(response.body<Error>())
        410 -> PostTagFollowResponseFailure
        else -> PostTagFollowResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostTagUnfollowResponseSuccess(response.body<Tag>())
        401, 404, 429, 503 -> PostTagUnfollowResponseFailure401(response.body<Error>())
        410 -> PostTagUnfollowResponseFailure410
        422 -> PostTagUnfollowResponseFailure(response.body<ValidationError>())
        else -> PostTagUnfollowResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostTagUnfollowResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class PostTagFeatureResponse

  @Serializable
  public data class PostTagFeatureResponseSuccess(
    public val body: Tag,
  ) : PostTagFeatureResponse()

  @Serializable
  public data class PostTagFeatureResponseFailure401(
    public val body: Error,
  ) : PostTagFeatureResponse()

  @Serializable
  public object PostTagFeatureResponseFailure410 : PostTagFeatureResponse()

  @Serializable
  public data class PostTagFeatureResponseFailure(
    public val body: ValidationError,
  ) : PostTagFeatureResponse()

  @Serializable
  public data class PostTagFeatureResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostTagFeatureResponse()

  @Serializable
  public sealed class PostTagUnfeatureResponse

  @Serializable
  public data class PostTagUnfeatureResponseSuccess(
    public val body: Tag,
  ) : PostTagUnfeatureResponse()

  @Serializable
  public data class PostTagUnfeatureResponseFailure401(
    public val body: Error,
  ) : PostTagUnfeatureResponse()

  @Serializable
  public object PostTagUnfeatureResponseFailure410 : PostTagUnfeatureResponse()

  @Serializable
  public data class PostTagUnfeatureResponseFailure(
    public val body: ValidationError,
  ) : PostTagUnfeatureResponse()

  @Serializable
  public data class PostTagUnfeatureResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostTagUnfeatureResponse()

  @Serializable
  public sealed class GetTagsByNameResponse

  @Serializable
  public data class GetTagsByNameResponseSuccess(
    public val body: Tag,
  ) : GetTagsByNameResponse()

  @Serializable
  public data class GetTagsByNameResponseFailure401(
    public val body: Error,
  ) : GetTagsByNameResponse()

  @Serializable
  public object GetTagsByNameResponseFailure410 : GetTagsByNameResponse()

  @Serializable
  public data class GetTagsByNameResponseFailure(
    public val body: ValidationError,
  ) : GetTagsByNameResponse()

  @Serializable
  public data class GetTagsByNameResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTagsByNameResponse()

  @Serializable
  public sealed class PostTagFollowResponse

  @Serializable
  public data class PostTagFollowResponseSuccess(
    public val body: Tag,
  ) : PostTagFollowResponse()

  @Serializable
  public data class PostTagFollowResponseFailure401(
    public val body: Error,
  ) : PostTagFollowResponse()

  @Serializable
  public object PostTagFollowResponseFailure : PostTagFollowResponse()

  @Serializable
  public data class PostTagFollowResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostTagFollowResponse()

  @Serializable
  public sealed class PostTagUnfollowResponse

  @Serializable
  public data class PostTagUnfollowResponseSuccess(
    public val body: Tag,
  ) : PostTagUnfollowResponse()

  @Serializable
  public data class PostTagUnfollowResponseFailure401(
    public val body: Error,
  ) : PostTagUnfollowResponse()

  @Serializable
  public object PostTagUnfollowResponseFailure410 : PostTagUnfollowResponse()

  @Serializable
  public data class PostTagUnfollowResponseFailure(
    public val body: ValidationError,
  ) : PostTagUnfollowResponse()

  @Serializable
  public data class PostTagUnfollowResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostTagUnfollowResponse()
}
