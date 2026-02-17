package mastodon.api.model

import kotlin.Long
import kotlinx.serialization.Serializable

@Serializable
public data class CountResponse(
  public val count: Long,
)
