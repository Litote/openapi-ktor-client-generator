package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class QuoteStateEnum {
  @SerialName("pending")
  PENDING,
  @SerialName("accepted")
  ACCEPTED,
  @SerialName("rejected")
  REJECTED,
  @SerialName("revoked")
  REVOKED,
  @SerialName("deleted")
  DELETED,
  @SerialName("unauthorized")
  UNAUTHORIZED,
  @SerialName("blocked_account")
  BLOCKED_ACCOUNT,
  @SerialName("blocked_domain")
  BLOCKED_DOMAIN,
  @SerialName("muted_account")
  MUTED_ACCOUNT,
  ;

  public fun serialName(): String = QuoteStateEnum.serializer().descriptor.getElementName(this.ordinal)
}
