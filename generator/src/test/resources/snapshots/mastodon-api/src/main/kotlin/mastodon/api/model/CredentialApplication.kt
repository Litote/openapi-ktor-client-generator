package mastodon.api.model

import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class CredentialApplication(
  @SerialName("client_id")
  public val clientId: String,
  @SerialName("client_secret")
  public val clientSecret: String,
  @SerialName("client_secret_expires_at")
  public val clientSecretExpiresAt: Long? = null,
  public val id: String,
  public val name: String,
  @SerialName("redirect_uris")
  public val redirectUris: List<String>? = null,
  public val scopes: JsonElement? = null,
  public val website: String? = null,
)
