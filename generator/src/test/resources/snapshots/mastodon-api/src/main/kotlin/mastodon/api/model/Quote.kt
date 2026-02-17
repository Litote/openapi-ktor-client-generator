package mastodon.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class Quote(
  @SerialName("quoted_status")
  public val quotedStatus: JsonElement? = null,
  public val state: QuoteStateEnum,
)
