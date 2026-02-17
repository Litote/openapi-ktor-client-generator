package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ExtendedDescription(
  public val content: String,
  @SerialName("updated_at")
  public val updatedAt: String,
)
