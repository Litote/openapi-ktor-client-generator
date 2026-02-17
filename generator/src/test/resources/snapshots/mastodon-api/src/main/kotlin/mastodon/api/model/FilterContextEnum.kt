package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class FilterContextEnum {
  @SerialName("home")
  HOME,
  @SerialName("notifications")
  NOTIFICATIONS,
  @SerialName("public")
  PUBLIC,
  @SerialName("thread")
  THREAD,
  @SerialName("account")
  ACCOUNT,
  ;

  public fun serialName(): String = FilterContextEnum.serializer().descriptor.getElementName(this.ordinal)
}
