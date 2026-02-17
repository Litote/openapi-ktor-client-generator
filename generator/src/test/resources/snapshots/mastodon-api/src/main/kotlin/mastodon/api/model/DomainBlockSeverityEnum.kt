package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class DomainBlockSeverityEnum {
  @SerialName("silence")
  SILENCE,
  @SerialName("suspend")
  SUSPEND,
  ;

  public fun serialName(): String = DomainBlockSeverityEnum.serializer().descriptor.getElementName(this.ordinal)
}
