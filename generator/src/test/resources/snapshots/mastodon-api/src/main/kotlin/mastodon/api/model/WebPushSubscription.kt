package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class WebPushSubscription(
  public val alerts: JsonElement,
  public val endpoint: String,
  public val id: String,
  @SerialName("server_key")
  public val serverKey: String,
  public val standard: Boolean? = null,
)
