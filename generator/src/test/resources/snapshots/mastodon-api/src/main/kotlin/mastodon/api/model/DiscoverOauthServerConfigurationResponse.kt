package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class DiscoverOauthServerConfigurationResponse(
  @SerialName("app_registration_endpoint")
  public val appRegistrationEndpoint: String,
  @SerialName("authorization_endpoint")
  public val authorizationEndpoint: String,
  @SerialName("code_challenge_methods_supported")
  public val codeChallengeMethodsSupported: List<String>,
  @SerialName("grant_types_supported")
  public val grantTypesSupported: List<String>,
  public val issuer: String,
  @SerialName("response_modes_supported")
  public val responseModesSupported: List<String>,
  @SerialName("response_types_supported")
  public val responseTypesSupported: List<String>,
  @SerialName("revocation_endpoint")
  public val revocationEndpoint: String,
  @SerialName("scopes_supported")
  public val scopesSupported: OAuthScopes,
  @SerialName("service_documentation")
  public val serviceDocumentation: String,
  @SerialName("token_endpoint")
  public val tokenEndpoint: String,
  @SerialName("token_endpoint_auth_methods_supported")
  public val tokenEndpointAuthMethodsSupported: List<String>,
  @SerialName("userinfo_endpoint")
  public val userinfoEndpoint: String,
)
