package mastodon.api.model

import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class Search(
  public val accounts: List<Account>,
  public val hashtags: List<Tag>,
  public val statuses: List<Status>,
)
