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
  FOLLOW_REQUEST,
  @SerialName("favourite")
  FAVOURITE,
  @SerialName("poll")
  POLL,
  @SerialName("update")
  UPDATE,
  @SerialName("admin.sign_up")
  ADMIN_SIGN_UP,
  @SerialName("admin.report")
  ADMIN_REPORT,
  @SerialName("severed_relationships")
  SEVERED_RELATIONSHIPS,
  @SerialName("moderation_warning")
  MODERATION_WARNING,
  @SerialName("quote")
  QUOTE,
  @SerialName("quoted_update")
  QUOTED_UPDATE,
  ;

  public fun serialName(): String = NotificationTypeEnum.serializer().descriptor.getElementName(this.ordinal)
}
