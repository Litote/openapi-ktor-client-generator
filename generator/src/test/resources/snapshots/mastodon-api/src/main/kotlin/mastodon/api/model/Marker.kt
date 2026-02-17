package mastodon.api.model

import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Marker(
  @SerialName("last_read_id")
  public val lastReadId: String,
  @SerialName("updated_at")
  public val updatedAt: String,
  public val version: Long,
)
