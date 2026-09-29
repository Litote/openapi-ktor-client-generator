package response.headers.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Boolean
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import response.headers.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import response.headers.api.model.Item

public class ItemsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  public suspend fun listItems(): ListItemsResponse {
    try {
      val response = configuration.client.`get`("items") {
      }
      return when (response.status.value) {
        200 -> ListItemsResponseSuccess(response.body<List<Item>>(), response.headers)
        429 -> ListItemsResponseFailure(response.headers)
        else -> ListItemsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return ListItemsResponseUnknownFailure(500)
    }
  }

  public suspend fun createItem(request: Item): CreateItemResponse {
    try {
      val response = configuration.client.post("items") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        201, 202 -> CreateItemResponseSuccess(response.body<Item>(), response.headers)
        else -> CreateItemResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateItemResponseUnknownFailure(500)
    }
  }

  public suspend fun deleteItem(id: String): DeleteItemResponse {
    try {
      val response = configuration.client.delete("items/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        204 -> DeleteItemResponseSuccess(response.headers)
        else -> DeleteItemResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteItemResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class ListItemsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class ListItemsResponseSuccess(
    public val body: List<Item>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : ListItemsResponse() {
    public val bodyHeader: String?
      get() = headers["Body"]

    /**
     * Pagination links
     */
    public val link: String?
      get() = headers["Link"]

    public val xBigCounter: Long?
      get() = headers["X-Big-Counter"]?.toLongOrNull()

    public val xCached: Boolean?
      get() = headers["X-Cached"]?.toBooleanStrictOrNull()

    public val xExpiresAt: String?
      get() = headers["X-Expires-At"]

    public val xMode: String?
      get() = headers["X-Mode"]

    public val xScore: Double?
      get() = headers["X-Score"]?.toDoubleOrNull()

    public val xTags: String?
      get() = headers["X-Tags"]

    /**
     * Total number of items
     */
    public val xTotalCount: Int?
      get() = headers["X-Total-Count"]?.toIntOrNull()
  }

  @Serializable
  public data class ListItemsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : ListItemsResponse() {
    public val retryAfter: Int?
      get() = headers["Retry-After"]?.toIntOrNull()
  }

  @Serializable
  public data class ListItemsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : ListItemsResponse()

  @Serializable
  public sealed class CreateItemResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateItemResponseSuccess(
    public val body: Item,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateItemResponse() {
    public val eTag: String?
      get() = headers["ETag"]

    public val location: String?
      get() = headers["Location"]
  }

  @Serializable
  public data class CreateItemResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateItemResponse()

  @Serializable
  public sealed class DeleteItemResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteItemResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteItemResponse() {
    public val headersHeader: String?
      get() = headers["Headers"]

    public val statusCodeHeader: String?
      get() = headers["Status-Code"]
  }

  @Serializable
  public data class DeleteItemResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteItemResponse()
}
