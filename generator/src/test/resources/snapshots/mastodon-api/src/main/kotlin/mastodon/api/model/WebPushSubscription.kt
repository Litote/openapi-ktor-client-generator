package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class WebPushSubscription(
  public val alerts: Alerts,
  public val endpoint: String,
  public val id: String,
  @SerialName("server_key")
  public val serverKey: String,
  public val standard: Boolean? = null,
) {
  @Serializable
  public data class Alerts(
    @SerialName("admin.report")
    public val adminReport: Boolean,
    @SerialName("admin.sign_up")
    public val adminSignUp: Boolean,
    public val favourite: Boolean,
    public val follow: Boolean,
    @SerialName("follow_request")
    public val followRequest: Boolean,
    public val mention: Boolean,
    public val poll: Boolean,
    public val reblog: Boolean,
    public val status: Boolean,
    public val update: Boolean,
  )
}
