package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class List(
  public val exclusive: Boolean,
  public val id: String,
  @SerialName("replies_policy")
  public val repliesPolicy: ListRepliesPolicyEnum,
  public val title: String,
)
