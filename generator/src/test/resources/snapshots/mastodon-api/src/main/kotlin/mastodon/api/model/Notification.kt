package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class Notification(
  public val account: Account,
  @SerialName("created_at")
  public val createdAt: String,
  public val event: JsonElement? = null,
  @SerialName("group_key")
  public val groupKey: String? = null,
  public val id: String,
  @SerialName("moderation_warning")
  public val moderationWarning: JsonElement? = null,
  public val report: JsonElement? = null,
  public val status: JsonElement? = null,
  public val type: NotificationTypeEnum,
)
