package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AccountWarning(
  public val action: AccountWarningActionEnum,
  public val appeal: Appeal? = null,
  @SerialName("created_at")
  public val createdAt: String,
  public val id: String,
  @SerialName("status_ids")
  public val statusIds: List<String>? = null,
  @SerialName("target_account")
  public val targetAccount: Account,
  public val text: String,
)
