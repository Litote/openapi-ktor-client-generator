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
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Filter
import mastodon.api.model.FilterContextEnum
import mastodon.api.model.FilterKeyword
import mastodon.api.model.FilterStatus
import mastodon.api.model.V1Filter
import mastodon.api.model.ValidationError

public interface FiltersClient {
  /**
   * View your filters
   */
  public suspend fun getFilters(): GetFiltersResponse

  /**
   * Create a filter
   */
  public suspend fun createFilter(request: CreateFilterRequest): CreateFilterResponse

  /**
   * View a single filter
   */
  public suspend fun getFilter(id: String): GetFilterResponse

  /**
   * Update a filter
   */
  public suspend fun updateFilter(request: UpdateFilterRequest, id: String): UpdateFilterResponse

  /**
   * Remove a filter
   */
  public suspend fun deleteFilter(id: String): DeleteFilterResponse

  /**
   * View all filters
   */
  public suspend fun getFiltersV2(): GetFiltersV2Response

  /**
   * Create a filter
   */
  public suspend fun createFilterV2(request: CreateFilterV2Request): CreateFilterV2Response

  /**
   * View keywords added to a filter
   */
  public suspend fun getFilterKeywordsV2(filterId: String): GetFilterKeywordsV2Response

  /**
   * Add a keyword to a filter
   */
  public suspend fun postFilterKeywordsV2(request: PostFilterKeywordsV2Request, filterId: String): PostFilterKeywordsV2Response

  /**
   * View all status filters
   */
  public suspend fun getFilterStatusesV2(filterId: String): GetFilterStatusesV2Response

  /**
   * Add a status to a filter group
   */
  public suspend fun postFilterStatusesV2(request: PostFilterStatusesV2Request, filterId: String): PostFilterStatusesV2Response

  /**
   * View a specific filter
   */
  public suspend fun getFilterV2(id: String): GetFilterV2Response

  /**
   * Update a filter
   */
  public suspend fun updateFilterV2(request: UpdateFilterV2Request, id: String): UpdateFilterV2Response

  /**
   * Delete a filter
   */
  public suspend fun deleteFilterV2(id: String): DeleteFilterV2Response

  /**
   * View a single keyword
   */
  public suspend fun getFiltersKeywordsByIdV2(id: String): GetFiltersKeywordsByIdV2Response

  /**
   * Edit a keyword within a filter
   */
  public suspend fun updateFiltersKeywordsByIdV2(request: UpdateFiltersKeywordsByIdV2Request, id: String): UpdateFiltersKeywordsByIdV2Response

  /**
   * Remove keywords from a filter
   */
  public suspend fun deleteFiltersKeywordsByIdV2(id: String): DeleteFiltersKeywordsByIdV2Response

  /**
   * View a single status filter
   */
  public suspend fun getFiltersStatusesByIdV2(id: String): GetFiltersStatusesByIdV2Response

  /**
   * Remove a status from a filter group
   */
  public suspend fun deleteFiltersStatusesByIdV2(id: String): DeleteFiltersStatusesByIdV2Response

