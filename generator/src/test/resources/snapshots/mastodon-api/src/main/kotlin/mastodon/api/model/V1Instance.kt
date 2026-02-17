package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class V1Instance(
  @SerialName("approval_required")
  public val approvalRequired: Boolean,
  public val configuration: JsonElement,
  @SerialName("contact_account")
  public val contactAccount: JsonElement? = null,
  public val description: String,
  public val email: String,
  @SerialName("invites_enabled")
  public val invitesEnabled: Boolean,
  public val languages: List<String>,
  public val registrations: Boolean,
  public val rules: List<Rule>,
  @SerialName("short_description")
  public val shortDescription: String,
  public val stats: JsonElement,
  public val thumbnail: String? = null,
  public val title: String,
  public val uri: String,
  public val urls: JsonElement,
  public val version: String,
)
