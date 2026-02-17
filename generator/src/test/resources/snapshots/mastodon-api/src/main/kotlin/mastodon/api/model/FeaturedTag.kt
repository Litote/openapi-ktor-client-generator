package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class FeaturedTag(
  public val id: String,
  @SerialName("last_status_at")
  public val lastStatusAt: String? = null,
  public val name: String,
  @SerialName("statuses_count")
  public val statusesCount: String,
  public val url: String,
)
