package mastodon.api.model

import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Reaction(
  public val count: Long,
  public val me: Boolean? = null,
  public val name: String,
  @SerialName("static_url")
  public val staticUrl: String? = null,
  public val url: String? = null,
)
