package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.Error
import mastodon.api.model.ListRepliesPolicyEnum
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
        200 -> GetListsResponseSuccess(response.body<CollectionsList<ModelList>>(), response.headers)
        401, 404, 429, 503 -> GetListsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetListsResponseFailure410(response.headers)
        422 -> GetListsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetListsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetListsResponseUnknownFailure(500)
    }
  }

  /**
   * Create a list
   */
  public suspend fun createList(request: CreateListRequest): CreateListResponse {
    try {
      val response = configuration.client.post("api/v1/lists") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateListResponseSuccess(response.body<ModelList>(), response.headers)
        401, 404, 422, 429, 503 -> CreateListResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateListResponseFailure(response.headers)
        else -> CreateListResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetListResponseSuccess(response.body<ModelList>(), response.headers)
        401, 404, 429, 503 -> GetListResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetListResponseFailure410(response.headers)
        422 -> GetListResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetListResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetListResponseUnknownFailure(500)
    }
  }

  /**
   * Update a list
   */
  public suspend fun updateList(request: UpdateListRequest, id: String): UpdateListResponse {
    try {
      val response = configuration.client.put("api/v1/lists/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> UpdateListResponseSuccess(response.body<ModelList>(), response.headers)
        401, 404, 422, 429, 503 -> UpdateListResponseFailure401(response.body<Error>(), response.headers)
        410 -> UpdateListResponseFailure(response.headers)
        else -> UpdateListResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> DeleteListResponseSuccess(response.headers)
        401, 404, 429, 503 -> DeleteListResponseFailure401(response.body<Error>(), response.headers)
        410 -> DeleteListResponseFailure410(response.headers)
        422 -> DeleteListResponseFailure(response.body<ValidationError>(), response.headers)
        else -> DeleteListResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetListAccountsResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> GetListAccountsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetListAccountsResponseFailure410(response.headers)
        422 -> GetListAccountsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetListAccountsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetListAccountsResponseUnknownFailure(500)
    }
  }

  /**
   * Add accounts to a list
   */
  public suspend fun postListAccounts(request: PostListAccountsRequest, id: String): PostListAccountsResponse {
    try {
      val response = configuration.client.post("api/v1/lists/{id}/accounts".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostListAccountsResponseSuccess(response.headers)
        401, 404, 422, 429, 503 -> PostListAccountsResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostListAccountsResponseFailure(response.headers)
        else -> PostListAccountsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostListAccountsResponseUnknownFailure(500)
    }
  }

  /**
   * Remove accounts from list
   */
  public suspend fun deleteListAccounts(request: DeleteListAccountsRequest, id: String): DeleteListAccountsResponse {
    try {
      val response = configuration.client.delete("api/v1/lists/{id}/accounts".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> DeleteListAccountsResponseSuccess(response.headers)
        401, 404, 429, 503 -> DeleteListAccountsResponseFailure401(response.body<Error>(), response.headers)
        410 -> DeleteListAccountsResponseFailure410(response.headers)
        422 -> DeleteListAccountsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> DeleteListAccountsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteListAccountsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetListsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetListsResponseSuccess(
    public val body: CollectionsList<ModelList>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListsResponse() {
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
  public data class GetListsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListsResponse()

  @Serializable
  public data class GetListsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListsResponse()

  @Serializable
  public data class GetListsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListsResponse()

  @Serializable
  public data class GetListsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListsResponse()

  @Serializable
  public data class CreateListRequest(
    public val exclusive: Boolean? = null,
    @SerialName("replies_policy")
    public val repliesPolicy: ListRepliesPolicyEnum? = null,
    public val title: String,
  )

  @Serializable
  public sealed class CreateListResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateListResponseSuccess(
    public val body: ModelList,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateListResponse() {
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
  public data class CreateListResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateListResponse()

  @Serializable
  public data class CreateListResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateListResponse()

  @Serializable
  public data class CreateListResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateListResponse()

  @Serializable
  public sealed class GetListResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetListResponseSuccess(
    public val body: ModelList,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListResponse() {
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
  public data class GetListResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListResponse()

  @Serializable
  public data class GetListResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListResponse()

  @Serializable
  public data class GetListResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListResponse()

  @Serializable
  public data class GetListResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListResponse()

  @Serializable
  public data class UpdateListRequest(
    public val exclusive: Boolean? = null,
    @SerialName("replies_policy")
    public val repliesPolicy: ListRepliesPolicyEnum? = null,
    public val title: String,
  )

  @Serializable
  public sealed class UpdateListResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class UpdateListResponseSuccess(
    public val body: ModelList,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateListResponse() {
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
  public data class UpdateListResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateListResponse()

  @Serializable
  public data class UpdateListResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateListResponse()

  @Serializable
  public data class UpdateListResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateListResponse()

  @Serializable
  public sealed class DeleteListResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteListResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteListResponse() {
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
  public data class DeleteListResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteListResponse()

  @Serializable
  public data class DeleteListResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteListResponse()

  @Serializable
  public data class DeleteListResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteListResponse()

  @Serializable
  public data class DeleteListResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteListResponse()

  @Serializable
  public sealed class GetListAccountsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetListAccountsResponseSuccess(
    public val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListAccountsResponse() {
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
  public data class GetListAccountsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListAccountsResponse()

  @Serializable
  public data class GetListAccountsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListAccountsResponse()

  @Serializable
  public data class GetListAccountsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListAccountsResponse()

  @Serializable
  public data class GetListAccountsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetListAccountsResponse()

  @Serializable
  public data class PostListAccountsRequest(
    @SerialName("account_ids")
    public val accountIds: CollectionsList<String>,
  )

  @Serializable
  public sealed class PostListAccountsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostListAccountsResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostListAccountsResponse() {
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
  public data class PostListAccountsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostListAccountsResponse()

  @Serializable
  public data class PostListAccountsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostListAccountsResponse()

  @Serializable
  public data class PostListAccountsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostListAccountsResponse()

  @Serializable
  public data class DeleteListAccountsRequest(
    @SerialName("account_ids")
    public val accountIds: CollectionsList<String>,
  )

  @Serializable
  public sealed class DeleteListAccountsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteListAccountsResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteListAccountsResponse() {
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
  public data class DeleteListAccountsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteListAccountsResponse()

  @Serializable
  public data class DeleteListAccountsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteListAccountsResponse()

  @Serializable
  public data class DeleteListAccountsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteListAccountsResponse()

  @Serializable
  public data class DeleteListAccountsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteListAccountsResponse()
}
