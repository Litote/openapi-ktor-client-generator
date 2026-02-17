package mastodon.api.model

import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GroupedNotificationsResults(
  public val accounts: List<Account>,
  @SerialName("notification_groups")
  public val notificationGroups: List<NotificationGroup>,
  @SerialName("partial_accounts")
  public val partialAccounts: List<PartialAccountWithAvatar>? = null,
  public val statuses: List<Status>,
)
