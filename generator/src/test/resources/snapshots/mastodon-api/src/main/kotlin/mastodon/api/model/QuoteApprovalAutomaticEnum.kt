package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class QuoteApprovalAutomaticEnum {
  @SerialName("public")
  PUBLIC,
  @SerialName("followers")
  FOLLOWERS,
  @SerialName("following")
  FOLLOWING,
  @SerialName("unsupported_policy")
  UNSUPPORTEDPOLICY,
  ;

  public fun serialName(): String = QuoteApprovalAutomaticEnum.serializer().descriptor.getElementName(this.ordinal)
}
