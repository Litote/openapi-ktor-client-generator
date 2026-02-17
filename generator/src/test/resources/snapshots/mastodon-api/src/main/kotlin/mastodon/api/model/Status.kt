package mastodon.api.model

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class Status(
  public val account: Account,
  public val application: JsonElement? = null,
  public val bookmarked: Boolean? = null,
  public val card: JsonElement? = null,
  public val content: String,
  @SerialName("created_at")
  public val createdAt: String,
  @SerialName("edited_at")
  public val editedAt: String? = null,
  public val emojis: List<CustomEmoji>,
  public val favourited: Boolean? = null,
  @SerialName("favourites_count")
  public val favouritesCount: Long,
  public val filtered: List<FilterResult>? = null,
  public val id: String,
  @SerialName("in_reply_to_account_id")
  public val inReplyToAccountId: String? = null,
  @SerialName("in_reply_to_id")
  public val inReplyToId: String? = null,
  public val language: String? = null,
  @SerialName("media_attachments")
  public val mediaAttachments: List<MediaAttachment>,
  public val mentions: List<StatusMention>,
  public val muted: Boolean? = null,
  public val pinned: Boolean? = null,
  public val poll: JsonElement? = null,
  public val quote: JsonElement? = null,
  @SerialName("quote_approval")
  public val quoteApproval: JsonElement? = null,
  @SerialName("quotes_count")
  public val quotesCount: Long? = null,
  public val reblog: JsonElement? = null,
  public val reblogged: Boolean? = null,
  @SerialName("reblogs_count")
  public val reblogsCount: Long,
  @SerialName("replies_count")
  public val repliesCount: Long,
  public val sensitive: Boolean,
  @SerialName("spoiler_text")
  public val spoilerText: String,
  public val tags: List<StatusTag>,
  public val text: String? = null,
  public val uri: String,
  public val url: String? = null,
  public val visibility: StatusVisibilityEnum,
)
