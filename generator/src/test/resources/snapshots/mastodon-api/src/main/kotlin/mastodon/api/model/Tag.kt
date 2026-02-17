package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class Tag(
  public val featuring: Boolean? = null,
  public val following: Boolean? = null,
  public val history: List<TagHistory>,
  public val id: String? = null,
  public val name: String,
  public val url: String,
)
