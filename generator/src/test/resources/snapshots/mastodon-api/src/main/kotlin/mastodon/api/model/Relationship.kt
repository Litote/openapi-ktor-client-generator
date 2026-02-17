package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Relationship(
  @SerialName("blocked_by")
  public val blockedBy: Boolean,
  public val blocking: Boolean,
  @SerialName("domain_blocking")
  public val domainBlocking: Boolean,
  public val endorsed: Boolean,
  @SerialName("followed_by")
  public val followedBy: Boolean,
  public val following: Boolean,
  public val id: String,
  public val languages: List<String>? = null,
  public val muting: Boolean,
  @SerialName("muting_notifications")
  public val mutingNotifications: Boolean,
  public val note: String,
  public val notifying: Boolean,
  public val requested: Boolean,
  @SerialName("requested_by")
  public val requestedBy: Boolean,
  @SerialName("showing_reblogs")
  public val showingReblogs: Boolean,
)
