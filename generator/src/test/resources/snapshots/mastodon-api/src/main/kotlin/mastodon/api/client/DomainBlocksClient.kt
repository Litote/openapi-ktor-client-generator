package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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
        200 -> GetDomainBlocksResponseSuccess(response.body<List<String>>())
        401, 404, 429, 503 -> GetDomainBlocksResponseFailure401(response.body<Error>())
        410 -> GetDomainBlocksResponseFailure410
        422 -> GetDomainBlocksResponseFailure(response.body<ValidationError>())
        else -> GetDomainBlocksResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetDomainBlocksResponseUnknownFailure(500)
    }
  }

  /**
   * Block a domain
   */
  public suspend fun createDomainBlock(request: JsonElement): CreateDomainBlockResponse {
    try {
      val response = configuration.client.post("api/v1/domain_blocks") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateDomainBlockResponseSuccess
        401, 404, 422, 429, 503 -> CreateDomainBlockResponseFailure401(response.body<Error>())
        410 -> CreateDomainBlockResponseFailure
        else -> CreateDomainBlockResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateDomainBlockResponseUnknownFailure(500)
    }
  }

  /**
   * Unblock a domain
   */
  public suspend fun deleteDomainBlocks(request: JsonElement): DeleteDomainBlocksResponse {
    try {
      val response = configuration.client.delete("api/v1/domain_blocks") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> DeleteDomainBlocksResponseSuccess
        401, 404, 422, 429, 503 -> DeleteDomainBlocksResponseFailure401(response.body<Error>())
        410 -> DeleteDomainBlocksResponseFailure
        else -> DeleteDomainBlocksResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteDomainBlocksResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetDomainBlocksResponse

  @Serializable
  public data class GetDomainBlocksResponseSuccess(
    public val body: List<String>,
  ) : GetDomainBlocksResponse()

  @Serializable
  public data class GetDomainBlocksResponseFailure401(
    public val body: Error,
  ) : GetDomainBlocksResponse()

  @Serializable
  public object GetDomainBlocksResponseFailure410 : GetDomainBlocksResponse()

  @Serializable
  public data class GetDomainBlocksResponseFailure(
    public val body: ValidationError,
  ) : GetDomainBlocksResponse()

  @Serializable
  public data class GetDomainBlocksResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetDomainBlocksResponse()

  @Serializable
  public sealed class CreateDomainBlockResponse

  @Serializable
  public object CreateDomainBlockResponseSuccess : CreateDomainBlockResponse()

  @Serializable
  public data class CreateDomainBlockResponseFailure401(
    public val body: Error,
  ) : CreateDomainBlockResponse()

  @Serializable
  public object CreateDomainBlockResponseFailure : CreateDomainBlockResponse()

  @Serializable
  public data class CreateDomainBlockResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateDomainBlockResponse()

  @Serializable
  public sealed class DeleteDomainBlocksResponse

  @Serializable
  public object DeleteDomainBlocksResponseSuccess : DeleteDomainBlocksResponse()

  @Serializable
  public data class DeleteDomainBlocksResponseFailure401(
    public val body: Error,
  ) : DeleteDomainBlocksResponse()

  @Serializable
  public object DeleteDomainBlocksResponseFailure : DeleteDomainBlocksResponse()

  @Serializable
  public data class DeleteDomainBlocksResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteDomainBlocksResponse()
}
