package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class IdentityProof(
  @SerialName("profile_url")
  public val profileUrl: String,
  @SerialName("proof_url")
  public val proofUrl: String,
  public val provider: String,
  @SerialName("provider_username")
  public val providerUsername: String,
  @SerialName("updated_at")
  public val updatedAt: String,
)
