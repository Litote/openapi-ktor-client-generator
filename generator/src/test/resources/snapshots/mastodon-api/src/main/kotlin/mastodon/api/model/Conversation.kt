package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Conversation(
  public val accounts: List<Account>,
  public val id: String,
  @SerialName("last_status")
  public val lastStatus: Status? = null,
  public val unread: Boolean,
)
