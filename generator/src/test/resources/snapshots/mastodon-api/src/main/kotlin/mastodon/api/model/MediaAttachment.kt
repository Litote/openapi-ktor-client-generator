package mastodon.api.model

import kotlin.Double
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MediaAttachment(
  public val blurhash: String? = null,
  public val description: String? = null,
  public val id: String,
  public val meta: Meta? = null,
  @SerialName("preview_url")
  public val previewUrl: String? = null,
  @SerialName("remote_url")
  public val remoteUrl: String? = null,
  public val type: MediaAttachmentTypeEnum,
  public val url: String? = null,
) {
  @Serializable
  public data class Meta(
    public val focus: Focus? = null,
    public val original: MetaDetails? = null,
    public val small: MetaDetails? = null,
  ) {
    @Serializable
    public data class Focus(
      public val x: Double? = null,
      public val y: Double? = null,
    )
  }
}
