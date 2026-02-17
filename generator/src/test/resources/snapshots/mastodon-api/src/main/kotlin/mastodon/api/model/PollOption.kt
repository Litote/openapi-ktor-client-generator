package mastodon.api.model

import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PollOption(
  public val title: String,
  @SerialName("votes_count")
  public val votesCount: Long? = null,
)
