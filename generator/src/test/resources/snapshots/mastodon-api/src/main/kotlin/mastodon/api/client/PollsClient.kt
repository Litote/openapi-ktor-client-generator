package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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
        200 -> GetPollResponseSuccess(response.body<Poll>())
        401, 404, 429, 503 -> GetPollResponseFailure401(response.body<Error>())
        410 -> GetPollResponseFailure410
        422 -> GetPollResponseFailure(response.body<ValidationError>())
        else -> GetPollResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetPollResponseUnknownFailure(500)
    }
  }

  /**
   * Vote on a poll
   */
  public suspend fun postPollVotes(request: JsonElement, id: String): PostPollVotesResponse {
    try {
      val response = configuration.client.post("api/v1/polls/{id}/votes".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostPollVotesResponseSuccess(response.body<Poll>())
        401, 404, 422, 429, 503 -> PostPollVotesResponseFailure401(response.body<Error>())
        410 -> PostPollVotesResponseFailure
        else -> PostPollVotesResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostPollVotesResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetPollResponse

  @Serializable
  public data class GetPollResponseSuccess(
    public val body: Poll,
  ) : GetPollResponse()

  @Serializable
  public data class GetPollResponseFailure401(
    public val body: Error,
  ) : GetPollResponse()

  @Serializable
  public object GetPollResponseFailure410 : GetPollResponse()

  @Serializable
  public data class GetPollResponseFailure(
    public val body: ValidationError,
  ) : GetPollResponse()

  @Serializable
  public data class GetPollResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetPollResponse()

  @Serializable
  public sealed class PostPollVotesResponse

  @Serializable
  public data class PostPollVotesResponseSuccess(
    public val body: Poll,
  ) : PostPollVotesResponse()

  @Serializable
  public data class PostPollVotesResponseFailure401(
    public val body: Error,
  ) : PostPollVotesResponse()

  @Serializable
  public object PostPollVotesResponseFailure : PostPollVotesResponse()

  @Serializable
  public data class PostPollVotesResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostPollVotesResponse()
}
