package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class NotificationPolicy(
  @SerialName("for_limited_accounts")
  public val forLimitedAccounts: String,
  @SerialName("for_new_accounts")
  public val forNewAccounts: String,
  @SerialName("for_not_followers")
  public val forNotFollowers: String,
  @SerialName("for_not_following")
  public val forNotFollowing: String,
  @SerialName("for_private_mentions")
  public val forPrivateMentions: String,
  public val summary: JsonElement,
)
