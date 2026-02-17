package mastodon.api.model

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class Account(
  public val acct: String,
  public val avatar: String,
  @SerialName("avatar_static")
  public val avatarStatic: String,
  public val bot: Boolean,
  @SerialName("created_at")
  public val createdAt: String,
  public val discoverable: Boolean? = null,
  @SerialName("display_name")
  public val displayName: String,
  public val emojis: List<CustomEmoji>,
  public val fields: List<Field>,
  @SerialName("followers_count")
  public val followersCount: Long,
  @SerialName("following_count")
  public val followingCount: Long,
  public val group: Boolean,
  public val `header`: String,
  @SerialName("header_static")
  public val headerStatic: String,
  @SerialName("hide_collections")
  public val hideCollections: Boolean? = null,
  public val id: String,
  public val indexable: Boolean? = null,
  @SerialName("last_status_at")
  public val lastStatusAt: String? = null,
  public val limited: Boolean? = null,
  public val locked: Boolean,
  public val memorial: Boolean? = null,
  public val moved: JsonElement? = null,
  public val noindex: Boolean? = null,
  public val note: String,
  public val roles: List<AccountRole>? = null,
  @SerialName("statuses_count")
  public val statusesCount: Long,
  public val suspended: Boolean? = null,
  public val uri: String,
  public val url: String? = null,
  public val username: String,
)
