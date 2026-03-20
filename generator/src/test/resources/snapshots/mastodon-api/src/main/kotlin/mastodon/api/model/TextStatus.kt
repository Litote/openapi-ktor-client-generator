package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TextStatus(
  @SerialName("in_reply_to_id")
  override val inReplyToId: String? = null,
  override val language: String? = null,
  @SerialName("quote_approval_policy")
  override val quoteApprovalPolicy: String? = null,
  @SerialName("quoted_status_id")
  override val quotedStatusId: String? = null,
  @SerialName("scheduled_at")
  override val scheduledAt: String? = null,
  override val sensitive: Boolean? = false,
  @SerialName("spoiler_text")
  override val spoilerText: String? = null,
  public val status: String,
  override val visibility: StatusVisibilityEnum? = null,
) : CreateStatusRequest(),
    BaseStatus
