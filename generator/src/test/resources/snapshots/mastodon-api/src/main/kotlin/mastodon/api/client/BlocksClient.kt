package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
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

public interface BlocksClient {
  /**
   * View blocked users
   */
  public suspend fun getBlocks(
    limit: Long? = 40,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetBlocksResponse

  @Serializable
  public sealed class GetBlocksResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetBlocksResponseSuccess(
    public val body: List<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetBlocksResponse() {
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
  public data class GetBlocksResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetBlocksResponse()

  @Serializable
  public data class GetBlocksResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetBlocksResponse()

  @Serializable
  public data class GetBlocksResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetBlocksResponse()

  @Serializable
  public data class GetBlocksResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetBlocksResponse()
}

public fun BlocksClient(configuration: ClientConfiguration = defaultClientConfiguration): BlocksClient = DefaultBlocksClient(configuration)

public class DefaultBlocksClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : BlocksClient {
  override suspend fun getBlocks(
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): BlocksClient.GetBlocksResponse {
    try {
      val response = configuration.client.`get`("api/v1/blocks") {
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
        200 -> BlocksClient.GetBlocksResponseSuccess(response.body<List<Account>>(), response.headers)
        401, 404, 429, 503 -> BlocksClient.GetBlocksResponseFailure401(response.body<Error>(), response.headers)
        410 -> BlocksClient.GetBlocksResponseFailure410(response.headers)
        422 -> BlocksClient.GetBlocksResponseFailure(response.body<ValidationError>(), response.headers)
        else -> BlocksClient.GetBlocksResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return BlocksClient.GetBlocksResponseUnknownFailure(500)
    }
  }
}
