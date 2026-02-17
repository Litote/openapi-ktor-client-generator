package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ShallowQuote(
  @SerialName("quoted_status_id")
  public val quotedStatusId: String? = null,
  public val state: QuoteStateEnum,
)
