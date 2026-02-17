package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Announcement(
  @SerialName("all_day")
  public val allDay: Boolean,
  public val content: String,
  public val emojis: List<CustomEmoji>,
  @SerialName("ends_at")
  public val endsAt: String? = null,
  public val id: String,
  public val mentions: List<AnnouncementAccount>,
  @SerialName("published_at")
  public val publishedAt: String,
  public val reactions: List<Reaction>,
  public val read: Boolean? = null,
  @SerialName("starts_at")
  public val startsAt: String? = null,
  public val statuses: List<AnnouncementStatus>,
  public val tags: List<StatusTag>,
  @SerialName("updated_at")
  public val updatedAt: String,
)
