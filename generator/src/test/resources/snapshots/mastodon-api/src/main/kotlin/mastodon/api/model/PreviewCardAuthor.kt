package mastodon.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class PreviewCardAuthor(
  public val account: Account? = null,
  public val name: String,
  public val url: String,
)
