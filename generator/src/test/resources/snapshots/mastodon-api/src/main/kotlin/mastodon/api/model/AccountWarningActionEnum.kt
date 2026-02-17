package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class AccountWarningActionEnum {
  @SerialName("none")
  NONE,
  @SerialName("disable")
  DISABLE,
  @SerialName("mark_statuses_as_sensitive")
  MARKSTATUSESASSENSITIVE,
  @SerialName("delete_statuses")
  DELETESTATUSES,
  @SerialName("sensitive")
  SENSITIVE,
  @SerialName("silence")
  SILENCE,
  @SerialName("suspend")
  SUSPEND,
  ;

  public fun serialName(): String = AccountWarningActionEnum.serializer().descriptor.getElementName(this.ordinal)
}
