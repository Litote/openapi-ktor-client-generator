package mastodon.api.model

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ScheduledStatus(
  public val id: String,
  @SerialName("media_attachments")
  public val mediaAttachments: List<MediaAttachment>,
  public val params: Params,
  @SerialName("scheduled_at")
  public val scheduledAt: String,
) : CreateStatusResponse() {
  @Serializable
  public data class Params(
    public val idempotency: String? = null,
    @SerialName("in_reply_to_id")
    public val inReplyToId: Long? = null,
    public val language: String? = null,
    @SerialName("media_ids")
    public val mediaIds: List<String>? = null,
    public val poll: Poll? = null,
    @SerialName("scheduled_at")
    public val scheduledAt: String? = null,
    public val sensitive: Boolean? = null,
    @SerialName("spoiler_text")
    public val spoilerText: String? = null,
    public val text: String,
    public val visibility: StatusVisibilityEnum,
  ) {
    @Serializable
    public data class Poll(
      @SerialName("expires_in")
      public val expiresIn: Long,
      @SerialName("hide_totals")
      public val hideTotals: Boolean,
      public val multiple: Boolean,
      public val options: List<String>,
    )
  }
}
