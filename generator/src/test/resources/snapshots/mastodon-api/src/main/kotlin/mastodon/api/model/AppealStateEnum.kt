package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class AppealStateEnum {
  @SerialName("approved")
  APPROVED,
  @SerialName("rejected")
  REJECTED,
  @SerialName("pending")
  PENDING,
  ;

  public fun serialName(): String = AppealStateEnum.serializer().descriptor.getElementName(this.ordinal)
}
