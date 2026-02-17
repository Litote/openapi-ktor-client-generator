package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class MediaAttachment(
  public val blurhash: String? = null,
  public val description: String? = null,
  public val id: String,
  public val meta: JsonElement? = null,
  @SerialName("preview_url")
  public val previewUrl: String? = null,
  @SerialName("remote_url")
  public val remoteUrl: String? = null,
  public val type: MediaAttachmentTypeEnum,
  public val url: String? = null,
)
