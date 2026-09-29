package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class DirectoryClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View profile directory
   */
  public suspend fun getDirectory(
    limit: Long? = 40,
    local: Boolean? = null,
    offset: Long? = null,
    order: String? = null,
  ): GetDirectoryResponse {
    try {
      val response = configuration.client.`get`("api/v1/directory") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (local != null) {
            parameters.append("local", local.toString())
          }
          if (offset != null) {
            parameters.append("offset", offset.toString())
          }
          if (order != null) {
            parameters.append("order", order)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetDirectoryResponseSuccess(response.body<List<Account>>(), response.headers)
        401, 404, 429, 503 -> GetDirectoryResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetDirectoryResponseFailure410(response.headers)
        422 -> GetDirectoryResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetDirectoryResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetDirectoryResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetDirectoryResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetDirectoryResponseSuccess(
    public val body: List<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDirectoryResponse() {
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
  public data class GetDirectoryResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDirectoryResponse()

  @Serializable
  public data class GetDirectoryResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDirectoryResponse()

  @Serializable
  public data class GetDirectoryResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDirectoryResponse()

  @Serializable
  public data class GetDirectoryResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDirectoryResponse()
}
