package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class FilterStatus(
  public val id: String,
  @SerialName("status_id")
  public val statusId: String,
)
