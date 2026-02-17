package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
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
        200 -> GetDirectoryResponseSuccess(response.body<List<Account>>())
        401, 404, 429, 503 -> GetDirectoryResponseFailure401(response.body<Error>())
        410 -> GetDirectoryResponseFailure410
        422 -> GetDirectoryResponseFailure(response.body<ValidationError>())
        else -> GetDirectoryResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetDirectoryResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetDirectoryResponse

  @Serializable
  public data class GetDirectoryResponseSuccess(
    public val body: List<Account>,
  ) : GetDirectoryResponse()

  @Serializable
  public data class GetDirectoryResponseFailure401(
    public val body: Error,
  ) : GetDirectoryResponse()

  @Serializable
  public object GetDirectoryResponseFailure410 : GetDirectoryResponse()

  @Serializable
  public data class GetDirectoryResponseFailure(
    public val body: ValidationError,
  ) : GetDirectoryResponse()

  @Serializable
  public data class GetDirectoryResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetDirectoryResponse()
}
