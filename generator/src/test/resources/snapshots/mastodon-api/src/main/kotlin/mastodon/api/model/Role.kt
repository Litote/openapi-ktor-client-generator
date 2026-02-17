package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class Role(
  public val color: String,
  public val highlighted: Boolean,
  public val id: String,
  public val name: String,
  public val permissions: String,
)
