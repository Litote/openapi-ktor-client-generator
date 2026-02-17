package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.MediaAttachment
import mastodon.api.model.ValidationError

public class MediaClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Upload media as an attachment (v1)
   */
  public suspend fun createMedia(request: JsonElement): CreateMediaResponse {
    try {
      val response = configuration.client.post("api/v1/media") {
        setBody(request)
      }
      return when (response.status.value) {
        200 -> CreateMediaResponseSuccess(response.body<MediaAttachment>())
        401, 404, 422, 429, 503 -> CreateMediaResponseFailure401(response.body<Error>())
        410 -> CreateMediaResponseFailure
        else -> CreateMediaResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateMediaResponseUnknownFailure(500)
    }
  }

  /**
   * Get media attachment
   */
  public suspend fun getMedia(id: String): GetMediaResponse {
    try {
      val response = configuration.client.`get`("api/v1/media/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetMediaResponseSuccess200(response.body<MediaAttachment>())
        206 -> GetMediaResponseSuccess
        401, 404, 422, 429, 503 -> GetMediaResponseFailure401(response.body<Error>())
        410 -> GetMediaResponseFailure
        else -> GetMediaResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetMediaResponseUnknownFailure(500)
    }
  }

  /**
   * Update media attachment
   */
  public suspend fun updateMedia(request: JsonElement, id: String): UpdateMediaResponse {
    try {
      val response = configuration.client.put("api/v1/media/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
      }
      return when (response.status.value) {
        200 -> UpdateMediaResponseSuccess(response.body<MediaAttachment>())
        401, 404, 422, 429, 503 -> UpdateMediaResponseFailure401(response.body<Error>())
        410 -> UpdateMediaResponseFailure
        else -> UpdateMediaResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return UpdateMediaResponseUnknownFailure(500)
    }
  }

  /**
   * Delete media attachment
   */
  public suspend fun deleteMedia(id: String): DeleteMediaResponse {
    try {
      val response = configuration.client.delete("api/v1/media/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteMediaResponseSuccess
        401, 404, 429, 503 -> DeleteMediaResponseFailure401(response.body<Error>())
        410 -> DeleteMediaResponseFailure410
        422 -> DeleteMediaResponseFailure(response.body<ValidationError>())
        else -> DeleteMediaResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteMediaResponseUnknownFailure(500)
    }
  }

  /**
   * Upload media as an attachment (async)
   */
  public suspend fun createMediaV2(request: JsonElement): CreateMediaV2Response {
    try {
      val response = configuration.client.post("api/v2/media") {
        setBody(request)
      }
      return when (response.status.value) {
        200, 202 -> CreateMediaV2ResponseSuccess(response.body<MediaAttachment>())
        401, 404, 422, 429, 500, 503 -> CreateMediaV2ResponseFailure401(response.body<Error>())
        410 -> CreateMediaV2ResponseFailure
        else -> CreateMediaV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateMediaV2ResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class CreateMediaResponse

  @Serializable
  public data class CreateMediaResponseSuccess(
    public val body: MediaAttachment,
  ) : CreateMediaResponse()

  @Serializable
  public data class CreateMediaResponseFailure401(
    public val body: Error,
  ) : CreateMediaResponse()

  @Serializable
  public object CreateMediaResponseFailure : CreateMediaResponse()

  @Serializable
  public data class CreateMediaResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateMediaResponse()

  @Serializable
  public sealed class GetMediaResponse

  @Serializable
  public data class GetMediaResponseSuccess200(
    public val body: MediaAttachment,
  ) : GetMediaResponse()

  @Serializable
  public object GetMediaResponseSuccess : GetMediaResponse()

  @Serializable
  public data class GetMediaResponseFailure401(
    public val body: Error,
  ) : GetMediaResponse()

  @Serializable
  public object GetMediaResponseFailure : GetMediaResponse()

  @Serializable
  public data class GetMediaResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetMediaResponse()

  @Serializable
  public sealed class UpdateMediaResponse

  @Serializable
  public data class UpdateMediaResponseSuccess(
    public val body: MediaAttachment,
  ) : UpdateMediaResponse()

  @Serializable
  public data class UpdateMediaResponseFailure401(
    public val body: Error,
  ) : UpdateMediaResponse()

  @Serializable
  public object UpdateMediaResponseFailure : UpdateMediaResponse()

  @Serializable
  public data class UpdateMediaResponseUnknownFailure(
    public val statusCode: Int,
  ) : UpdateMediaResponse()

  @Serializable
  public sealed class DeleteMediaResponse

  @Serializable
  public object DeleteMediaResponseSuccess : DeleteMediaResponse()

  @Serializable
  public data class DeleteMediaResponseFailure401(
    public val body: Error,
  ) : DeleteMediaResponse()

  @Serializable
  public object DeleteMediaResponseFailure410 : DeleteMediaResponse()

  @Serializable
  public data class DeleteMediaResponseFailure(
    public val body: ValidationError,
  ) : DeleteMediaResponse()

  @Serializable
  public data class DeleteMediaResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteMediaResponse()

  @Serializable
  public sealed class CreateMediaV2Response

  @Serializable
  public data class CreateMediaV2ResponseSuccess(
    public val body: MediaAttachment,
  ) : CreateMediaV2Response()

  @Serializable
  public data class CreateMediaV2ResponseFailure401(
    public val body: Error,
  ) : CreateMediaV2Response()

  @Serializable
  public object CreateMediaV2ResponseFailure : CreateMediaV2Response()

  @Serializable
  public data class CreateMediaV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateMediaV2Response()
}
