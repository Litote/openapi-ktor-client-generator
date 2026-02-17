package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class RelationshipSeveranceEventTypeEnum {
  @SerialName("domain_block")
  DOMAIN_BLOCK,
  @SerialName("user_domain_block")
  USER_DOMAIN_BLOCK,
  @SerialName("account_suspension")
  ACCOUNT_SUSPENSION,
  ;

  public fun serialName(): String = RelationshipSeveranceEventTypeEnum.serializer().descriptor.getElementName(this.ordinal)
}
