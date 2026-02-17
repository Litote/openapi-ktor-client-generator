package mastodon.api.model

import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MetaDetails(
  public val aspect: Double? = null,
  public val bitrate: Long? = null,
  public val duration: Double? = null,
  @SerialName("frame_rate")
  public val frameRate: String? = null,
  public val height: Long? = null,
  public val width: Long? = null,
)
