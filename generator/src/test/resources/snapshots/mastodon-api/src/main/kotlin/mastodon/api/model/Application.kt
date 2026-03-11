package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Application(
  public val id: String,
  public val name: String,
  @SerialName("redirect_uris")
  public val redirectUris: List<String>? = null,
  public val scopes: OAuthScopes? = null,
  public val website: String? = null,
)
