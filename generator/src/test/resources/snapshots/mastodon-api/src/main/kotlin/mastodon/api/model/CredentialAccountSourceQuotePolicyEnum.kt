package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class CredentialAccountSourceQuotePolicyEnum {
  @SerialName("public")
  PUBLIC,
  @SerialName("followers")
  FOLLOWERS,
  @SerialName("nobody")
  NOBODY,
  ;

  public fun serialName(): String = CredentialAccountSourceQuotePolicyEnum.serializer().descriptor.getElementName(this.ordinal)
}
