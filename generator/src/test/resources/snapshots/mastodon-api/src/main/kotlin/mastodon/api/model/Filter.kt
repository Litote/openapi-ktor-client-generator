package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Filter(
  public val context: List<FilterContextEnum>,
  @SerialName("expires_at")
  public val expiresAt: String? = null,
  @SerialName("filter_action")
  public val filterAction: FilterFilterActionEnum,
  public val id: String,
  public val keywords: List<FilterKeyword>? = null,
  public val statuses: List<FilterStatus>? = null,
  public val title: String,
)
