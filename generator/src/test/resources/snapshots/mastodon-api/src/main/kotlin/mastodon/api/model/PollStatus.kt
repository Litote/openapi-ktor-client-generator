package mastodon.api.model

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PollStatus(
  @SerialName("in_reply_to_id")
  override val inReplyToId: String? = null,
  override val language: String? = null,
  public val poll: Poll,
  @SerialName("quote_approval_policy")
  override val quoteApprovalPolicy: String? = null,
  @SerialName("quoted_status_id")
  override val quotedStatusId: String? = null,
  @SerialName("scheduled_at")
  override val scheduledAt: String? = null,
  override val sensitive: Boolean? = false,
  @SerialName("spoiler_text")
  override val spoilerText: String? = null,
  public val status: String? = null,
  override val visibility: StatusVisibilityEnum? = null,
) : CreateStatusRequest(),
    BaseStatus {
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
