package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLPathPart
import kotlin.Any
import kotlin.ByteArray
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.MediaAttachment
import mastodon.api.model.ValidationError

public interface MediaClient {
  /**
   * Upload media as an attachment (v1)
   */
  public suspend fun createMedia(form: CreateMediaForm): CreateMediaResponse

  /**
   * Get media attachment
   */
  public suspend fun getMedia(id: String): GetMediaResponse

  /**
   * Update media attachment
   */
  public suspend fun updateMedia(form: UpdateMediaForm, id: String): UpdateMediaResponse

  /**
   * Delete media attachment
   */
  public suspend fun deleteMedia(id: String): DeleteMediaResponse

  /**
   * Upload media as an attachment (async)
   */
  public suspend fun createMediaV2(form: CreateMediaV2Form): CreateMediaV2Response

  public data class CreateMediaForm(
    public val `file`: CreateMediaFormFile,
    public val description: String? = null,
    public val focus: String? = null,
    public val thumbnail: CreateMediaFormFile? = null,
  )

  public data class CreateMediaFormFile(
    public val bytes: ByteArray,
    public val contentType: ContentType,
    public val filename: String = "upload",
  )

  @Serializable
  public sealed class CreateMediaResponse : OperationResponse<MediaAttachment> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class CreateMediaResponseSuccess(
    override val body: MediaAttachment,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaResponse(),
      OperationResponseSuccess<MediaAttachment> {
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
  public data class CreateMediaResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class CreateMediaResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class CreateMediaResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetMediaResponse : OperationResponse<Any?> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetMediaResponseSuccess200(
    override val body: MediaAttachment,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMediaResponse(),
      OperationResponseSuccess<Any?> {
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
  public data class GetMediaResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMediaResponse(),
      OperationResponseSuccess<Any?> {
    override val body: Unit
      get() = Unit

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
  public data class GetMediaResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMediaResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetMediaResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMediaResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetMediaResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMediaResponse(),
      OperationResponseUnknownFailure

  public data class UpdateMediaForm(
    public val description: String? = null,
    public val focus: String? = null,
    public val thumbnail: UpdateMediaFormFile? = null,
  )

  public data class UpdateMediaFormFile(
    public val bytes: ByteArray,
    public val contentType: ContentType,
    public val filename: String = "upload",
  )

  @Serializable
  public sealed class UpdateMediaResponse : OperationResponse<MediaAttachment> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class UpdateMediaResponseSuccess(
    override val body: MediaAttachment,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateMediaResponse(),
      OperationResponseSuccess<MediaAttachment> {
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
  public data class UpdateMediaResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateMediaResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class UpdateMediaResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateMediaResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class UpdateMediaResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateMediaResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class DeleteMediaResponse : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class DeleteMediaResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteMediaResponse(),
      OperationResponseSuccess<Unit> {
    override val body: Unit
      get() = Unit

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
  public data class DeleteMediaResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteMediaResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class DeleteMediaResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteMediaResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class DeleteMediaResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteMediaResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class DeleteMediaResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteMediaResponse(),
      OperationResponseUnknownFailure

  public data class CreateMediaV2Form(
    public val `file`: CreateMediaV2FormFile,
    public val description: String? = null,
    public val focus: String? = null,
    public val thumbnail: CreateMediaV2FormFile? = null,
  )

  public data class CreateMediaV2FormFile(
    public val bytes: ByteArray,
    public val contentType: ContentType,
    public val filename: String = "upload",
  )

  @Serializable
  public sealed class CreateMediaV2Response : OperationResponse<MediaAttachment> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class CreateMediaV2ResponseSuccess(
    override val body: MediaAttachment,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaV2Response(),
      OperationResponseSuccess<MediaAttachment> {
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
  public data class CreateMediaV2ResponseFailure401(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaV2Response(),
      OperationResponseFailure<Error>

  @Serializable
  public data class CreateMediaV2ResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaV2Response(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class CreateMediaV2ResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaV2Response(),
      OperationResponseUnknownFailure
}

public fun MediaClient(configuration: ClientConfiguration = defaultClientConfiguration): MediaClient = DefaultMediaClient(configuration)

public class DefaultMediaClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : MediaClient {
  override suspend fun createMedia(form: MediaClient.CreateMediaForm): MediaClient.CreateMediaResponse {
    try {
      val response = configuration.client.post("api/v1/media") {
        setBody(MultiPartFormDataContent(formData {
        append("file", form.file.bytes, Headers.build {
          append(HttpHeaders.ContentType, form.file.contentType.toString())
          append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"" + form.file.filename + "\"")
        })
        form.description?.let { value ->
          append("description", value)
        }
        form.focus?.let { value ->
          append("focus", value)
        }
        form.thumbnail?.let { value ->
          append("thumbnail", value.bytes, Headers.build {
            append(HttpHeaders.ContentType, value.contentType.toString())
            append(HttpHeaders.ContentDisposition, "form-data; name=\"thumbnail\"; filename=\"" + value.filename + "\"")
          })
        }
        }))
      }
      return when (response.status.value) {
        200 -> MediaClient.CreateMediaResponseSuccess(response.body<MediaAttachment>(), response.headers)
        401, 404, 422, 429, 503 -> MediaClient.CreateMediaResponseFailure401(response.body<Error>(), response.headers)
        410 -> MediaClient.CreateMediaResponseFailure(response.headers)
        else -> MediaClient.CreateMediaResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return MediaClient.CreateMediaResponseUnknownFailure(500)
    }
  }

  override suspend fun getMedia(id: String): MediaClient.GetMediaResponse {
    try {
      val response = configuration.client.`get`("api/v1/media/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> MediaClient.GetMediaResponseSuccess200(response.body<MediaAttachment>(), response.headers)
        206 -> MediaClient.GetMediaResponseSuccess(response.headers)
        401, 404, 422, 429, 503 -> MediaClient.GetMediaResponseFailure401(response.body<Error>(), response.headers)
        410 -> MediaClient.GetMediaResponseFailure(response.headers)
        else -> MediaClient.GetMediaResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return MediaClient.GetMediaResponseUnknownFailure(500)
    }
  }

  override suspend fun updateMedia(form: MediaClient.UpdateMediaForm, id: String): MediaClient.UpdateMediaResponse {
    try {
      val response = configuration.client.put("api/v1/media/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(MultiPartFormDataContent(formData {
        form.description?.let { value ->
          append("description", value)
        }
        form.focus?.let { value ->
          append("focus", value)
        }
        form.thumbnail?.let { value ->
          append("thumbnail", value.bytes, Headers.build {
            append(HttpHeaders.ContentType, value.contentType.toString())
            append(HttpHeaders.ContentDisposition, "form-data; name=\"thumbnail\"; filename=\"" + value.filename + "\"")
          })
        }
        }))
      }
      return when (response.status.value) {
        200 -> MediaClient.UpdateMediaResponseSuccess(response.body<MediaAttachment>(), response.headers)
        401, 404, 422, 429, 503 -> MediaClient.UpdateMediaResponseFailure401(response.body<Error>(), response.headers)
        410 -> MediaClient.UpdateMediaResponseFailure(response.headers)
        else -> MediaClient.UpdateMediaResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return MediaClient.UpdateMediaResponseUnknownFailure(500)
    }
  }

  override suspend fun deleteMedia(id: String): MediaClient.DeleteMediaResponse {
    try {
      val response = configuration.client.delete("api/v1/media/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> MediaClient.DeleteMediaResponseSuccess(response.headers)
        401, 404, 429, 503 -> MediaClient.DeleteMediaResponseFailure401(response.body<Error>(), response.headers)
        410 -> MediaClient.DeleteMediaResponseFailure410(response.headers)
        422 -> MediaClient.DeleteMediaResponseFailure(response.body<ValidationError>(), response.headers)
        else -> MediaClient.DeleteMediaResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return MediaClient.DeleteMediaResponseUnknownFailure(500)
    }
  }

  override suspend fun createMediaV2(form: MediaClient.CreateMediaV2Form): MediaClient.CreateMediaV2Response {
    try {
      val response = configuration.client.post("api/v2/media") {
        setBody(MultiPartFormDataContent(formData {
        append("file", form.file.bytes, Headers.build {
          append(HttpHeaders.ContentType, form.file.contentType.toString())
          append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"" + form.file.filename + "\"")
        })
        form.description?.let { value ->
          append("description", value)
        }
        form.focus?.let { value ->
          append("focus", value)
        }
        form.thumbnail?.let { value ->
          append("thumbnail", value.bytes, Headers.build {
            append(HttpHeaders.ContentType, value.contentType.toString())
            append(HttpHeaders.ContentDisposition, "form-data; name=\"thumbnail\"; filename=\"" + value.filename + "\"")
          })
        }
        }))
      }
      return when (response.status.value) {
        200, 202 -> MediaClient.CreateMediaV2ResponseSuccess(response.body<MediaAttachment>(), response.headers)
        401, 404, 422, 429, 500, 503 -> MediaClient.CreateMediaV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> MediaClient.CreateMediaV2ResponseFailure(response.headers)
        else -> MediaClient.CreateMediaV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return MediaClient.CreateMediaV2ResponseUnknownFailure(500)
    }
  }
}
