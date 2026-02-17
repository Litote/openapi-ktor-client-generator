package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class NotificationRequest(
  public val account: Account,
  @SerialName("created_at")
  public val createdAt: String,
  public val id: String,
  @SerialName("last_status")
  public val lastStatus: JsonElement? = null,
  @SerialName("notifications_count")
  public val notificationsCount: String,
  @SerialName("updated_at")
  public val updatedAt: String,
)
