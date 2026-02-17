package mastodon.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class AnnouncementAccount(
  public val acct: String,
  public val id: String,
  public val url: String,
  public val username: String,
)
