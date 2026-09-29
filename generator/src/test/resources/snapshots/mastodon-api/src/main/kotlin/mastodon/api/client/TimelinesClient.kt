package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Status
import mastodon.api.model.ValidationError

public interface TimelinesClient {
  /**
   * View direct timeline
   */
  public suspend fun getTimelineDirect(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetTimelineDirectResponse

  /**
   * View home timeline
   */
  public suspend fun getTimelineHome(
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetTimelineHomeResponse

  /**
   * View link timeline
   */
  public suspend fun getTimelineLink(
    url: String,
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetTimelineLinkResponse

  /**
   * View list timeline
   */
  public suspend fun getTimelinesListByListId(
    listId: String,
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetTimelinesListByListIdResponse

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
  ): GetTimelinePublicResponse

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
  ): GetTimelinesTagByHashtagResponse

  @Serializable
  public sealed class GetTimelineDirectResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTimelineDirectResponseSuccess(
    public val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineDirectResponse() {
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
  public data class GetTimelineDirectResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineDirectResponse()

  @Serializable
  public data class GetTimelineDirectResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineDirectResponse()

  @Serializable
  public data class GetTimelineDirectResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineDirectResponse()

  @Serializable
  public data class GetTimelineDirectResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineDirectResponse()

  @Serializable
  public sealed class GetTimelineHomeResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTimelineHomeResponseSuccess200(
    public val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineHomeResponse() {
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
  public data class GetTimelineHomeResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineHomeResponse() {
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
  public data class GetTimelineHomeResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineHomeResponse()

  @Serializable
  public data class GetTimelineHomeResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineHomeResponse()

  @Serializable
  public data class GetTimelineHomeResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineHomeResponse()

  @Serializable
  public data class GetTimelineHomeResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineHomeResponse()

  @Serializable
  public sealed class GetTimelineLinkResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTimelineLinkResponseSuccess(
    public val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineLinkResponse() {
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
  public data class GetTimelineLinkResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineLinkResponse()

  @Serializable
  public data class GetTimelineLinkResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineLinkResponse()

  @Serializable
  public data class GetTimelineLinkResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineLinkResponse()

  @Serializable
  public data class GetTimelineLinkResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelineLinkResponse()

  @Serializable
  public sealed class GetTimelinesListByListIdResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTimelinesListByListIdResponseSuccess(
    public val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinesListByListIdResponse() {
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
  public data class GetTimelinesListByListIdResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinesListByListIdResponse()

  @Serializable
  public data class GetTimelinesListByListIdResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinesListByListIdResponse()

  @Serializable
  public data class GetTimelinesListByListIdResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinesListByListIdResponse()

  @Serializable
  public data class GetTimelinesListByListIdResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinesListByListIdResponse()

  @Serializable
  public sealed class GetTimelinePublicResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTimelinePublicResponseSuccess(
    public val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinePublicResponse() {
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
  public data class GetTimelinePublicResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinePublicResponse()

  @Serializable
  public data class GetTimelinePublicResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinePublicResponse()

  @Serializable
  public data class GetTimelinePublicResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinePublicResponse()

  @Serializable
  public data class GetTimelinePublicResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinePublicResponse()

  @Serializable
  public sealed class GetTimelinesTagByHashtagResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTimelinesTagByHashtagResponseSuccess(
    public val body: List<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinesTagByHashtagResponse() {
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
  public data class GetTimelinesTagByHashtagResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinesTagByHashtagResponse()

  @Serializable
  public data class GetTimelinesTagByHashtagResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinesTagByHashtagResponse()

  @Serializable
  public data class GetTimelinesTagByHashtagResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinesTagByHashtagResponse()

  @Serializable
  public data class GetTimelinesTagByHashtagResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTimelinesTagByHashtagResponse()
}

public fun TimelinesClient(configuration: ClientConfiguration = defaultClientConfiguration): TimelinesClient = DefaultTimelinesClient(configuration)

public class DefaultTimelinesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : TimelinesClient {
  override suspend fun getTimelineDirect(
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): TimelinesClient.GetTimelineDirectResponse {
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
        200 -> TimelinesClient.GetTimelineDirectResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> TimelinesClient.GetTimelineDirectResponseFailure401(response.body<Error>(), response.headers)
        410 -> TimelinesClient.GetTimelineDirectResponseFailure410(response.headers)
        422 -> TimelinesClient.GetTimelineDirectResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TimelinesClient.GetTimelineDirectResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TimelinesClient.GetTimelineDirectResponseUnknownFailure(500)
    }
  }

  override suspend fun getTimelineHome(
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): TimelinesClient.GetTimelineHomeResponse {
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
        200 -> TimelinesClient.GetTimelineHomeResponseSuccess200(response.body<List<Status>>(), response.headers)
        206 -> TimelinesClient.GetTimelineHomeResponseSuccess(response.headers)
        401, 404, 429, 503 -> TimelinesClient.GetTimelineHomeResponseFailure401(response.body<Error>(), response.headers)
        410 -> TimelinesClient.GetTimelineHomeResponseFailure410(response.headers)
        422 -> TimelinesClient.GetTimelineHomeResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TimelinesClient.GetTimelineHomeResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TimelinesClient.GetTimelineHomeResponseUnknownFailure(500)
    }
  }

  override suspend fun getTimelineLink(
    url: String,
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): TimelinesClient.GetTimelineLinkResponse {
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
        200 -> TimelinesClient.GetTimelineLinkResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> TimelinesClient.GetTimelineLinkResponseFailure401(response.body<Error>(), response.headers)
        410 -> TimelinesClient.GetTimelineLinkResponseFailure410(response.headers)
        422 -> TimelinesClient.GetTimelineLinkResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TimelinesClient.GetTimelineLinkResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TimelinesClient.GetTimelineLinkResponseUnknownFailure(500)
    }
  }

  override suspend fun getTimelinesListByListId(
    listId: String,
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): TimelinesClient.GetTimelinesListByListIdResponse {
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
        200 -> TimelinesClient.GetTimelinesListByListIdResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> TimelinesClient.GetTimelinesListByListIdResponseFailure401(response.body<Error>(), response.headers)
        410 -> TimelinesClient.GetTimelinesListByListIdResponseFailure410(response.headers)
        422 -> TimelinesClient.GetTimelinesListByListIdResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TimelinesClient.GetTimelinesListByListIdResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TimelinesClient.GetTimelinesListByListIdResponseUnknownFailure(500)
    }
  }

  override suspend fun getTimelinePublic(
    limit: Long?,
    local: Boolean?,
    maxId: String?,
    minId: String?,
    onlyMedia: Boolean?,
    remote: Boolean?,
    sinceId: String?,
  ): TimelinesClient.GetTimelinePublicResponse {
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
        200 -> TimelinesClient.GetTimelinePublicResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> TimelinesClient.GetTimelinePublicResponseFailure401(response.body<Error>(), response.headers)
        410 -> TimelinesClient.GetTimelinePublicResponseFailure410(response.headers)
        422 -> TimelinesClient.GetTimelinePublicResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TimelinesClient.GetTimelinePublicResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TimelinesClient.GetTimelinePublicResponseUnknownFailure(500)
    }
  }

  override suspend fun getTimelinesTagByHashtag(
    hashtag: String,
    all: List<String>?,
    any: List<String>?,
    limit: Long?,
    local: Boolean?,
    maxId: String?,
    minId: String?,
    none: List<String>?,
    onlyMedia: Boolean?,
    remote: Boolean?,
    sinceId: String?,
  ): TimelinesClient.GetTimelinesTagByHashtagResponse {
    try {
      val response = configuration.client.`get`("api/v1/timelines/tag/{hashtag}".replace("/{hashtag}", "/${hashtag.encodeURLPathPart()}")) {
        url {
          if (all != null) {
            parameters.appendAll("all", all)
          }
          if (any != null) {
            parameters.appendAll("any", any)
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
            parameters.appendAll("none", none)
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
        200 -> TimelinesClient.GetTimelinesTagByHashtagResponseSuccess(response.body<List<Status>>(), response.headers)
        401, 404, 429, 503 -> TimelinesClient.GetTimelinesTagByHashtagResponseFailure401(response.body<Error>(), response.headers)
        410 -> TimelinesClient.GetTimelinesTagByHashtagResponseFailure410(response.headers)
        422 -> TimelinesClient.GetTimelinesTagByHashtagResponseFailure(response.body<ValidationError>(), response.headers)
        else -> TimelinesClient.GetTimelinesTagByHashtagResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return TimelinesClient.GetTimelinesTagByHashtagResponseUnknownFailure(500)
    }
  }
}
