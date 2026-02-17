package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class RelationshipSeveranceEventTypeEnum {
  @SerialName("domain_block")
  DOMAINBLOCK,
  @SerialName("user_domain_block")
  USERDOMAINBLOCK,
  @SerialName("account_suspension")
  ACCOUNTSUSPENSION,
  ;

  public fun serialName(): String = RelationshipSeveranceEventTypeEnum.serializer().descriptor.getElementName(this.ordinal)
}
