package mastodon.api.model

import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class PreviewCardAuthor(
  public val account: JsonElement? = null,
  public val name: String,
  public val url: String,
)
