package mastodon.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Quote(
  @SerialName("quoted_status")
  public val quotedStatus: Status? = null,
  public val state: QuoteStateEnum,
)
