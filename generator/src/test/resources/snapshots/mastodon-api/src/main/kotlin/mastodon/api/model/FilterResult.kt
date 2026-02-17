package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class FilterResult(
  public val filter: Filter,
  @SerialName("keyword_matches")
  public val keywordMatches: List<String>? = null,
  @SerialName("status_matches")
  public val statusMatches: List<String>? = null,
)