  @Serializable
  public sealed class GetFiltersResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFiltersResponseSuccess(
    public val body: V1Filter,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersResponse() {
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
  public data class GetFiltersResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersResponse()

  @Serializable
  public data class GetFiltersResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersResponse()

  @Serializable
  public data class GetFiltersResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersResponse()

  @Serializable
  public data class GetFiltersResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersResponse()

  @Serializable
  public data class CreateFilterRequest(
    public val context: List<FilterContextEnum>,
    @SerialName("expires_in")
    public val expiresIn: Long? = null,
    public val irreversible: Boolean? = false,
    public val phrase: String,
    @SerialName("whole_word")
    public val wholeWord: Boolean? = false,
  )

  @Serializable
  public sealed class CreateFilterResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateFilterResponseSuccess(
    public val body: V1Filter,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFilterResponse() {
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
  public data class CreateFilterResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFilterResponse()

  @Serializable
  public data class CreateFilterResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFilterResponse()

  @Serializable
  public data class CreateFilterResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFilterResponse()

  @Serializable
  public sealed class GetFilterResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFilterResponseSuccess(
    public val body: V1Filter,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterResponse() {
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
  public data class GetFilterResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterResponse()

  @Serializable
  public data class GetFilterResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterResponse()

  @Serializable
  public data class GetFilterResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterResponse()

  @Serializable
  public data class GetFilterResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterResponse()

  @Serializable
  public data class UpdateFilterRequest(
    public val context: List<FilterContextEnum>,
    @SerialName("expires_in")
    public val expiresIn: Long? = null,
    public val irreversible: Boolean? = false,
    public val phrase: String,
    @SerialName("whole_word")
    public val wholeWord: Boolean? = false,
  )

  @Serializable
  public sealed class UpdateFilterResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class UpdateFilterResponseSuccess(
    public val body: V1Filter,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFilterResponse() {
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
  public data class UpdateFilterResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFilterResponse()

  @Serializable
  public data class UpdateFilterResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFilterResponse()

  @Serializable
  public data class UpdateFilterResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFilterResponse()

  @Serializable
  public sealed class DeleteFilterResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteFilterResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFilterResponse() {
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
  public data class DeleteFilterResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFilterResponse()

  @Serializable
  public data class DeleteFilterResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFilterResponse()

  @Serializable
  public data class DeleteFilterResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFilterResponse()

  @Serializable
  public data class DeleteFilterResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFilterResponse()

  @Serializable
  public sealed class GetFiltersV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFiltersV2ResponseSuccess(
    public val body: List<Filter>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersV2Response() {
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
  public data class GetFiltersV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersV2Response()

  @Serializable
  public data class GetFiltersV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersV2Response()

  @Serializable
  public data class GetFiltersV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersV2Response()

  @Serializable
  public data class GetFiltersV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersV2Response()

  @Serializable
  public data class CreateFilterV2Request(
    public val context: List<FilterContextEnum>,
    @SerialName("expires_in")
    public val expiresIn: Long? = null,
    @SerialName("filter_action")
    public val filterAction: String? = null,
    @SerialName("keywords_attributes")
    public val keywordsAttributes: List<JsonElement>? = null,
    public val title: String,
  )

  @Serializable
  public sealed class CreateFilterV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateFilterV2ResponseSuccess(
    public val body: Filter,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFilterV2Response() {
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
  public data class CreateFilterV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFilterV2Response()

  @Serializable
  public data class CreateFilterV2ResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFilterV2Response()

  @Serializable
  public data class CreateFilterV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateFilterV2Response()

  @Serializable
  public sealed class GetFilterKeywordsV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFilterKeywordsV2ResponseSuccess(
    public val body: List<FilterKeyword>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterKeywordsV2Response() {
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
  public data class GetFilterKeywordsV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterKeywordsV2Response()

  @Serializable
  public data class GetFilterKeywordsV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterKeywordsV2Response()

  @Serializable
  public data class GetFilterKeywordsV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterKeywordsV2Response()

  @Serializable
  public data class GetFilterKeywordsV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterKeywordsV2Response()

  @Serializable
  public data class PostFilterKeywordsV2Request(
    public val keyword: String,
    @SerialName("whole_word")
    public val wholeWord: Boolean? = null,
  )

  @Serializable
  public sealed class PostFilterKeywordsV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostFilterKeywordsV2ResponseSuccess(
    public val body: FilterKeyword,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFilterKeywordsV2Response() {
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
  public data class PostFilterKeywordsV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFilterKeywordsV2Response()

  @Serializable
  public data class PostFilterKeywordsV2ResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFilterKeywordsV2Response()

  @Serializable
  public data class PostFilterKeywordsV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFilterKeywordsV2Response()

  @Serializable
  public sealed class GetFilterStatusesV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFilterStatusesV2ResponseSuccess(
    public val body: List<FilterStatus>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterStatusesV2Response() {
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
  public data class GetFilterStatusesV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterStatusesV2Response()

  @Serializable
  public data class GetFilterStatusesV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterStatusesV2Response()

  @Serializable
  public data class GetFilterStatusesV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterStatusesV2Response()

  @Serializable
  public data class GetFilterStatusesV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterStatusesV2Response()

  @Serializable
  public data class PostFilterStatusesV2Request(
    @SerialName("status_id")
    public val statusId: String,
  )

  @Serializable
  public sealed class PostFilterStatusesV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostFilterStatusesV2ResponseSuccess(
    public val body: FilterStatus,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFilterStatusesV2Response() {
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
  public data class PostFilterStatusesV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFilterStatusesV2Response()

  @Serializable
  public data class PostFilterStatusesV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFilterStatusesV2Response()

  @Serializable
  public data class PostFilterStatusesV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFilterStatusesV2Response()

  @Serializable
  public data class PostFilterStatusesV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostFilterStatusesV2Response()

  @Serializable
  public sealed class GetFilterV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFilterV2ResponseSuccess(
    public val body: Filter,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterV2Response() {
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
  public data class GetFilterV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterV2Response()

  @Serializable
  public data class GetFilterV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterV2Response()

  @Serializable
  public data class GetFilterV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterV2Response()

  @Serializable
  public data class GetFilterV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFilterV2Response()

  @Serializable
  public data class UpdateFilterV2Request(
    public val context: List<FilterContextEnum>? = null,
    @SerialName("expires_in")
    public val expiresIn: Long? = null,
    @SerialName("filter_action")
    public val filterAction: String? = null,
    @SerialName("keywords_attributes")
    public val keywordsAttributes: List<JsonElement>? = null,
    public val title: String? = null,
  )

  @Serializable
  public sealed class UpdateFilterV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class UpdateFilterV2ResponseSuccess(
    public val body: Filter,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFilterV2Response() {
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
  public data class UpdateFilterV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFilterV2Response()

  @Serializable
  public data class UpdateFilterV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFilterV2Response()

  @Serializable
  public data class UpdateFilterV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFilterV2Response()

  @Serializable
  public data class UpdateFilterV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFilterV2Response()

  @Serializable
  public sealed class DeleteFilterV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteFilterV2ResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFilterV2Response() {
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
  public data class DeleteFilterV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFilterV2Response()

  @Serializable
  public data class DeleteFilterV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFilterV2Response()

  @Serializable
  public data class DeleteFilterV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFilterV2Response()

  @Serializable
  public data class DeleteFilterV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFilterV2Response()

  @Serializable
  public sealed class GetFiltersKeywordsByIdV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFiltersKeywordsByIdV2ResponseSuccess(
    public val body: FilterKeyword,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersKeywordsByIdV2Response() {
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
  public data class GetFiltersKeywordsByIdV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersKeywordsByIdV2Response()

  @Serializable
  public data class GetFiltersKeywordsByIdV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersKeywordsByIdV2Response()

  @Serializable
  public data class GetFiltersKeywordsByIdV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersKeywordsByIdV2Response()

  @Serializable
  public data class GetFiltersKeywordsByIdV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersKeywordsByIdV2Response()

  @Serializable
  public data class UpdateFiltersKeywordsByIdV2Request(
    public val keyword: String,
    @SerialName("whole_word")
    public val wholeWord: Boolean? = null,
  )

  @Serializable
  public sealed class UpdateFiltersKeywordsByIdV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class UpdateFiltersKeywordsByIdV2ResponseSuccess(
    public val body: FilterKeyword,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFiltersKeywordsByIdV2Response() {
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
  public data class UpdateFiltersKeywordsByIdV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFiltersKeywordsByIdV2Response()

  @Serializable
  public data class UpdateFiltersKeywordsByIdV2ResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFiltersKeywordsByIdV2Response()

  @Serializable
  public data class UpdateFiltersKeywordsByIdV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateFiltersKeywordsByIdV2Response()

  @Serializable
  public sealed class DeleteFiltersKeywordsByIdV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteFiltersKeywordsByIdV2ResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFiltersKeywordsByIdV2Response() {
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
  public data class DeleteFiltersKeywordsByIdV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFiltersKeywordsByIdV2Response()

  @Serializable
  public data class DeleteFiltersKeywordsByIdV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFiltersKeywordsByIdV2Response()

  @Serializable
  public data class DeleteFiltersKeywordsByIdV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFiltersKeywordsByIdV2Response()

  @Serializable
  public data class DeleteFiltersKeywordsByIdV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFiltersKeywordsByIdV2Response()

  @Serializable
  public sealed class GetFiltersStatusesByIdV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetFiltersStatusesByIdV2ResponseSuccess(
    public val body: FilterStatus,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersStatusesByIdV2Response() {
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
  public data class GetFiltersStatusesByIdV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersStatusesByIdV2Response()

  @Serializable
  public data class GetFiltersStatusesByIdV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersStatusesByIdV2Response()

  @Serializable
  public data class GetFiltersStatusesByIdV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersStatusesByIdV2Response()

  @Serializable
  public data class GetFiltersStatusesByIdV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetFiltersStatusesByIdV2Response()

  @Serializable
  public sealed class DeleteFiltersStatusesByIdV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteFiltersStatusesByIdV2ResponseSuccess(
    public val body: FilterStatus,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFiltersStatusesByIdV2Response() {
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
  public data class DeleteFiltersStatusesByIdV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFiltersStatusesByIdV2Response()

  @Serializable
  public data class DeleteFiltersStatusesByIdV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFiltersStatusesByIdV2Response()

  @Serializable
  public data class DeleteFiltersStatusesByIdV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFiltersStatusesByIdV2Response()

  @Serializable
  public data class DeleteFiltersStatusesByIdV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteFiltersStatusesByIdV2Response()
}

public fun FiltersClient(configuration: ClientConfiguration = defaultClientConfiguration): FiltersClient = DefaultFiltersClient(configuration)

public class DefaultFiltersClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : FiltersClient {
  override suspend fun getFilters(): FiltersClient.GetFiltersResponse {
    try {
      val response = configuration.client.`get`("api/v1/filters") {
      }
      return when (response.status.value) {
        200 -> FiltersClient.GetFiltersResponseSuccess(response.body<V1Filter>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.GetFiltersResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.GetFiltersResponseFailure410(response.headers)
        422 -> FiltersClient.GetFiltersResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.GetFiltersResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.GetFiltersResponseUnknownFailure(500)
    }
  }

  override suspend fun createFilter(request: FiltersClient.CreateFilterRequest): FiltersClient.CreateFilterResponse {
    try {
      val response = configuration.client.post("api/v1/filters") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> FiltersClient.CreateFilterResponseSuccess(response.body<V1Filter>(), response.headers)
        401, 404, 422, 429, 503 -> FiltersClient.CreateFilterResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.CreateFilterResponseFailure(response.headers)
        else -> FiltersClient.CreateFilterResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.CreateFilterResponseUnknownFailure(500)
    }
  }

  override suspend fun getFilter(id: String): FiltersClient.GetFilterResponse {
    try {
      val response = configuration.client.`get`("api/v1/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FiltersClient.GetFilterResponseSuccess(response.body<V1Filter>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.GetFilterResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.GetFilterResponseFailure410(response.headers)
        422 -> FiltersClient.GetFilterResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.GetFilterResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.GetFilterResponseUnknownFailure(500)
    }
  }

  override suspend fun updateFilter(request: FiltersClient.UpdateFilterRequest, id: String): FiltersClient.UpdateFilterResponse {
    try {
      val response = configuration.client.put("api/v1/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> FiltersClient.UpdateFilterResponseSuccess(response.body<V1Filter>(), response.headers)
        401, 404, 422, 429, 503 -> FiltersClient.UpdateFilterResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.UpdateFilterResponseFailure(response.headers)
        else -> FiltersClient.UpdateFilterResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.UpdateFilterResponseUnknownFailure(500)
    }
  }

  override suspend fun deleteFilter(id: String): FiltersClient.DeleteFilterResponse {
    try {
      val response = configuration.client.delete("api/v1/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FiltersClient.DeleteFilterResponseSuccess(response.headers)
        401, 404, 429, 503 -> FiltersClient.DeleteFilterResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.DeleteFilterResponseFailure410(response.headers)
        422 -> FiltersClient.DeleteFilterResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.DeleteFilterResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.DeleteFilterResponseUnknownFailure(500)
    }
  }

  override suspend fun getFiltersV2(): FiltersClient.GetFiltersV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters") {
      }
      return when (response.status.value) {
        200 -> FiltersClient.GetFiltersV2ResponseSuccess(response.body<List<Filter>>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.GetFiltersV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.GetFiltersV2ResponseFailure410(response.headers)
        422 -> FiltersClient.GetFiltersV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.GetFiltersV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.GetFiltersV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun createFilterV2(request: FiltersClient.CreateFilterV2Request): FiltersClient.CreateFilterV2Response {
    try {
      val response = configuration.client.post("api/v2/filters") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> FiltersClient.CreateFilterV2ResponseSuccess(response.body<Filter>(), response.headers)
        401, 404, 422, 429, 503 -> FiltersClient.CreateFilterV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.CreateFilterV2ResponseFailure(response.headers)
        else -> FiltersClient.CreateFilterV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.CreateFilterV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun getFilterKeywordsV2(filterId: String): FiltersClient.GetFilterKeywordsV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters/{filter_id}/keywords".replace("/{filter_id}", "/${filterId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FiltersClient.GetFilterKeywordsV2ResponseSuccess(response.body<List<FilterKeyword>>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.GetFilterKeywordsV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.GetFilterKeywordsV2ResponseFailure410(response.headers)
        422 -> FiltersClient.GetFilterKeywordsV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.GetFilterKeywordsV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.GetFilterKeywordsV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun postFilterKeywordsV2(request: FiltersClient.PostFilterKeywordsV2Request, filterId: String): FiltersClient.PostFilterKeywordsV2Response {
    try {
      val response = configuration.client.post("api/v2/filters/{filter_id}/keywords".replace("/{filter_id}", "/${filterId.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> FiltersClient.PostFilterKeywordsV2ResponseSuccess(response.body<FilterKeyword>(), response.headers)
        401, 404, 422, 429, 503 -> FiltersClient.PostFilterKeywordsV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.PostFilterKeywordsV2ResponseFailure(response.headers)
        else -> FiltersClient.PostFilterKeywordsV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.PostFilterKeywordsV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun getFilterStatusesV2(filterId: String): FiltersClient.GetFilterStatusesV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters/{filter_id}/statuses".replace("/{filter_id}", "/${filterId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FiltersClient.GetFilterStatusesV2ResponseSuccess(response.body<List<FilterStatus>>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.GetFilterStatusesV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.GetFilterStatusesV2ResponseFailure410(response.headers)
        422 -> FiltersClient.GetFilterStatusesV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.GetFilterStatusesV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.GetFilterStatusesV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun postFilterStatusesV2(request: FiltersClient.PostFilterStatusesV2Request, filterId: String): FiltersClient.PostFilterStatusesV2Response {
    try {
      val response = configuration.client.post("api/v2/filters/{filter_id}/statuses".replace("/{filter_id}", "/${filterId.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> FiltersClient.PostFilterStatusesV2ResponseSuccess(response.body<FilterStatus>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.PostFilterStatusesV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.PostFilterStatusesV2ResponseFailure410(response.headers)
        422 -> FiltersClient.PostFilterStatusesV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.PostFilterStatusesV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.PostFilterStatusesV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun getFilterV2(id: String): FiltersClient.GetFilterV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FiltersClient.GetFilterV2ResponseSuccess(response.body<Filter>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.GetFilterV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.GetFilterV2ResponseFailure410(response.headers)
        422 -> FiltersClient.GetFilterV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.GetFilterV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.GetFilterV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun updateFilterV2(request: FiltersClient.UpdateFilterV2Request, id: String): FiltersClient.UpdateFilterV2Response {
    try {
      val response = configuration.client.put("api/v2/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> FiltersClient.UpdateFilterV2ResponseSuccess(response.body<Filter>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.UpdateFilterV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.UpdateFilterV2ResponseFailure410(response.headers)
        422 -> FiltersClient.UpdateFilterV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.UpdateFilterV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.UpdateFilterV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun deleteFilterV2(id: String): FiltersClient.DeleteFilterV2Response {
    try {
      val response = configuration.client.delete("api/v2/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FiltersClient.DeleteFilterV2ResponseSuccess(response.headers)
        401, 404, 429, 503 -> FiltersClient.DeleteFilterV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.DeleteFilterV2ResponseFailure410(response.headers)
        422 -> FiltersClient.DeleteFilterV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.DeleteFilterV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.DeleteFilterV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun getFiltersKeywordsByIdV2(id: String): FiltersClient.GetFiltersKeywordsByIdV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters/keywords/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FiltersClient.GetFiltersKeywordsByIdV2ResponseSuccess(response.body<FilterKeyword>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.GetFiltersKeywordsByIdV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.GetFiltersKeywordsByIdV2ResponseFailure410(response.headers)
        422 -> FiltersClient.GetFiltersKeywordsByIdV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.GetFiltersKeywordsByIdV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.GetFiltersKeywordsByIdV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun updateFiltersKeywordsByIdV2(request: FiltersClient.UpdateFiltersKeywordsByIdV2Request, id: String): FiltersClient.UpdateFiltersKeywordsByIdV2Response {
    try {
      val response = configuration.client.put("api/v2/filters/keywords/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> FiltersClient.UpdateFiltersKeywordsByIdV2ResponseSuccess(response.body<FilterKeyword>(), response.headers)
        401, 404, 422, 429, 503 -> FiltersClient.UpdateFiltersKeywordsByIdV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.UpdateFiltersKeywordsByIdV2ResponseFailure(response.headers)
        else -> FiltersClient.UpdateFiltersKeywordsByIdV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.UpdateFiltersKeywordsByIdV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun deleteFiltersKeywordsByIdV2(id: String): FiltersClient.DeleteFiltersKeywordsByIdV2Response {
    try {
      val response = configuration.client.delete("api/v2/filters/keywords/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FiltersClient.DeleteFiltersKeywordsByIdV2ResponseSuccess(response.headers)
        401, 404, 429, 503 -> FiltersClient.DeleteFiltersKeywordsByIdV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.DeleteFiltersKeywordsByIdV2ResponseFailure410(response.headers)
        422 -> FiltersClient.DeleteFiltersKeywordsByIdV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.DeleteFiltersKeywordsByIdV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.DeleteFiltersKeywordsByIdV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun getFiltersStatusesByIdV2(id: String): FiltersClient.GetFiltersStatusesByIdV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FiltersClient.GetFiltersStatusesByIdV2ResponseSuccess(response.body<FilterStatus>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.GetFiltersStatusesByIdV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.GetFiltersStatusesByIdV2ResponseFailure410(response.headers)
        422 -> FiltersClient.GetFiltersStatusesByIdV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.GetFiltersStatusesByIdV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.GetFiltersStatusesByIdV2ResponseUnknownFailure(500)
    }
  }

  override suspend fun deleteFiltersStatusesByIdV2(id: String): FiltersClient.DeleteFiltersStatusesByIdV2Response {
    try {
      val response = configuration.client.delete("api/v2/filters/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FiltersClient.DeleteFiltersStatusesByIdV2ResponseSuccess(response.body<FilterStatus>(), response.headers)
        401, 404, 429, 503 -> FiltersClient.DeleteFiltersStatusesByIdV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> FiltersClient.DeleteFiltersStatusesByIdV2ResponseFailure410(response.headers)
        422 -> FiltersClient.DeleteFiltersStatusesByIdV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> FiltersClient.DeleteFiltersStatusesByIdV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FiltersClient.DeleteFiltersStatusesByIdV2ResponseUnknownFailure(500)
    }
  }
}
