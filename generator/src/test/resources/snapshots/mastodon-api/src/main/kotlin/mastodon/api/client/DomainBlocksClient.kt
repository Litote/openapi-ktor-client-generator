package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class DomainBlocksClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get domain blocks
   */
  public suspend fun getDomainBlocks(
    limit: Long? = 100,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetDomainBlocksResponse {
    try {
      val response = configuration.client.`get`("api/v1/domain_blocks") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (minId != null) {
            parameters.append("min_id", minId)
          }
          if (sinceId != null) {
            parameters.append("since_id", sinceId)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetDomainBlocksResponseSuccess(response.body<List<String>>(), response.headers)
        401, 404, 429, 503 -> GetDomainBlocksResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetDomainBlocksResponseFailure410(response.headers)
        422 -> GetDomainBlocksResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetDomainBlocksResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetDomainBlocksResponseUnknownFailure(500)
    }
  }

  /**
   * Block a domain
   */
  public suspend fun createDomainBlock(request: CreateDomainBlockRequest): CreateDomainBlockResponse {
    try {
      val response = configuration.client.post("api/v1/domain_blocks") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateDomainBlockResponseSuccess(response.headers)
        401, 404, 422, 429, 503 -> CreateDomainBlockResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateDomainBlockResponseFailure(response.headers)
        else -> CreateDomainBlockResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateDomainBlockResponseUnknownFailure(500)
    }
  }

  /**
   * Unblock a domain
   */
  public suspend fun deleteDomainBlocks(request: DeleteDomainBlocksRequest): DeleteDomainBlocksResponse {
    try {
      val response = configuration.client.delete("api/v1/domain_blocks") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> DeleteDomainBlocksResponseSuccess(response.headers)
        401, 404, 422, 429, 503 -> DeleteDomainBlocksResponseFailure401(response.body<Error>(), response.headers)
        410 -> DeleteDomainBlocksResponseFailure(response.headers)
        else -> DeleteDomainBlocksResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteDomainBlocksResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetDomainBlocksResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetDomainBlocksResponseSuccess(
    public val body: List<String>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDomainBlocksResponse() {
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
  public data class GetDomainBlocksResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDomainBlocksResponse()

  @Serializable
  public data class GetDomainBlocksResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDomainBlocksResponse()

  @Serializable
  public data class GetDomainBlocksResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDomainBlocksResponse()

  @Serializable
  public data class GetDomainBlocksResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDomainBlocksResponse()

  @Serializable
  public data class CreateDomainBlockRequest(
    public val domain: String,
  )

  @Serializable
  public sealed class CreateDomainBlockResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateDomainBlockResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateDomainBlockResponse() {
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
  public data class CreateDomainBlockResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateDomainBlockResponse()

  @Serializable
  public data class CreateDomainBlockResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateDomainBlockResponse()

  @Serializable
  public data class CreateDomainBlockResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateDomainBlockResponse()

  @Serializable
  public data class DeleteDomainBlocksRequest(
    public val domain: String,
  )

  @Serializable
  public sealed class DeleteDomainBlocksResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteDomainBlocksResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteDomainBlocksResponse() {
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
  public data class DeleteDomainBlocksResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteDomainBlocksResponse()

  @Serializable
  public data class DeleteDomainBlocksResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteDomainBlocksResponse()

  @Serializable
  public data class DeleteDomainBlocksResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteDomainBlocksResponse()
}
