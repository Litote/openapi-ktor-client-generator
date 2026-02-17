package mastodon.api.model

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Poll(
  public val emojis: List<CustomEmoji>,
  public val expired: Boolean,
  @SerialName("expires_at")
  public val expiresAt: String? = null,
  public val id: String,
  public val multiple: Boolean,
  public val options: List<PollOption>,
  @SerialName("own_votes")
  public val ownVotes: List<Long>? = null,
  public val voted: Boolean? = null,
  @SerialName("voters_count")
  public val votersCount: Long? = null,
  @SerialName("votes_count")
  public val votesCount: Long,
)
