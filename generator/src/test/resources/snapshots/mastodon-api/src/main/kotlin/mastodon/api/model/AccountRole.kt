package mastodon.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class AccountRole(
  public val color: String,
  public val id: String,
  public val name: String,
)
