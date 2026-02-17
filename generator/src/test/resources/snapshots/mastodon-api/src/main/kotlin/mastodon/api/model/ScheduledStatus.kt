package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class ScheduledStatus(
  public val id: String,
  @SerialName("media_attachments")
  public val mediaAttachments: List<MediaAttachment>,
  public val params: JsonElement,
  @SerialName("scheduled_at")
  public val scheduledAt: String,
)
