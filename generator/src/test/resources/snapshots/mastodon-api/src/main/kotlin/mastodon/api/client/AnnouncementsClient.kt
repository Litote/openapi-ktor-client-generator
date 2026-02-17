package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
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
        200 -> GetAnnouncementsResponseSuccess(response.body<List<Announcement>>())
        401, 404, 429, 503 -> GetAnnouncementsResponseFailure401(response.body<Error>())
        410 -> GetAnnouncementsResponseFailure410
        422 -> GetAnnouncementsResponseFailure(response.body<ValidationError>())
        else -> GetAnnouncementsResponseUnknownFailure(response.status.value)
      }
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
        200 -> PostAnnouncementDismissResponseSuccess
        401, 404, 429, 503 -> PostAnnouncementDismissResponseFailure401(response.body<Error>())
        410 -> PostAnnouncementDismissResponseFailure410
        422 -> PostAnnouncementDismissResponseFailure(response.body<ValidationError>())
        else -> PostAnnouncementDismissResponseUnknownFailure(response.status.value)
      }
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
        200 -> UpdateAnnouncementReactionResponseSuccess
        401, 404, 422, 429, 503 -> UpdateAnnouncementReactionResponseFailure401(response.body<Error>())
        410 -> UpdateAnnouncementReactionResponseFailure
        else -> UpdateAnnouncementReactionResponseUnknownFailure(response.status.value)
      }
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
        200 -> DeleteAnnouncementReactionResponseSuccess
        401, 404, 422, 429, 503 -> DeleteAnnouncementReactionResponseFailure401(response.body<Error>())
        410 -> DeleteAnnouncementReactionResponseFailure
        else -> DeleteAnnouncementReactionResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeleteAnnouncementReactionResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetAnnouncementsResponse

  @Serializable
  public data class GetAnnouncementsResponseSuccess(
    public val body: List<Announcement>,
  ) : GetAnnouncementsResponse()

  @Serializable
  public data class GetAnnouncementsResponseFailure401(
    public val body: Error,
  ) : GetAnnouncementsResponse()

  @Serializable
  public object GetAnnouncementsResponseFailure410 : GetAnnouncementsResponse()

  @Serializable
  public data class GetAnnouncementsResponseFailure(
    public val body: ValidationError,
  ) : GetAnnouncementsResponse()

  @Serializable
  public data class GetAnnouncementsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAnnouncementsResponse()

  @Serializable
  public sealed class PostAnnouncementDismissResponse

  @Serializable
  public object PostAnnouncementDismissResponseSuccess : PostAnnouncementDismissResponse()

  @Serializable
  public data class PostAnnouncementDismissResponseFailure401(
    public val body: Error,
  ) : PostAnnouncementDismissResponse()

  @Serializable
  public object PostAnnouncementDismissResponseFailure410 : PostAnnouncementDismissResponse()

  @Serializable
  public data class PostAnnouncementDismissResponseFailure(
    public val body: ValidationError,
  ) : PostAnnouncementDismissResponse()

  @Serializable
  public data class PostAnnouncementDismissResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAnnouncementDismissResponse()

  @Serializable
  public sealed class UpdateAnnouncementReactionResponse

  @Serializable
  public object UpdateAnnouncementReactionResponseSuccess : UpdateAnnouncementReactionResponse()

  @Serializable
  public data class UpdateAnnouncementReactionResponseFailure401(
    public val body: Error,
  ) : UpdateAnnouncementReactionResponse()

  @Serializable
  public object UpdateAnnouncementReactionResponseFailure : UpdateAnnouncementReactionResponse()

  @Serializable
  public data class UpdateAnnouncementReactionResponseUnknownFailure(
    public val statusCode: Int,
  ) : UpdateAnnouncementReactionResponse()

  @Serializable
  public sealed class DeleteAnnouncementReactionResponse

  @Serializable
  public object DeleteAnnouncementReactionResponseSuccess : DeleteAnnouncementReactionResponse()

  @Serializable
  public data class DeleteAnnouncementReactionResponseFailure401(
    public val body: Error,
  ) : DeleteAnnouncementReactionResponse()

  @Serializable
  public object DeleteAnnouncementReactionResponseFailure : DeleteAnnouncementReactionResponse()

  @Serializable
  public data class DeleteAnnouncementReactionResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeleteAnnouncementReactionResponse()
}
