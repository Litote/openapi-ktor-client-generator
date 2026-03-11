package mastodon.api.model

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public data class Instance(
  @SerialName("api_versions")
  public val apiVersions: ApiVersions? = null,
  public val configuration: Configuration,
  public val contact: Contact,
  public val description: String,
  public val domain: String,
  public val icon: List<InstanceIcon>? = null,
  public val languages: List<String>,
  public val registrations: Registrations,
  public val rules: List<Rule>,
  @SerialName("source_url")
  public val sourceUrl: String,
  public val thumbnail: Thumbnail,
  public val title: String,
  public val usage: Usage,
  public val version: String,
) {
  @Serializable
  public data class ApiVersions(
    public val mastodon: Long? = null,
  )

  @Serializable
  public data class Configuration(
    public val accounts: Accounts,
    @SerialName("limited_federation")
    public val limitedFederation: Boolean? = null,
    @SerialName("media_attachments")
    public val mediaAttachments: MediaAttachments,
    public val polls: Polls,
    public val statuses: Statuses,
    @SerialName("timelines_access")
    public val timelinesAccess: TimelinesAccess? = null,
    public val translation: Translation,
    public val urls: Urls,
  ) {
    @Serializable
    public data class Accounts(
      @SerialName("max_featured_tags")
      public val maxFeaturedTags: Long,
      @SerialName("max_pinned_statuses")
      public val maxPinnedStatuses: Long? = null,
      @SerialName("max_profile_fields")
      public val maxProfileFields: Long? = null,
      @SerialName("profile_field_name_limit")
      public val profileFieldNameLimit: Long? = null,
      @SerialName("profile_field_value_limit")
      public val profileFieldValueLimit: Long? = null,
    )

    @Serializable
    public data class MediaAttachments(
      @SerialName("description_limit")
      public val descriptionLimit: Long? = null,
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

    @Serializable
    public data class TimelinesAccess(
      @SerialName("hashtag_feeds")
      public val hashtagFeeds: HashtagFeeds? = null,
      @SerialName("live_feeds")
      public val liveFeeds: LiveFeeds? = null,
      @SerialName("trending_link_feeds")
      public val trendingLinkFeeds: TrendingLinkFeeds? = null,
    ) {
      @Serializable
      public data class HashtagFeeds(
        public val local: Local? = null,
        public val remote: Remote? = null,
      ) {
        @Serializable
        public enum class Local {
          @SerialName("public")
          PUBLIC,
          @SerialName("authenticated")
          AUTHENTICATED,
          ;

          public fun serialName(): String = Local.serializer().descriptor.getElementName(this.ordinal)
        }

        @Serializable
        public enum class Remote {
          @SerialName("public")
          PUBLIC,
          @SerialName("authenticated")
          AUTHENTICATED,
          @SerialName("disabled")
          DISABLED,
          ;

          public fun serialName(): String = Remote.serializer().descriptor.getElementName(this.ordinal)
        }
      }

      @Serializable
      public data class LiveFeeds(
        public val local: Local? = null,
        public val remote: Remote? = null,
      ) {
        @Serializable
        public enum class Local {
          @SerialName("public")
          PUBLIC,
          @SerialName("authenticated")
          AUTHENTICATED,
          @SerialName("disabled")
          DISABLED,
          ;

          public fun serialName(): String = Local.serializer().descriptor.getElementName(this.ordinal)
        }

        @Serializable
        public enum class Remote {
          @SerialName("public")
          PUBLIC,
          @SerialName("authenticated")
          AUTHENTICATED,
          @SerialName("disabled")
          DISABLED,
          ;

          public fun serialName(): String = Remote.serializer().descriptor.getElementName(this.ordinal)
        }
      }

      @Serializable
      public data class TrendingLinkFeeds(
        public val local: Local? = null,
        public val remote: Remote? = null,
      ) {
        @Serializable
        public enum class Local {
          @SerialName("public")
          PUBLIC,
          @SerialName("authenticated")
          AUTHENTICATED,
          ;

          public fun serialName(): String = Local.serializer().descriptor.getElementName(this.ordinal)
        }

        @Serializable
        public enum class Remote {
          @SerialName("public")
          PUBLIC,
          @SerialName("authenticated")
          AUTHENTICATED,
          @SerialName("disabled")
          DISABLED,
          ;

          public fun serialName(): String = Remote.serializer().descriptor.getElementName(this.ordinal)
        }
      }
    }

    @Serializable
    public data class Translation(
      public val enabled: Boolean,
    )

    @Serializable
    public data class Urls(
      public val about: String? = null,
      @SerialName("privacy_policy")
      public val privacyPolicy: String? = null,
      public val status: String? = null,
      public val streaming: String? = null,
      @SerialName("terms_of_service")
      public val termsOfService: String? = null,
    )
  }

  @Serializable
  public data class Contact(
    public val account: Account? = null,
    public val email: String,
  )

  @Serializable
  public data class Registrations(
    @SerialName("approval_required")
    public val approvalRequired: Boolean,
    public val enabled: Boolean,
    public val message: String? = null,
    @SerialName("min_age")
    public val minAge: Long? = null,
    @SerialName("reason_required")
    public val reasonRequired: Boolean? = null,
    public val url: String? = null,
  )

  @Serializable
  public data class Thumbnail(
    public val blurhash: String? = null,
    public val url: String,
    public val versions: Versions? = null,
  ) {
    @Serializable
    public data class Versions(
      @SerialName("@1x")
      public val `1x`: String? = null,
      @SerialName("@2x")
      public val `2x`: String? = null,
    )
  }

  @Serializable
  public data class Usage(
    public val users: Users,
  ) {
    @Serializable
    public data class Users(
      @SerialName("active_month")
      public val activeMonth: Long,
    )
  }
}
