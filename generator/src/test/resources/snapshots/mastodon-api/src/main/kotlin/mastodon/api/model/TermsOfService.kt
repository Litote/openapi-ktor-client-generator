package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TermsOfService(
  public val content: String,
  public val effective: Boolean,
  @SerialName("effective_date")
  public val effectiveDate: String,
  @SerialName("succeeded_by")
  public val succeededBy: String? = null,
)
