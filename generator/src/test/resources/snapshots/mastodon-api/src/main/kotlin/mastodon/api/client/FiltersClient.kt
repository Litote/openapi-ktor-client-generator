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
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Filter
import mastodon.api.model.FilterKeyword
import mastodon.api.model.FilterStatus
import mastodon.api.model.V1Filter
import mastodon.api.model.ValidationError

public class FiltersClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View your filters
   */
  public suspend fun getFilters(): GetFiltersResponse {
    try {
      val response = configuration.client.`get`("api/v1/filters") {
      }
      return when (response.status.value) {
        200 -> GetFiltersResponseSuccess(response.body<V1Filter>())
        401, 404, 429, 503 -> GetFiltersResponseFailure401(response.body<Error>())
        410 -> GetFiltersResponseFailure410
        422 -> GetFiltersResponseFailure(response.body<ValidationError>())
        else -> GetFiltersResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFiltersResponseUnknownFailure(500)
    }
  }

  /**
   * Create a filter
   */
  public suspend fun createFilter(request: JsonElement): CreateFilterResponse {
    try {
      val response = configuration.client.post("api/v1/filters") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateFilterResponseSuccess(response.body<V1Filter>())
        401, 404, 422, 429, 503 -> CreateFilterResponseFailure401(response.body<Error>())
        410 -> CreateFilterResponseFailure
        else -> CreateFilterResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateFilterResponseUnknownFailure(500)
    }
  }

  /**
   * View a single filter
   */
  public suspend fun getFilter(id: String): GetFilterResponse {
    try {
      val response = configuration.client.`get`("api/v1/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetFilterResponseSuccess(response.body<V1Filter>())
        401, 404, 429, 503 -> GetFilterResponseFailure401(response.body<Error>())
        410 -> GetFilterResponseFailure410
        422 -> GetFilterResponseFailure(response.body<ValidationError>())
        else -> GetFilterResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFilterResponseUnknownFailure(500)
    }
  }

  /**
   * Update a filter
   */
  public suspend fun updateFilter(request: JsonElement, id: String): UpdateFilterResponse {
    try {
      val response = configuration.client.put("api/v1/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> UpdateFilterResponseSuccess(response.body<V1Filter>())
        401, 404, 422, 429, 503 -> UpdateFilterResponseFailure401(response.body<Error>())
        410 -> UpdateFilterResponseFailure
        else -> UpdateFilterResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return UpdateFilterResponseUnknownFailure(500)
    }
  }

  /**
   * Remove a filter
   */
  public suspend fun deleteFilter(id: String): DeleteFilterResponse {
    try {
      val response = configuration.client.delete("api/v1/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteFilterResponseSuccess
        401, 404, 429, 503 -> DeleteFilterResponseFailure401(response.body<Error>())
        410 -> DeleteFilterResponseFailure410
        422 -> DeleteFilterResponseFailure(response.body<ValidationError>())
        else -> DeleteFilterResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteFilterResponseUnknownFailure(500)
    }
  }

  /**
   * View all filters
   */
  public suspend fun getFiltersV2(): GetFiltersV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters") {
      }
      return when (response.status.value) {
        200 -> GetFiltersV2ResponseSuccess(response.body<List<Filter>>())
        401, 404, 429, 503 -> GetFiltersV2ResponseFailure401(response.body<Error>())
        410 -> GetFiltersV2ResponseFailure410
        422 -> GetFiltersV2ResponseFailure(response.body<ValidationError>())
        else -> GetFiltersV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFiltersV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Create a filter
   */
  public suspend fun createFilterV2(request: JsonElement): CreateFilterV2Response {
    try {
      val response = configuration.client.post("api/v2/filters") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateFilterV2ResponseSuccess(response.body<Filter>())
        401, 404, 422, 429, 503 -> CreateFilterV2ResponseFailure401(response.body<Error>())
        410 -> CreateFilterV2ResponseFailure
        else -> CreateFilterV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateFilterV2ResponseUnknownFailure(500)
    }
  }

  /**
   * View keywords added to a filter
   */
  public suspend fun getFilterKeywordsV2(filterId: String): GetFilterKeywordsV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters/{filter_id}/keywords".replace("/{filter_id}", "/${filterId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetFilterKeywordsV2ResponseSuccess(response.body<List<FilterKeyword>>())
        401, 404, 429, 503 -> GetFilterKeywordsV2ResponseFailure401(response.body<Error>())
        410 -> GetFilterKeywordsV2ResponseFailure410
        422 -> GetFilterKeywordsV2ResponseFailure(response.body<ValidationError>())
        else -> GetFilterKeywordsV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFilterKeywordsV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Add a keyword to a filter
   */
  public suspend fun postFilterKeywordsV2(request: JsonElement, filterId: String): PostFilterKeywordsV2Response {
    try {
      val response = configuration.client.post("api/v2/filters/{filter_id}/keywords".replace("/{filter_id}", "/${filterId.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostFilterKeywordsV2ResponseSuccess(response.body<FilterKeyword>())
        401, 404, 422, 429, 503 -> PostFilterKeywordsV2ResponseFailure401(response.body<Error>())
        410 -> PostFilterKeywordsV2ResponseFailure
        else -> PostFilterKeywordsV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostFilterKeywordsV2ResponseUnknownFailure(500)
    }
  }

  /**
   * View all status filters
   */
  public suspend fun getFilterStatusesV2(filterId: String): GetFilterStatusesV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters/{filter_id}/statuses".replace("/{filter_id}", "/${filterId.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetFilterStatusesV2ResponseSuccess(response.body<List<FilterStatus>>())
        401, 404, 429, 503 -> GetFilterStatusesV2ResponseFailure401(response.body<Error>())
        410 -> GetFilterStatusesV2ResponseFailure410
        422 -> GetFilterStatusesV2ResponseFailure(response.body<ValidationError>())
        else -> GetFilterStatusesV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFilterStatusesV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Add a status to a filter group
   */
  public suspend fun postFilterStatusesV2(request: JsonElement, filterId: String): PostFilterStatusesV2Response {
    try {
      val response = configuration.client.post("api/v2/filters/{filter_id}/statuses".replace("/{filter_id}", "/${filterId.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostFilterStatusesV2ResponseSuccess(response.body<FilterStatus>())
        401, 404, 429, 503 -> PostFilterStatusesV2ResponseFailure401(response.body<Error>())
        410 -> PostFilterStatusesV2ResponseFailure410
        422 -> PostFilterStatusesV2ResponseFailure(response.body<ValidationError>())
        else -> PostFilterStatusesV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostFilterStatusesV2ResponseUnknownFailure(500)
    }
  }

  /**
   * View a specific filter
   */
  public suspend fun getFilterV2(id: String): GetFilterV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetFilterV2ResponseSuccess(response.body<Filter>())
        401, 404, 429, 503 -> GetFilterV2ResponseFailure401(response.body<Error>())
        410 -> GetFilterV2ResponseFailure410
        422 -> GetFilterV2ResponseFailure(response.body<ValidationError>())
        else -> GetFilterV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFilterV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Update a filter
   */
  public suspend fun updateFilterV2(request: JsonElement, id: String): UpdateFilterV2Response {
    try {
      val response = configuration.client.put("api/v2/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> UpdateFilterV2ResponseSuccess(response.body<Filter>())
        401, 404, 429, 503 -> UpdateFilterV2ResponseFailure401(response.body<Error>())
        410 -> UpdateFilterV2ResponseFailure410
        422 -> UpdateFilterV2ResponseFailure(response.body<ValidationError>())
        else -> UpdateFilterV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return UpdateFilterV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Delete a filter
   */
  public suspend fun deleteFilterV2(id: String): DeleteFilterV2Response {
    try {
      val response = configuration.client.delete("api/v2/filters/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteFilterV2ResponseSuccess
        401, 404, 429, 503 -> DeleteFilterV2ResponseFailure401(response.body<Error>())
        410 -> DeleteFilterV2ResponseFailure410
        422 -> DeleteFilterV2ResponseFailure(response.body<ValidationError>())
        else -> DeleteFilterV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteFilterV2ResponseUnknownFailure(500)
    }
  }

  /**
   * View a single keyword
   */
  public suspend fun getFiltersKeywordsByIdV2(id: String): GetFiltersKeywordsByIdV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters/keywords/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetFiltersKeywordsByIdV2ResponseSuccess(response.body<FilterKeyword>())
        401, 404, 429, 503 -> GetFiltersKeywordsByIdV2ResponseFailure401(response.body<Error>())
        410 -> GetFiltersKeywordsByIdV2ResponseFailure410
        422 -> GetFiltersKeywordsByIdV2ResponseFailure(response.body<ValidationError>())
        else -> GetFiltersKeywordsByIdV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFiltersKeywordsByIdV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Edit a keyword within a filter
   */
  public suspend fun updateFiltersKeywordsByIdV2(request: JsonElement, id: String): UpdateFiltersKeywordsByIdV2Response {
    try {
      val response = configuration.client.put("api/v2/filters/keywords/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> UpdateFiltersKeywordsByIdV2ResponseSuccess(response.body<FilterKeyword>())
        401, 404, 422, 429, 503 -> UpdateFiltersKeywordsByIdV2ResponseFailure401(response.body<Error>())
        410 -> UpdateFiltersKeywordsByIdV2ResponseFailure
        else -> UpdateFiltersKeywordsByIdV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return UpdateFiltersKeywordsByIdV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Remove keywords from a filter
   */
  public suspend fun deleteFiltersKeywordsByIdV2(id: String): DeleteFiltersKeywordsByIdV2Response {
    try {
      val response = configuration.client.delete("api/v2/filters/keywords/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteFiltersKeywordsByIdV2ResponseSuccess
        401, 404, 429, 503 -> DeleteFiltersKeywordsByIdV2ResponseFailure401(response.body<Error>())
        410 -> DeleteFiltersKeywordsByIdV2ResponseFailure410
        422 -> DeleteFiltersKeywordsByIdV2ResponseFailure(response.body<ValidationError>())
        else -> DeleteFiltersKeywordsByIdV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteFiltersKeywordsByIdV2ResponseUnknownFailure(500)
    }
  }

  /**
   * View a single status filter
   */
  public suspend fun getFiltersStatusesByIdV2(id: String): GetFiltersStatusesByIdV2Response {
    try {
      val response = configuration.client.`get`("api/v2/filters/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetFiltersStatusesByIdV2ResponseSuccess(response.body<FilterStatus>())
        401, 404, 429, 503 -> GetFiltersStatusesByIdV2ResponseFailure401(response.body<Error>())
        410 -> GetFiltersStatusesByIdV2ResponseFailure410
        422 -> GetFiltersStatusesByIdV2ResponseFailure(response.body<ValidationError>())
        else -> GetFiltersStatusesByIdV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetFiltersStatusesByIdV2ResponseUnknownFailure(500)
    }
  }

  /**
   * Remove a status from a filter group
   */
  public suspend fun deleteFiltersStatusesByIdV2(id: String): DeleteFiltersStatusesByIdV2Response {
    try {
      val response = configuration.client.delete("api/v2/filters/statuses/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteFiltersStatusesByIdV2ResponseSuccess(response.body<FilterStatus>())
        401, 404, 429, 503 -> DeleteFiltersStatusesByIdV2ResponseFailure401(response.body<Error>())
        410 -> DeleteFiltersStatusesByIdV2ResponseFailure410
        422 -> DeleteFiltersStatusesByIdV2ResponseFailure(response.body<ValidationError>())
        else -> DeleteFiltersStatusesByIdV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteFiltersStatusesByIdV2ResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetFiltersResponse

  @Serializable
  public data class GetFiltersResponseSuccess(
    public val body: V1Filter,
  ) : GetFiltersResponse()

  @Serializable
  public data class GetFiltersResponseFailure401(
    public val body: Error,
  ) : GetFiltersResponse()

  @Serializable
  public object GetFiltersResponseFailure410 : GetFiltersResponse()

  @Serializable
  public data class GetFiltersResponseFailure(
    public val body: ValidationError,
  ) : GetFiltersResponse()

  @Serializable
  public data class GetFiltersResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFiltersResponse()

  @Serializable
  public sealed class CreateFilterResponse

  @Serializable
  public data class CreateFilterResponseSuccess(
    public val body: V1Filter,
  ) : CreateFilterResponse()

  @Serializable
  public data class CreateFilterResponseFailure401(
    public val body: Error,
  ) : CreateFilterResponse()

  @Serializable
  public object CreateFilterResponseFailure : CreateFilterResponse()

  @Serializable
  public data class CreateFilterResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateFilterResponse()

  @Serializable
  public sealed class GetFilterResponse

  @Serializable
  public data class GetFilterResponseSuccess(
    public val body: V1Filter,
  ) : GetFilterResponse()

  @Serializable
  public data class GetFilterResponseFailure401(
    public val body: Error,
  ) : GetFilterResponse()

  @Serializable
  public object GetFilterResponseFailure410 : GetFilterResponse()

  @Serializable
  public data class GetFilterResponseFailure(
    public val body: ValidationError,
  ) : GetFilterResponse()

  @Serializable
  public data class GetFilterResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFilterResponse()

  @Serializable
  public sealed class UpdateFilterResponse

  @Serializable
  public data class UpdateFilterResponseSuccess(
    public val body: V1Filter,
  ) : UpdateFilterResponse()

  @Serializable
  public data class UpdateFilterResponseFailure401(
    public val body: Error,
  ) : UpdateFilterResponse()

  @Serializable
  public object UpdateFilterResponseFailure : UpdateFilterResponse()

  @Serializable
  public data class UpdateFilterResponseUnknownFailure(
    public val statusCode: Int,
  ) : UpdateFilterResponse()

  @Serializable
  public sealed class DeleteFilterResponse

  @Serializable
  public object DeleteFilterResponseSuccess : DeleteFilterResponse()

  @Serializable
  public data class DeleteFilterResponseFailure401(
    public val body: Error,
  ) : DeleteFilterResponse()

  @Serializable
  public object DeleteFilterResponseFailure410 : DeleteFilterResponse()

  @Serializable
  public data class DeleteFilterResponseFailure(
    public val body: ValidationError,
  ) : DeleteFilterResponse()

  @Serializable
  public data class DeleteFilterResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteFilterResponse()

  @Serializable
  public sealed class GetFiltersV2Response

  @Serializable
  public data class GetFiltersV2ResponseSuccess(
    public val body: List<Filter>,
  ) : GetFiltersV2Response()

  @Serializable
  public data class GetFiltersV2ResponseFailure401(
    public val body: Error,
  ) : GetFiltersV2Response()

  @Serializable
  public object GetFiltersV2ResponseFailure410 : GetFiltersV2Response()

  @Serializable
  public data class GetFiltersV2ResponseFailure(
    public val body: ValidationError,
  ) : GetFiltersV2Response()

  @Serializable
  public data class GetFiltersV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFiltersV2Response()

  @Serializable
  public sealed class CreateFilterV2Response

  @Serializable
  public data class CreateFilterV2ResponseSuccess(
    public val body: Filter,
  ) : CreateFilterV2Response()

  @Serializable
  public data class CreateFilterV2ResponseFailure401(
    public val body: Error,
  ) : CreateFilterV2Response()

  @Serializable
  public object CreateFilterV2ResponseFailure : CreateFilterV2Response()

  @Serializable
  public data class CreateFilterV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateFilterV2Response()

  @Serializable
  public sealed class GetFilterKeywordsV2Response

  @Serializable
  public data class GetFilterKeywordsV2ResponseSuccess(
    public val body: List<FilterKeyword>,
  ) : GetFilterKeywordsV2Response()

  @Serializable
  public data class GetFilterKeywordsV2ResponseFailure401(
    public val body: Error,
  ) : GetFilterKeywordsV2Response()

  @Serializable
  public object GetFilterKeywordsV2ResponseFailure410 : GetFilterKeywordsV2Response()

  @Serializable
  public data class GetFilterKeywordsV2ResponseFailure(
    public val body: ValidationError,
  ) : GetFilterKeywordsV2Response()

  @Serializable
  public data class GetFilterKeywordsV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFilterKeywordsV2Response()

  @Serializable
  public sealed class PostFilterKeywordsV2Response

  @Serializable
  public data class PostFilterKeywordsV2ResponseSuccess(
    public val body: FilterKeyword,
  ) : PostFilterKeywordsV2Response()

  @Serializable
  public data class PostFilterKeywordsV2ResponseFailure401(
    public val body: Error,
  ) : PostFilterKeywordsV2Response()

  @Serializable
  public object PostFilterKeywordsV2ResponseFailure : PostFilterKeywordsV2Response()

  @Serializable
  public data class PostFilterKeywordsV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostFilterKeywordsV2Response()

  @Serializable
  public sealed class GetFilterStatusesV2Response

  @Serializable
  public data class GetFilterStatusesV2ResponseSuccess(
    public val body: List<FilterStatus>,
  ) : GetFilterStatusesV2Response()

  @Serializable
  public data class GetFilterStatusesV2ResponseFailure401(
    public val body: Error,
  ) : GetFilterStatusesV2Response()

  @Serializable
  public object GetFilterStatusesV2ResponseFailure410 : GetFilterStatusesV2Response()

  @Serializable
  public data class GetFilterStatusesV2ResponseFailure(
    public val body: ValidationError,
  ) : GetFilterStatusesV2Response()

  @Serializable
  public data class GetFilterStatusesV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFilterStatusesV2Response()

  @Serializable
  public sealed class PostFilterStatusesV2Response

  @Serializable
  public data class PostFilterStatusesV2ResponseSuccess(
    public val body: FilterStatus,
  ) : PostFilterStatusesV2Response()

  @Serializable
  public data class PostFilterStatusesV2ResponseFailure401(
    public val body: Error,
  ) : PostFilterStatusesV2Response()

  @Serializable
  public object PostFilterStatusesV2ResponseFailure410 : PostFilterStatusesV2Response()

  @Serializable
  public data class PostFilterStatusesV2ResponseFailure(
    public val body: ValidationError,
  ) : PostFilterStatusesV2Response()

  @Serializable
  public data class PostFilterStatusesV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostFilterStatusesV2Response()

  @Serializable
  public sealed class GetFilterV2Response

  @Serializable
  public data class GetFilterV2ResponseSuccess(
    public val body: Filter,
  ) : GetFilterV2Response()

  @Serializable
  public data class GetFilterV2ResponseFailure401(
    public val body: Error,
  ) : GetFilterV2Response()

  @Serializable
  public object GetFilterV2ResponseFailure410 : GetFilterV2Response()

  @Serializable
  public data class GetFilterV2ResponseFailure(
    public val body: ValidationError,
  ) : GetFilterV2Response()

  @Serializable
  public data class GetFilterV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFilterV2Response()

  @Serializable
  public sealed class UpdateFilterV2Response

  @Serializable
  public data class UpdateFilterV2ResponseSuccess(
    public val body: Filter,
  ) : UpdateFilterV2Response()

  @Serializable
  public data class UpdateFilterV2ResponseFailure401(
    public val body: Error,
  ) : UpdateFilterV2Response()

  @Serializable
  public object UpdateFilterV2ResponseFailure410 : UpdateFilterV2Response()

  @Serializable
  public data class UpdateFilterV2ResponseFailure(
    public val body: ValidationError,
  ) : UpdateFilterV2Response()

  @Serializable
  public data class UpdateFilterV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : UpdateFilterV2Response()

  @Serializable
  public sealed class DeleteFilterV2Response

  @Serializable
  public object DeleteFilterV2ResponseSuccess : DeleteFilterV2Response()

  @Serializable
  public data class DeleteFilterV2ResponseFailure401(
    public val body: Error,
  ) : DeleteFilterV2Response()

  @Serializable
  public object DeleteFilterV2ResponseFailure410 : DeleteFilterV2Response()

  @Serializable
  public data class DeleteFilterV2ResponseFailure(
    public val body: ValidationError,
  ) : DeleteFilterV2Response()

  @Serializable
  public data class DeleteFilterV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteFilterV2Response()

  @Serializable
  public sealed class GetFiltersKeywordsByIdV2Response

  @Serializable
  public data class GetFiltersKeywordsByIdV2ResponseSuccess(
    public val body: FilterKeyword,
  ) : GetFiltersKeywordsByIdV2Response()

  @Serializable
  public data class GetFiltersKeywordsByIdV2ResponseFailure401(
    public val body: Error,
  ) : GetFiltersKeywordsByIdV2Response()

  @Serializable
  public object GetFiltersKeywordsByIdV2ResponseFailure410 : GetFiltersKeywordsByIdV2Response()

  @Serializable
  public data class GetFiltersKeywordsByIdV2ResponseFailure(
    public val body: ValidationError,
  ) : GetFiltersKeywordsByIdV2Response()

  @Serializable
  public data class GetFiltersKeywordsByIdV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFiltersKeywordsByIdV2Response()

  @Serializable
  public sealed class UpdateFiltersKeywordsByIdV2Response

  @Serializable
  public data class UpdateFiltersKeywordsByIdV2ResponseSuccess(
    public val body: FilterKeyword,
  ) : UpdateFiltersKeywordsByIdV2Response()

  @Serializable
  public data class UpdateFiltersKeywordsByIdV2ResponseFailure401(
    public val body: Error,
  ) : UpdateFiltersKeywordsByIdV2Response()

  @Serializable
  public object UpdateFiltersKeywordsByIdV2ResponseFailure : UpdateFiltersKeywordsByIdV2Response()

  @Serializable
  public data class UpdateFiltersKeywordsByIdV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : UpdateFiltersKeywordsByIdV2Response()

  @Serializable
  public sealed class DeleteFiltersKeywordsByIdV2Response

  @Serializable
  public object DeleteFiltersKeywordsByIdV2ResponseSuccess : DeleteFiltersKeywordsByIdV2Response()

  @Serializable
  public data class DeleteFiltersKeywordsByIdV2ResponseFailure401(
    public val body: Error,
  ) : DeleteFiltersKeywordsByIdV2Response()

  @Serializable
  public object DeleteFiltersKeywordsByIdV2ResponseFailure410 : DeleteFiltersKeywordsByIdV2Response()

  @Serializable
  public data class DeleteFiltersKeywordsByIdV2ResponseFailure(
    public val body: ValidationError,
  ) : DeleteFiltersKeywordsByIdV2Response()

  @Serializable
  public data class DeleteFiltersKeywordsByIdV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteFiltersKeywordsByIdV2Response()

  @Serializable
  public sealed class GetFiltersStatusesByIdV2Response

  @Serializable
  public data class GetFiltersStatusesByIdV2ResponseSuccess(
    public val body: FilterStatus,
  ) : GetFiltersStatusesByIdV2Response()

  @Serializable
  public data class GetFiltersStatusesByIdV2ResponseFailure401(
    public val body: Error,
  ) : GetFiltersStatusesByIdV2Response()

  @Serializable
  public object GetFiltersStatusesByIdV2ResponseFailure410 : GetFiltersStatusesByIdV2Response()

  @Serializable
  public data class GetFiltersStatusesByIdV2ResponseFailure(
    public val body: ValidationError,
  ) : GetFiltersStatusesByIdV2Response()

  @Serializable
  public data class GetFiltersStatusesByIdV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetFiltersStatusesByIdV2Response()

  @Serializable
  public sealed class DeleteFiltersStatusesByIdV2Response

  @Serializable
  public data class DeleteFiltersStatusesByIdV2ResponseSuccess(
    public val body: FilterStatus,
  ) : DeleteFiltersStatusesByIdV2Response()

  @Serializable
  public data class DeleteFiltersStatusesByIdV2ResponseFailure401(
    public val body: Error,
  ) : DeleteFiltersStatusesByIdV2Response()

  @Serializable
  public object DeleteFiltersStatusesByIdV2ResponseFailure410 : DeleteFiltersStatusesByIdV2Response()

  @Serializable
  public data class DeleteFiltersStatusesByIdV2ResponseFailure(
    public val body: ValidationError,
  ) : DeleteFiltersStatusesByIdV2Response()

  @Serializable
  public data class DeleteFiltersStatusesByIdV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteFiltersStatusesByIdV2Response()
}
