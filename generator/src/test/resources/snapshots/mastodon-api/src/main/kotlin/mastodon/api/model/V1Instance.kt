package mastodon.api.model

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class V1Instance(
  @SerialName("approval_required")
  public val approvalRequired: Boolean,
  public val configuration: Configuration,
  @SerialName("contact_account")
  public val contactAccount: Account? = null,
  public val description: String,
  public val email: String,
  @SerialName("invites_enabled")
  public val invitesEnabled: Boolean,
  public val languages: List<String>,
  public val registrations: Boolean,
  public val rules: List<Rule>,
  @SerialName("short_description")
  public val shortDescription: String,
  public val stats: Stats,
  public val thumbnail: String? = null,
  public val title: String,
  public val uri: String,
  public val urls: Urls,
  public val version: String,
) {
  @Serializable
  public data class Configuration(
    public val accounts: Accounts,
    @SerialName("media_attachments")
    public val mediaAttachments: MediaAttachments,
    public val polls: Polls,
    public val statuses: Statuses,
  ) {
    @Serializable
    public data class Accounts(
      @SerialName("max_featured_tags")
      public val maxFeaturedTags: Long,
    )

    @Serializable
    public data class MediaAttachments(
      @SerialName("image_matrix_limit")
      public val imageMatrixLimit: Long,
      @SerialName("image_size_limit")
      public val imageSizeLimit: Long,
      @SerialName("supported_mime_types")
      public val supportedMimeTypes: List<String>,
      @SerialName("video_frame_rate_limit")
      public val videoFrameRateLimit: Long,
      @SerialName("video_matrix_limit")
      public val videoMatrixLimit: Long,
      @SerialName("video_size_limit")
      public val videoSizeLimit: Long,
    )

    @Serializable
    public data class Polls(
      @SerialName("max_characters_per_option")
      public val maxCharactersPerOption: Long,
      @SerialName("max_expiration")
      public val maxExpiration: Long,
      @SerialName("max_options")
      public val maxOptions: Long,
      @SerialName("min_expiration")
      public val minExpiration: Long,
    )

    @Serializable
    public data class Statuses(
      @SerialName("characters_reserved_per_url")
      public val charactersReservedPerUrl: Long,
      @SerialName("max_characters")
      public val maxCharacters: Long,
      @SerialName("max_media_attachments")
      public val maxMediaAttachments: Long,
    )
  }

  @Serializable
  public data class Stats(
    @SerialName("domain_count")
    public val domainCount: Long,
    @SerialName("status_count")
    public val statusCount: Long,
    @SerialName("user_count")
    public val userCount: Long,
  )

  @Serializable
  public data class Urls(
    @SerialName("streaming_api")
    public val streamingApi: String,
  )
}
