package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.encodeURLPathPart
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Status
import mastodon.api.model.ValidationError

public class TimelinesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View direct timeline
   */
  public suspend fun getTimelineDirect(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetTimelineDirectResponse {
    try {
      val response = configuration.client.`get`("api/v1/timelines/direct") {
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
        200 -> GetTimelineDirectResponseSuccess(response.body<List<Status>>())
        401, 404, 429, 503 -> GetTimelineDirectResponseFailure401(response.body<Error>())
        410 -> GetTimelineDirectResponseFailure410
        422 -> GetTimelineDirectResponseFailure(response.body<ValidationError>())
        else -> GetTimelineDirectResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTimelineDirectResponseUnknownFailure(500)
    }
  }

  /**
   * View home timeline
   */
  public suspend fun getTimelineHome(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetTimelineHomeResponse {
    try {
      val response = configuration.client.`get`("api/v1/timelines/home") {
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
        200 -> GetTimelineHomeResponseSuccess200(response.body<List<Status>>())
        206 -> GetTimelineHomeResponseSuccess
        401, 404, 429, 503 -> GetTimelineHomeResponseFailure401(response.body<Error>())
        410 -> GetTimelineHomeResponseFailure410
        422 -> GetTimelineHomeResponseFailure(response.body<ValidationError>())
        else -> GetTimelineHomeResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTimelineHomeResponseUnknownFailure(500)
    }
  }

  /**
   * View link timeline
   */
  public suspend fun getTimelineLink(
    url: String,
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetTimelineLinkResponse {
    try {
      val response = configuration.client.`get`("api/v1/timelines/link") {
        url {
          parameters.append("url", url)
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
        200 -> GetTimelineLinkResponseSuccess(response.body<List<Status>>())
        401, 404, 429, 503 -> GetTimelineLinkResponseFailure401(response.body<Error>())
        410 -> GetTimelineLinkResponseFailure410
        422 -> GetTimelineLinkResponseFailure(response.body<ValidationError>())
        else -> GetTimelineLinkResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTimelineLinkResponseUnknownFailure(500)
    }
  }

  /**
   * View list timeline
   */
  public suspend fun getTimelinesListByListId(
    listId: String,
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetTimelinesListByListIdResponse {
    try {
      val response = configuration.client.`get`("api/v1/timelines/list/{list_id}".replace("/{list_id}", "/${listId.encodeURLPathPart()}")) {
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
        200 -> GetTimelinesListByListIdResponseSuccess(response.body<List<Status>>())
        401, 404, 429, 503 -> GetTimelinesListByListIdResponseFailure401(response.body<Error>())
        410 -> GetTimelinesListByListIdResponseFailure410
        422 -> GetTimelinesListByListIdResponseFailure(response.body<ValidationError>())
        else -> GetTimelinesListByListIdResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTimelinesListByListIdResponseUnknownFailure(500)
    }
  }

  /**
   * View public timeline
   */
  public suspend fun getTimelinePublic(
    limit: Long? = 20,
    local: Boolean? = false,
    maxId: String? = null,
    minId: String? = null,
    onlyMedia: Boolean? = false,
    remote: Boolean? = false,
    sinceId: String? = null,
  ): GetTimelinePublicResponse {
    try {
      val response = configuration.client.`get`("api/v1/timelines/public") {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (local != null) {
            parameters.append("local", local.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (minId != null) {
            parameters.append("min_id", minId)
          }
          if (onlyMedia != null) {
            parameters.append("only_media", onlyMedia.toString())
          }
          if (remote != null) {
            parameters.append("remote", remote.toString())
          }
          if (sinceId != null) {
            parameters.append("since_id", sinceId)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetTimelinePublicResponseSuccess(response.body<List<Status>>())
        401, 404, 429, 503 -> GetTimelinePublicResponseFailure401(response.body<Error>())
        410 -> GetTimelinePublicResponseFailure410
        422 -> GetTimelinePublicResponseFailure(response.body<ValidationError>())
        else -> GetTimelinePublicResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTimelinePublicResponseUnknownFailure(500)
    }
  }

  /**
   * View hashtag timeline
   */
  public suspend fun getTimelinesTagByHashtag(
    hashtag: String,
    all: List<String>? = null,
    any: List<String>? = null,
    limit: Long? = 20,
    local: Boolean? = false,
    maxId: String? = null,
    minId: String? = null,
    none: List<String>? = null,
    onlyMedia: Boolean? = false,
    remote: Boolean? = false,
    sinceId: String? = null,
  ): GetTimelinesTagByHashtagResponse {
    try {
      val response = configuration.client.`get`("api/v1/timelines/tag/{hashtag}".replace("/{hashtag}", "/${hashtag.encodeURLPathPart()}")) {
        url {
          if (all != null) {
            parameters.append("all", all.joinToString(","))
          }
          if (any != null) {
            parameters.append("any", any.joinToString(","))
          }
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (local != null) {
            parameters.append("local", local.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (minId != null) {
            parameters.append("min_id", minId)
          }
          if (none != null) {
            parameters.append("none", none.joinToString(","))
          }
          if (onlyMedia != null) {
            parameters.append("only_media", onlyMedia.toString())
          }
          if (remote != null) {
            parameters.append("remote", remote.toString())
          }
          if (sinceId != null) {
            parameters.append("since_id", sinceId)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetTimelinesTagByHashtagResponseSuccess(response.body<List<Status>>())
        401, 404, 429, 503 -> GetTimelinesTagByHashtagResponseFailure401(response.body<Error>())
        410 -> GetTimelinesTagByHashtagResponseFailure410
        422 -> GetTimelinesTagByHashtagResponseFailure(response.body<ValidationError>())
        else -> GetTimelinesTagByHashtagResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTimelinesTagByHashtagResponseUnknownFailure(500)
    }
  }

  @Serializable
  public object All

  @Serializable
  public object Any

  @Serializable
  public object None

  @Serializable
  public sealed class GetTimelineDirectResponse

  @Serializable
  public data class GetTimelineDirectResponseSuccess(
    public val body: List<Status>,
  ) : GetTimelineDirectResponse()

  @Serializable
  public data class GetTimelineDirectResponseFailure401(
    public val body: Error,
  ) : GetTimelineDirectResponse()

  @Serializable
  public object GetTimelineDirectResponseFailure410 : GetTimelineDirectResponse()

  @Serializable
  public data class GetTimelineDirectResponseFailure(
    public val body: ValidationError,
  ) : GetTimelineDirectResponse()

  @Serializable
  public data class GetTimelineDirectResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTimelineDirectResponse()

  @Serializable
  public sealed class GetTimelineHomeResponse

  @Serializable
  public data class GetTimelineHomeResponseSuccess200(
    public val body: List<Status>,
  ) : GetTimelineHomeResponse()

  @Serializable
  public object GetTimelineHomeResponseSuccess : GetTimelineHomeResponse()

  @Serializable
  public data class GetTimelineHomeResponseFailure401(
    public val body: Error,
  ) : GetTimelineHomeResponse()

  @Serializable
  public object GetTimelineHomeResponseFailure410 : GetTimelineHomeResponse()

  @Serializable
  public data class GetTimelineHomeResponseFailure(
    public val body: ValidationError,
  ) : GetTimelineHomeResponse()

  @Serializable
  public data class GetTimelineHomeResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTimelineHomeResponse()

  @Serializable
  public sealed class GetTimelineLinkResponse

  @Serializable
  public data class GetTimelineLinkResponseSuccess(
    public val body: List<Status>,
  ) : GetTimelineLinkResponse()

  @Serializable
  public data class GetTimelineLinkResponseFailure401(
    public val body: Error,
  ) : GetTimelineLinkResponse()

  @Serializable
  public object GetTimelineLinkResponseFailure410 : GetTimelineLinkResponse()

  @Serializable
  public data class GetTimelineLinkResponseFailure(
    public val body: ValidationError,
  ) : GetTimelineLinkResponse()

  @Serializable
  public data class GetTimelineLinkResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTimelineLinkResponse()

  @Serializable
  public sealed class GetTimelinesListByListIdResponse

  @Serializable
  public data class GetTimelinesListByListIdResponseSuccess(
    public val body: List<Status>,
  ) : GetTimelinesListByListIdResponse()

  @Serializable
  public data class GetTimelinesListByListIdResponseFailure401(
    public val body: Error,
  ) : GetTimelinesListByListIdResponse()

  @Serializable
  public object GetTimelinesListByListIdResponseFailure410 : GetTimelinesListByListIdResponse()

  @Serializable
  public data class GetTimelinesListByListIdResponseFailure(
    public val body: ValidationError,
  ) : GetTimelinesListByListIdResponse()

  @Serializable
  public data class GetTimelinesListByListIdResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTimelinesListByListIdResponse()

  @Serializable
  public sealed class GetTimelinePublicResponse

  @Serializable
  public data class GetTimelinePublicResponseSuccess(
    public val body: List<Status>,
  ) : GetTimelinePublicResponse()

  @Serializable
  public data class GetTimelinePublicResponseFailure401(
    public val body: Error,
  ) : GetTimelinePublicResponse()

  @Serializable
  public object GetTimelinePublicResponseFailure410 : GetTimelinePublicResponse()

  @Serializable
  public data class GetTimelinePublicResponseFailure(
    public val body: ValidationError,
  ) : GetTimelinePublicResponse()

  @Serializable
  public data class GetTimelinePublicResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTimelinePublicResponse()

  @Serializable
  public sealed class GetTimelinesTagByHashtagResponse

  @Serializable
  public data class GetTimelinesTagByHashtagResponseSuccess(
    public val body: List<Status>,
  ) : GetTimelinesTagByHashtagResponse()

  @Serializable
  public data class GetTimelinesTagByHashtagResponseFailure401(
    public val body: Error,
  ) : GetTimelinesTagByHashtagResponse()

  @Serializable
  public object GetTimelinesTagByHashtagResponseFailure410 : GetTimelinesTagByHashtagResponse()

  @Serializable
  public data class GetTimelinesTagByHashtagResponseFailure(
    public val body: ValidationError,
  ) : GetTimelinesTagByHashtagResponse()

  @Serializable
  public data class GetTimelinesTagByHashtagResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTimelinesTagByHashtagResponse()
}
