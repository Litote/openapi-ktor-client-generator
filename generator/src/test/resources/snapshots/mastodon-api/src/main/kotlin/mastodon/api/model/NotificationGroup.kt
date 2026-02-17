package mastodon.api.model

import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class NotificationGroup(
  public val event: JsonElement? = null,
  @SerialName("group_key")
  public val groupKey: String,
  @SerialName("latest_page_notification_at")
  public val latestPageNotificationAt: String? = null,
  @SerialName("moderation_warning")
  public val moderationWarning: JsonElement? = null,
  @SerialName("most_recent_notification_id")
  public val mostRecentNotificationId: Long,
  @SerialName("notifications_count")
  public val notificationsCount: Long,
  @SerialName("page_max_id")
  public val pageMaxId: String? = null,
  @SerialName("page_min_id")
  public val pageMinId: String? = null,
  public val report: JsonElement? = null,
  @SerialName("sample_account_ids")
  public val sampleAccountIds: List<String>,
  @SerialName("status_id")
  public val statusId: String? = null,
  public val type: NotificationTypeEnum,
)
