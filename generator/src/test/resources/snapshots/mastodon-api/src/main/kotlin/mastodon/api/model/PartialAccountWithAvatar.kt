package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PartialAccountWithAvatar(
  public val acct: String,
  public val avatar: String,
  @SerialName("avatar_static")
  public val avatarStatic: String,
  public val bot: Boolean,
  public val id: String,
  public val locked: Boolean,
  public val url: String,
)
