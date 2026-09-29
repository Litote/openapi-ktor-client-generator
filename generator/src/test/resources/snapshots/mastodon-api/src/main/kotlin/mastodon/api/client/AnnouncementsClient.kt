package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Announcement
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class AnnouncementsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View all announcements
   */
  public suspend fun getAnnouncements(): GetAnnouncementsResponse {
    try {
      val response = configuration.client.`get`("api/v1/announcements") {
      }
      return when (response.status.value) {
        200 -> GetAnnouncementsResponseSuccess(response.body<List<Announcement>>(), response.headers)
        401, 404, 429, 503 -> GetAnnouncementsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAnnouncementsResponseFailure410(response.headers)
        422 -> GetAnnouncementsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAnnouncementsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAnnouncementsResponseUnknownFailure(500)
    }
  }

  /**
   * Dismiss an announcement
   */
  public suspend fun postAnnouncementDismiss(id: String): PostAnnouncementDismissResponse {
    try {
      val response = configuration.client.post("api/v1/announcements/{id}/dismiss".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostAnnouncementDismissResponseSuccess(response.headers)
        401, 404, 429, 503 -> PostAnnouncementDismissResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAnnouncementDismissResponseFailure410(response.headers)
        422 -> PostAnnouncementDismissResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostAnnouncementDismissResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAnnouncementDismissResponseUnknownFailure(500)
    }
  }

  /**
   * Add a reaction to an announcement
   */
  public suspend fun updateAnnouncementReaction(id: String, name: String): UpdateAnnouncementReactionResponse {
    try {
      val response = configuration.client.put("api/v1/announcements/{id}/reactions/{name}".replace("/{id}", "/${id.encodeURLPathPart()}").replace("/{name}", "/${name.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> UpdateAnnouncementReactionResponseSuccess(response.headers)
        401, 404, 422, 429, 503 -> UpdateAnnouncementReactionResponseFailure401(response.body<Error>(), response.headers)
        410 -> UpdateAnnouncementReactionResponseFailure(response.headers)
        else -> UpdateAnnouncementReactionResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return UpdateAnnouncementReactionResponseUnknownFailure(500)
    }
  }

  /**
   * Remove a reaction from an announcement
   */
  public suspend fun deleteAnnouncementReaction(id: String, name: String): DeleteAnnouncementReactionResponse {
    try {
      val response = configuration.client.delete("api/v1/announcements/{id}/reactions/{name}".replace("/{id}", "/${id.encodeURLPathPart()}").replace("/{name}", "/${name.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> DeleteAnnouncementReactionResponseSuccess(response.headers)
        401, 404, 422, 429, 503 -> DeleteAnnouncementReactionResponseFailure401(response.body<Error>(), response.headers)
        410 -> DeleteAnnouncementReactionResponseFailure(response.headers)
        else -> DeleteAnnouncementReactionResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteAnnouncementReactionResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetAnnouncementsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAnnouncementsResponseSuccess(
    public val body: List<Announcement>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAnnouncementsResponse() {
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
  public data class GetAnnouncementsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAnnouncementsResponse()

  @Serializable
  public data class GetAnnouncementsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAnnouncementsResponse()

  @Serializable
  public data class GetAnnouncementsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAnnouncementsResponse()

  @Serializable
  public data class GetAnnouncementsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAnnouncementsResponse()

  @Serializable
  public sealed class PostAnnouncementDismissResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAnnouncementDismissResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAnnouncementDismissResponse() {
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
  public data class PostAnnouncementDismissResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAnnouncementDismissResponse()

  @Serializable
  public data class PostAnnouncementDismissResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAnnouncementDismissResponse()

  @Serializable
  public data class PostAnnouncementDismissResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAnnouncementDismissResponse()

  @Serializable
  public data class PostAnnouncementDismissResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAnnouncementDismissResponse()

  @Serializable
  public sealed class UpdateAnnouncementReactionResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class UpdateAnnouncementReactionResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateAnnouncementReactionResponse() {
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
  public data class UpdateAnnouncementReactionResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateAnnouncementReactionResponse()

  @Serializable
  public data class UpdateAnnouncementReactionResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateAnnouncementReactionResponse()

  @Serializable
  public data class UpdateAnnouncementReactionResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : UpdateAnnouncementReactionResponse()

  @Serializable
  public sealed class DeleteAnnouncementReactionResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeleteAnnouncementReactionResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteAnnouncementReactionResponse() {
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
  public data class DeleteAnnouncementReactionResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteAnnouncementReactionResponse()

  @Serializable
  public data class DeleteAnnouncementReactionResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteAnnouncementReactionResponse()

  @Serializable
  public data class DeleteAnnouncementReactionResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeleteAnnouncementReactionResponse()
}
