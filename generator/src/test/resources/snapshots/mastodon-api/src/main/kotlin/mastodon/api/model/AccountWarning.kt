package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class AccountWarning(
  public val action: AccountWarningActionEnum,
  public val appeal: JsonElement? = null,
  @SerialName("created_at")
  public val createdAt: String,
  public val id: String,
  @SerialName("status_ids")
  public val statusIds: List<String>? = null,
  @SerialName("target_account")
  public val targetAccount: Account,
  public val text: String,
)
