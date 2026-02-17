package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class QuoteApprovalCurrentUserEnum {
  @SerialName("automatic")
  AUTOMATIC,
  @SerialName("manual")
  MANUAL,
  @SerialName("denied")
  DENIED,
  @SerialName("unknown")
  UNKNOWN,
  ;

  public fun serialName(): String = QuoteApprovalCurrentUserEnum.serializer().descriptor.getElementName(this.ordinal)
}
