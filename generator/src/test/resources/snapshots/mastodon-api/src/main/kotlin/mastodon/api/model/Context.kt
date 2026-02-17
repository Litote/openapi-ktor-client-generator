package mastodon.api.model

import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class Context(
  public val ancestors: List<Status>,
  public val descendants: List<Status>,
)
