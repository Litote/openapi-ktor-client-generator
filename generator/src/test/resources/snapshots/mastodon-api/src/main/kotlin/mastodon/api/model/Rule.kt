package mastodon.api.model

import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class Rule(
  public val hint: String? = null,
  public val id: String,
  public val text: String,
  public val translations: JsonElement? = null,
)
