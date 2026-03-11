package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Notification(
  public val account: Account,
  @SerialName("created_at")
  public val createdAt: String,
  public val event: RelationshipSeveranceEvent? = null,
  @SerialName("group_key")
  public val groupKey: String? = null,
  public val id: String,
  @SerialName("moderation_warning")
  public val moderationWarning: AccountWarning? = null,
  public val report: Report? = null,
  public val status: Status? = null,
  public val type: NotificationTypeEnum,
)
