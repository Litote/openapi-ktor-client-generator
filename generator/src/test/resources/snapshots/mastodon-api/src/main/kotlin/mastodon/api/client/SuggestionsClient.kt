package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
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
import mastodon.api.model.Suggestion
import mastodon.api.model.ValidationError

public interface SuggestionsClient {
  /**
   * View follow suggestions (v1)
   */
  public suspend fun getSuggestions(limit: Long? = 40): GetSuggestionsResponse

  /**
   * Remove a suggestion
   */
  public suspend fun deleteSuggestionsByAccountId(accountId: String): DeleteSuggestionsByAccountIdResponse

  /**
   * View follow suggestions (v2)
   */
  public suspend fun getSuggestionsV2(limit: Long? = 40): GetSuggestionsV2Response

  @Serializable
  public sealed class GetSuggestionsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetSuggestionsResponseSuccess(
    public val body: List<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSuggestionsResponse() {
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
  public data class GetSuggestionsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSuggestionsResponse()

  @Serializable
  public data class GetSuggestionsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSuggestionsResponse()

  @Serializable
  public data class GetSuggestionsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSuggestionsResponse()

  @Serializable
  public data class GetSuggestionsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSuggestionsResponse()

  @Serializable
  public sealed class DeleteSuggestionsByAccountIdResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteSuggestionsByAccountIdResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteSuggestionsByAccountIdResponse() {
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
  public data class DeleteSuggestionsByAccountIdResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteSuggestionsByAccountIdResponse()

  @Serializable
  public data class DeleteSuggestionsByAccountIdResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteSuggestionsByAccountIdResponse()

  @Serializable
  public data class DeleteSuggestionsByAccountIdResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteSuggestionsByAccountIdResponse()

  @Serializable
  public data class DeleteSuggestionsByAccountIdResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteSuggestionsByAccountIdResponse()

  @Serializable
  public sealed class GetSuggestionsV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetSuggestionsV2ResponseSuccess(
    public val body: List<Suggestion>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSuggestionsV2Response() {
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
  public data class GetSuggestionsV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSuggestionsV2Response()

  @Serializable
  public data class GetSuggestionsV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSuggestionsV2Response()

  @Serializable
  public data class GetSuggestionsV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSuggestionsV2Response()

  @Serializable
  public data class GetSuggestionsV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetSuggestionsV2Response()
}

public fun SuggestionsClient(configuration: ClientConfiguration = defaultClientConfiguration): SuggestionsClient = DefaultSuggestionsClient(configuration)

public class DefaultSuggestionsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : SuggestionsClient {
  override suspend fun getSuggestions(limit: Long?): SuggestionsClient.GetSuggestionsResponse {
    try {
      val response = configuration.client.`get`("api/v1/suggestions") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> SuggestionsClient.GetSuggestionsResponseSuccess(response.body<List<Account>>(), response.headers)
        401, 404, 429, 503 -> SuggestionsClient.GetSuggestionsResponseFailure401(response.body<Error>(), response.headers)
        410 -> SuggestionsClient.GetSuggestionsResponseFailure410(response.headers)
        422 -> SuggestionsClient.GetSuggestionsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> SuggestionsClient.GetSuggestionsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return SuggestionsClient.GetSuggestionsResponseUnknownFailure(500)
    }
  }

  override suspend fun deleteSuggestionsByAccountId(accountId: String): SuggestionsClient.DeleteSuggestionsByAccountIdResponse {
    try {
      val response = configuration.client.delete("api/v1/suggestions/{account_id}".replace("/{account_id}", "/${accountId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> SuggestionsClient.DeleteSuggestionsByAccountIdResponseSuccess(response.headers)
        401, 404, 429, 503 -> SuggestionsClient.DeleteSuggestionsByAccountIdResponseFailure401(response.body<Error>(), response.headers)
        410 -> SuggestionsClient.DeleteSuggestionsByAccountIdResponseFailure410(response.headers)
        422 -> SuggestionsClient.DeleteSuggestionsByAccountIdResponseFailure(response.body<ValidationError>(), response.headers)
        else -> SuggestionsClient.DeleteSuggestionsByAccountIdResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return SuggestionsClient.DeleteSuggestionsByAccountIdResponseUnknownFailure(500)
    }
  }

  override suspend fun getSuggestionsV2(limit: Long?): SuggestionsClient.GetSuggestionsV2Response {
    try {
      val response = configuration.client.`get`("api/v2/suggestions") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> SuggestionsClient.GetSuggestionsV2ResponseSuccess(response.body<List<Suggestion>>(), response.headers)
        401, 404, 429, 503 -> SuggestionsClient.GetSuggestionsV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> SuggestionsClient.GetSuggestionsV2ResponseFailure410(response.headers)
        422 -> SuggestionsClient.GetSuggestionsV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> SuggestionsClient.GetSuggestionsV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return SuggestionsClient.GetSuggestionsV2ResponseUnknownFailure(500)
    }
  }
}
