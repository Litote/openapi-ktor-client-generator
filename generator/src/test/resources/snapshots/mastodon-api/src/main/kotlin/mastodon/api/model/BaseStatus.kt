package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class BaseStatus(
  @SerialName("in_reply_to_id")
  public val inReplyToId: String? = null,
  public val language: String? = null,
  @SerialName("quote_approval_policy")
  public val quoteApprovalPolicy: String? = null,
  @SerialName("quoted_status_id")
  public val quotedStatusId: String? = null,
  @SerialName("scheduled_at")
  public val scheduledAt: String? = null,
  public val sensitive: Boolean? = false,
  @SerialName("spoiler_text")
  public val spoilerText: String? = null,
  public val visibility: StatusVisibilityEnum? = null,
)
