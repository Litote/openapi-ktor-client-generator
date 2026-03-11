package mastodon.api.model

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PollStatus(
  public val poll: Poll,
  public val status: String? = null,
) : CreateStatusRequest() {
  @Serializable
  public data class Poll(
    @SerialName("expires_in")
    public val expiresIn: Long? = null,
    @SerialName("hide_totals")
    public val hideTotals: Boolean? = null,
    public val multiple: Boolean? = null,
    public val options: List<String>? = null,
  )
}
