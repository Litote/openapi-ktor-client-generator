package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.Error
import mastodon.api.model.Suggestion
import mastodon.api.model.ValidationError

public class SuggestionsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View follow suggestions (v1)
   */
  public suspend fun getSuggestions(limit: Long? = 40): GetSuggestionsResponse {
    try {
      val response = configuration.client.`get`("api/v1/suggestions") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetSuggestionsResponseSuccess(response.body<List<Account>>())
        401, 404, 429, 503 -> GetSuggestionsResponseFailure401(response.body<Error>())
        410 -> GetSuggestionsResponseFailure410
        422 -> GetSuggestionsResponseFailure(response.body<ValidationError>())
        else -> GetSuggestionsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetSuggestionsResponseUnknownFailure(500)
    }
  }

  /**
   * Remove a suggestion
   */
  public suspend fun deleteSuggestionsByAccountId(accountId: String): DeleteSuggestionsByAccountIdResponse {
    try {
      val response = configuration.client.delete("api/v1/suggestions/{account_id}".replace("/{account_id}", "/${accountId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteSuggestionsByAccountIdResponseSuccess
        401, 404, 429, 503 -> DeleteSuggestionsByAccountIdResponseFailure401(response.body<Error>())
        410 -> DeleteSuggestionsByAccountIdResponseFailure410
        422 -> DeleteSuggestionsByAccountIdResponseFailure(response.body<ValidationError>())
        else -> DeleteSuggestionsByAccountIdResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteSuggestionsByAccountIdResponseUnknownFailure(500)
    }
  }

  /**
   * View follow suggestions (v2)
   */
  public suspend fun getSuggestionsV2(limit: Long? = 40): GetSuggestionsV2Response {
    try {
      val response = configuration.client.`get`("api/v2/suggestions") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetSuggestionsV2ResponseSuccess(response.body<List<Suggestion>>())
        401, 404, 429, 503 -> GetSuggestionsV2ResponseFailure401(response.body<Error>())
        410 -> GetSuggestionsV2ResponseFailure410
        422 -> GetSuggestionsV2ResponseFailure(response.body<ValidationError>())
        else -> GetSuggestionsV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetSuggestionsV2ResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetSuggestionsResponse

  @Serializable
  public data class GetSuggestionsResponseSuccess(
    public val body: List<Account>,
  ) : GetSuggestionsResponse()

  @Serializable
  public data class GetSuggestionsResponseFailure401(
    public val body: Error,
  ) : GetSuggestionsResponse()

  @Serializable
  public object GetSuggestionsResponseFailure410 : GetSuggestionsResponse()

  @Serializable
  public data class GetSuggestionsResponseFailure(
    public val body: ValidationError,
  ) : GetSuggestionsResponse()

  @Serializable
  public data class GetSuggestionsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetSuggestionsResponse()

  @Serializable
  public sealed class DeleteSuggestionsByAccountIdResponse

  @Serializable
  public object DeleteSuggestionsByAccountIdResponseSuccess : DeleteSuggestionsByAccountIdResponse()

  @Serializable
  public data class DeleteSuggestionsByAccountIdResponseFailure401(
    public val body: Error,
  ) : DeleteSuggestionsByAccountIdResponse()

  @Serializable
  public object DeleteSuggestionsByAccountIdResponseFailure410 : DeleteSuggestionsByAccountIdResponse()

  @Serializable
  public data class DeleteSuggestionsByAccountIdResponseFailure(
    public val body: ValidationError,
  ) : DeleteSuggestionsByAccountIdResponse()

  @Serializable
  public data class DeleteSuggestionsByAccountIdResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteSuggestionsByAccountIdResponse()

  @Serializable
  public sealed class GetSuggestionsV2Response

  @Serializable
  public data class GetSuggestionsV2ResponseSuccess(
    public val body: List<Suggestion>,
  ) : GetSuggestionsV2Response()

  @Serializable
  public data class GetSuggestionsV2ResponseFailure401(
    public val body: Error,
  ) : GetSuggestionsV2Response()

  @Serializable
  public object GetSuggestionsV2ResponseFailure410 : GetSuggestionsV2Response()

  @Serializable
  public data class GetSuggestionsV2ResponseFailure(
    public val body: ValidationError,
  ) : GetSuggestionsV2Response()

  @Serializable
  public data class GetSuggestionsV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetSuggestionsV2Response()
}
