package mastodon.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class AnnouncementStatus(
  public val id: String,
  public val url: String,
)
