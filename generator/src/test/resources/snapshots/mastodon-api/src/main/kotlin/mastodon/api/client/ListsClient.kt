package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.Error
import mastodon.api.model.ValidationError
import kotlin.collections.List as CollectionsList
import mastodon.api.model.List as ModelList

public class ListsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View your lists
   */
  public suspend fun getLists(): GetListsResponse {
    try {
      val response = configuration.client.`get`("api/v1/lists") {
      }
      return when (response.status.value) {
        200 -> GetListsResponseSuccess(response.body<CollectionsList<ModelList>>())
        401, 404, 429, 503 -> GetListsResponseFailure401(response.body<Error>())
        410 -> GetListsResponseFailure410
        422 -> GetListsResponseFailure(response.body<ValidationError>())
        else -> GetListsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetListsResponseUnknownFailure(500)
    }
  }

  /**
   * Create a list
   */
  public suspend fun createList(request: JsonElement): CreateListResponse {
    try {
      val response = configuration.client.post("api/v1/lists") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateListResponseSuccess(response.body<ModelList>())
        401, 404, 422, 429, 503 -> CreateListResponseFailure401(response.body<Error>())
        410 -> CreateListResponseFailure
        else -> CreateListResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateListResponseUnknownFailure(500)
    }
  }

  /**
   * Show a single list
   */
  public suspend fun getList(id: String): GetListResponse {
    try {
      val response = configuration.client.`get`("api/v1/lists/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetListResponseSuccess(response.body<ModelList>())
        401, 404, 429, 503 -> GetListResponseFailure401(response.body<Error>())
        410 -> GetListResponseFailure410
        422 -> GetListResponseFailure(response.body<ValidationError>())
        else -> GetListResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetListResponseUnknownFailure(500)
    }
  }

  /**
   * Update a list
   */
  public suspend fun updateList(request: JsonElement, id: String): UpdateListResponse {
    try {
      val response = configuration.client.put("api/v1/lists/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> UpdateListResponseSuccess(response.body<ModelList>())
        401, 404, 422, 429, 503 -> UpdateListResponseFailure401(response.body<Error>())
        410 -> UpdateListResponseFailure
        else -> UpdateListResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return UpdateListResponseUnknownFailure(500)
    }
  }

  /**
   * Delete a list
   */
  public suspend fun deleteList(id: String): DeleteListResponse {
    try {
      val response = configuration.client.delete("api/v1/lists/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteListResponseSuccess
        401, 404, 429, 503 -> DeleteListResponseFailure401(response.body<Error>())
        410 -> DeleteListResponseFailure410
        422 -> DeleteListResponseFailure(response.body<ValidationError>())
        else -> DeleteListResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteListResponseUnknownFailure(500)
    }
  }

  /**
   * View accounts in a list
   */
  public suspend fun getListAccounts(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetListAccountsResponse {
    try {
      val response = configuration.client.`get`("api/v1/lists/{id}/accounts".replace("/{id}", "/${id.encodeURLPathPart()}")) {
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
        200 -> GetListAccountsResponseSuccess(response.body<CollectionsList<Account>>())
        401, 404, 429, 503 -> GetListAccountsResponseFailure401(response.body<Error>())
        410 -> GetListAccountsResponseFailure410
        422 -> GetListAccountsResponseFailure(response.body<ValidationError>())
        else -> GetListAccountsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetListAccountsResponseUnknownFailure(500)
    }
  }

  /**
   * Add accounts to a list
   */
  public suspend fun postListAccounts(request: JsonElement, id: String): PostListAccountsResponse {
    try {
      val response = configuration.client.post("api/v1/lists/{id}/accounts".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostListAccountsResponseSuccess
        401, 404, 422, 429, 503 -> PostListAccountsResponseFailure401(response.body<Error>())
        410 -> PostListAccountsResponseFailure
        else -> PostListAccountsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostListAccountsResponseUnknownFailure(500)
    }
  }

  /**
   * Remove accounts from list
   */
  public suspend fun deleteListAccounts(request: JsonElement, id: String): DeleteListAccountsResponse {
    try {
      val response = configuration.client.delete("api/v1/lists/{id}/accounts".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> DeleteListAccountsResponseSuccess
        401, 404, 429, 503 -> DeleteListAccountsResponseFailure401(response.body<Error>())
        410 -> DeleteListAccountsResponseFailure410
        422 -> DeleteListAccountsResponseFailure(response.body<ValidationError>())
        else -> DeleteListAccountsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteListAccountsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetListsResponse

  @Serializable
  public data class GetListsResponseSuccess(
    public val body: CollectionsList<ModelList>,
  ) : GetListsResponse()

  @Serializable
  public data class GetListsResponseFailure401(
    public val body: Error,
  ) : GetListsResponse()

  @Serializable
  public object GetListsResponseFailure410 : GetListsResponse()

  @Serializable
  public data class GetListsResponseFailure(
    public val body: ValidationError,
  ) : GetListsResponse()

  @Serializable
  public data class GetListsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetListsResponse()

  @Serializable
  public sealed class CreateListResponse

  @Serializable
  public data class CreateListResponseSuccess(
    public val body: ModelList,
  ) : CreateListResponse()

  @Serializable
  public data class CreateListResponseFailure401(
    public val body: Error,
  ) : CreateListResponse()

  @Serializable
  public object CreateListResponseFailure : CreateListResponse()

  @Serializable
  public data class CreateListResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateListResponse()

  @Serializable
  public sealed class GetListResponse

  @Serializable
  public data class GetListResponseSuccess(
    public val body: ModelList,
  ) : GetListResponse()

  @Serializable
  public data class GetListResponseFailure401(
    public val body: Error,
  ) : GetListResponse()

  @Serializable
  public object GetListResponseFailure410 : GetListResponse()

  @Serializable
  public data class GetListResponseFailure(
    public val body: ValidationError,
  ) : GetListResponse()

  @Serializable
  public data class GetListResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetListResponse()

  @Serializable
  public sealed class UpdateListResponse

  @Serializable
  public data class UpdateListResponseSuccess(
    public val body: ModelList,
  ) : UpdateListResponse()

  @Serializable
  public data class UpdateListResponseFailure401(
    public val body: Error,
  ) : UpdateListResponse()

  @Serializable
  public object UpdateListResponseFailure : UpdateListResponse()

  @Serializable
  public data class UpdateListResponseUnknownFailure(
    public val statusCode: Int,
  ) : UpdateListResponse()

  @Serializable
  public sealed class DeleteListResponse

  @Serializable
  public object DeleteListResponseSuccess : DeleteListResponse()

  @Serializable
  public data class DeleteListResponseFailure401(
    public val body: Error,
  ) : DeleteListResponse()

  @Serializable
  public object DeleteListResponseFailure410 : DeleteListResponse()

  @Serializable
  public data class DeleteListResponseFailure(
    public val body: ValidationError,
  ) : DeleteListResponse()

  @Serializable
  public data class DeleteListResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteListResponse()

  @Serializable
  public sealed class GetListAccountsResponse

  @Serializable
  public data class GetListAccountsResponseSuccess(
    public val body: CollectionsList<Account>,
  ) : GetListAccountsResponse()

  @Serializable
  public data class GetListAccountsResponseFailure401(
    public val body: Error,
  ) : GetListAccountsResponse()

  @Serializable
  public object GetListAccountsResponseFailure410 : GetListAccountsResponse()

  @Serializable
  public data class GetListAccountsResponseFailure(
    public val body: ValidationError,
  ) : GetListAccountsResponse()

  @Serializable
  public data class GetListAccountsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetListAccountsResponse()

  @Serializable
  public sealed class PostListAccountsResponse

  @Serializable
  public object PostListAccountsResponseSuccess : PostListAccountsResponse()

  @Serializable
  public data class PostListAccountsResponseFailure401(
    public val body: Error,
  ) : PostListAccountsResponse()

  @Serializable
  public object PostListAccountsResponseFailure : PostListAccountsResponse()

  @Serializable
  public data class PostListAccountsResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostListAccountsResponse()

  @Serializable
  public sealed class DeleteListAccountsResponse

  @Serializable
  public object DeleteListAccountsResponseSuccess : DeleteListAccountsResponse()

  @Serializable
  public data class DeleteListAccountsResponseFailure401(
    public val body: Error,
  ) : DeleteListAccountsResponse()

  @Serializable
  public object DeleteListAccountsResponseFailure410 : DeleteListAccountsResponse()

  @Serializable
  public data class DeleteListAccountsResponseFailure(
    public val body: ValidationError,
  ) : DeleteListAccountsResponse()

  @Serializable
  public data class DeleteListAccountsResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteListAccountsResponse()
}
