package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class ValidationError(
  public val details: Map<String, List<JsonElement>>,
  public val error: String,
)
