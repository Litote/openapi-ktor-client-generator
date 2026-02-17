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
  BLOCKEDACCOUNT,
  @SerialName("blocked_domain")
  BLOCKEDDOMAIN,
  @SerialName("muted_account")
  MUTEDACCOUNT,
  ;

  public fun serialName(): String = QuoteStateEnum.serializer().descriptor.getElementName(this.ordinal)
}
