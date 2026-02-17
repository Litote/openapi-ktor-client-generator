package mastodon.api.model

import kotlin.Double
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Token(
  @SerialName("access_token")
  public val accessToken: String,
  @SerialName("created_at")
  public val createdAt: Double,
  public val scope: String,
  @SerialName("token_type")
  public val tokenType: String,
)
