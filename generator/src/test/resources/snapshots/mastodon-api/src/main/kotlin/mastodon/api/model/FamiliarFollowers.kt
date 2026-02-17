package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class FamiliarFollowers(
  public val accounts: List<Account>,
  public val id: String,
)
