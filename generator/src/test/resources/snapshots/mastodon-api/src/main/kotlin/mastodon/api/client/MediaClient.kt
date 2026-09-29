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
import kotlin.ByteArray
import kotlin.Int
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
  public suspend fun createMedia(form: CreateMediaForm): CreateMediaResponse {
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
        200 -> CreateMediaResponseSuccess(response.body<MediaAttachment>(), response.headers)
        401, 404, 422, 429, 503 -> CreateMediaResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateMediaResponseFailure(response.headers)
        else -> CreateMediaResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetMediaResponseSuccess200(response.body<MediaAttachment>(), response.headers)
        206 -> GetMediaResponseSuccess(response.headers)
        401, 404, 422, 429, 503 -> GetMediaResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetMediaResponseFailure(response.headers)
        else -> GetMediaResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetMediaResponseUnknownFailure(500)
    }
  }

  /**
   * Update media attachment
   */
  public suspend fun updateMedia(form: UpdateMediaForm, id: String): UpdateMediaResponse {
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
        200 -> UpdateMediaResponseSuccess(response.body<MediaAttachment>(), response.headers)
        401, 404, 422, 429, 503 -> UpdateMediaResponseFailure401(response.body<Error>(), response.headers)
        410 -> UpdateMediaResponseFailure(response.headers)
        else -> UpdateMediaResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> DeleteMediaResponseSuccess(response.headers)
        401, 404, 429, 503 -> DeleteMediaResponseFailure401(response.body<Error>(), response.headers)
        410 -> DeleteMediaResponseFailure410(response.headers)
        422 -> DeleteMediaResponseFailure(response.body<ValidationError>(), response.headers)
        else -> DeleteMediaResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteMediaResponseUnknownFailure(500)
    }
  }

  /**
   * Upload media as an attachment (async)
   */
  public suspend fun createMediaV2(form: CreateMediaV2Form): CreateMediaV2Response {
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
        200, 202 -> CreateMediaV2ResponseSuccess(response.body<MediaAttachment>(), response.headers)
        401, 404, 422, 429, 500, 503 -> CreateMediaV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateMediaV2ResponseFailure(response.headers)
        else -> CreateMediaV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateMediaV2ResponseUnknownFailure(500)
    }
  }

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
  public sealed class CreateMediaResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateMediaResponseSuccess(
    public val body: MediaAttachment,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaResponse()

  @Serializable
  public data class CreateMediaResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaResponse()

  @Serializable
  public data class CreateMediaResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaResponse()

  @Serializable
  public sealed class GetMediaResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetMediaResponseSuccess200(
    public val body: MediaAttachment,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMediaResponse() {
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
  ) : GetMediaResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMediaResponse()

  @Serializable
  public data class GetMediaResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMediaResponse()

  @Serializable
  public data class GetMediaResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetMediaResponse()

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
  public sealed class UpdateMediaResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class UpdateMediaResponseSuccess(
    public val body: MediaAttachment,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateMediaResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateMediaResponse()

  @Serializable
  public data class UpdateMediaResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateMediaResponse()

  @Serializable
  public data class UpdateMediaResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateMediaResponse()

  @Serializable
  public sealed class DeleteMediaResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteMediaResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteMediaResponse() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteMediaResponse()

  @Serializable
  public data class DeleteMediaResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteMediaResponse()

  @Serializable
  public data class DeleteMediaResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteMediaResponse()

  @Serializable
  public data class DeleteMediaResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteMediaResponse()

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
  public sealed class CreateMediaV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateMediaV2ResponseSuccess(
    public val body: MediaAttachment,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaV2Response() {
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
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaV2Response()

  @Serializable
  public data class CreateMediaV2ResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaV2Response()

  @Serializable
  public data class CreateMediaV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateMediaV2Response()
}
