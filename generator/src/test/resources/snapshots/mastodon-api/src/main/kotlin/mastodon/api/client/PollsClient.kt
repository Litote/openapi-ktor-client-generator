package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Poll
import mastodon.api.model.ValidationError

public class PollsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View a poll
   */
  public suspend fun getPoll(id: String): GetPollResponse {
    try {
      val response = configuration.client.`get`("api/v1/polls/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetPollResponseSuccess(response.body<Poll>(), response.headers)
        401, 404, 429, 503 -> GetPollResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetPollResponseFailure410(response.headers)
        422 -> GetPollResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetPollResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetPollResponseUnknownFailure(500)
    }
  }

  /**
   * Vote on a poll
   */
  public suspend fun postPollVotes(request: PostPollVotesRequest, id: String): PostPollVotesResponse {
    try {
      val response = configuration.client.post("api/v1/polls/{id}/votes".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostPollVotesResponseSuccess(response.body<Poll>(), response.headers)
        401, 404, 422, 429, 503 -> PostPollVotesResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostPollVotesResponseFailure(response.headers)
        else -> PostPollVotesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostPollVotesResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetPollResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetPollResponseSuccess(
    public val body: Poll,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPollResponse() {
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
  public data class GetPollResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPollResponse()

  @Serializable
  public data class GetPollResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPollResponse()

  @Serializable
  public data class GetPollResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPollResponse()

  @Serializable
  public data class GetPollResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPollResponse()

  @Serializable
  public data class PostPollVotesRequest(
    public val choices: List<Long>,
  )

  @Serializable
  public sealed class PostPollVotesResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostPollVotesResponseSuccess(
    public val body: Poll,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostPollVotesResponse() {
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
  public data class PostPollVotesResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostPollVotesResponse()

  @Serializable
  public data class PostPollVotesResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostPollVotesResponse()

  @Serializable
  public data class PostPollVotesResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostPollVotesResponse()
}
