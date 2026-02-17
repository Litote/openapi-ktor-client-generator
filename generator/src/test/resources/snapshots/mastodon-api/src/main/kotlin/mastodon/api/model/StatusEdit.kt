package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class StatusEdit(
  public val account: Account,
  public val content: String,
  @SerialName("created_at")
  public val createdAt: String,
  public val emojis: List<CustomEmoji>,
  @SerialName("media_attachments")
  public val mediaAttachments: List<MediaAttachment>,
  public val poll: JsonElement? = null,
  public val quote: JsonElement? = null,
  public val sensitive: Boolean,
  @SerialName("spoiler_text")
  public val spoilerText: String,
)
