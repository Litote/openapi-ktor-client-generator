package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class NotificationTypeEnum {
  @SerialName("mention")
  MENTION,
  @SerialName("status")
  STATUS,
  @SerialName("reblog")
  REBLOG,
  @SerialName("follow")
  FOLLOW,
  @SerialName("follow_request")
  FOLLOWREQUEST,
  @SerialName("favourite")
  FAVOURITE,
  @SerialName("poll")
  POLL,
  @SerialName("update")
  UPDATE,
  @SerialName("admin.sign_up")
  ADMINSIGNUP,
  @SerialName("admin.report")
  ADMINREPORT,
  @SerialName("severed_relationships")
  SEVEREDRELATIONSHIPS,
  @SerialName("moderation_warning")
  MODERATIONWARNING,
  @SerialName("quote")
  QUOTE,
  @SerialName("quoted_update")
  QUOTEDUPDATE,
  ;

  public fun serialName(): String = NotificationTypeEnum.serializer().descriptor.getElementName(this.ordinal)
}
