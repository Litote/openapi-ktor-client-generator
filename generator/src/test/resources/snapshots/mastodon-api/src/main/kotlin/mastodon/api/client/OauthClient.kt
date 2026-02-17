package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Token
import mastodon.api.model.ValidationError

public class OauthClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Authorize a user
   */
  public suspend fun getOauthAuthorize(
    clientId: String,
    redirectUri: String,
    responseType: String,
    codeChallenge: String? = null,
    codeChallengeMethod: String? = null,
    forceLogin: Boolean? = null,
    lang: String? = null,
    scope: String? = "read",
    state: String? = null,
  ): GetOauthAuthorizeResponse {
    try {
      val response = configuration.client.`get`("oauth/authorize") {
        url {
          parameters.append("client_id", clientId)
          parameters.append("redirect_uri", redirectUri)
          parameters.append("response_type", responseType)
          if (codeChallenge != null) {
            parameters.append("code_challenge", codeChallenge)
          }
          if (codeChallengeMethod != null) {
            parameters.append("code_challenge_method", codeChallengeMethod)
          }
          if (forceLogin != null) {
            parameters.append("force_login", forceLogin.toString())
          }
          if (lang != null) {
            parameters.append("lang", lang)
          }
          if (scope != null) {
            parameters.append("scope", scope)
          }
          if (state != null) {
            parameters.append("state", state)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetOauthAuthorizeResponseSuccess
        400, 401, 404, 429, 503 -> GetOauthAuthorizeResponseFailure400(response.body<Error>())
        410 -> GetOauthAuthorizeResponseFailure410
        422 -> GetOauthAuthorizeResponseFailure(response.body<ValidationError>())
        else -> GetOauthAuthorizeResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetOauthAuthorizeResponseUnknownFailure(500)
    }
  }

  /**
   * Revoke a token
   */
  public suspend fun postOauthRevoke(request: JsonElement): PostOauthRevokeResponse {
    try {
      val response = configuration.client.post("oauth/revoke") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostOauthRevokeResponseSuccess
        401, 403, 404, 429, 503 -> PostOauthRevokeResponseFailure401(response.body<Error>())
        410 -> PostOauthRevokeResponseFailure410
        422 -> PostOauthRevokeResponseFailure(response.body<ValidationError>())
        else -> PostOauthRevokeResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostOauthRevokeResponseUnknownFailure(500)
    }
  }

  /**
   * Obtain a token
   */
  public suspend fun postOauthToken(request: JsonElement): PostOauthTokenResponse {
    try {
      val response = configuration.client.post("oauth/token") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostOauthTokenResponseSuccess(response.body<Token>())
        400, 401, 404, 429, 503 -> PostOauthTokenResponseFailure400(response.body<Error>())
        410 -> PostOauthTokenResponseFailure410
        422 -> PostOauthTokenResponseFailure(response.body<ValidationError>())
        else -> PostOauthTokenResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostOauthTokenResponseUnknownFailure(500)
    }
  }

  /**
   * Retrieve user information
   */
  public suspend fun getOauthUserinfo(): GetOauthUserinfoResponse {
    try {
      val response = configuration.client.`get`("oauth/userinfo") {
      }
      return when (response.status.value) {
        200 -> GetOauthUserinfoResponseSuccess
        401, 403, 404, 429, 503 -> GetOauthUserinfoResponseFailure401(response.body<Error>())
        410 -> GetOauthUserinfoResponseFailure410
        422 -> GetOauthUserinfoResponseFailure(response.body<ValidationError>())
        else -> GetOauthUserinfoResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetOauthUserinfoResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetOauthAuthorizeResponse

  @Serializable
  public object GetOauthAuthorizeResponseSuccess : GetOauthAuthorizeResponse()

  @Serializable
  public data class GetOauthAuthorizeResponseFailure400(
    public val body: Error,
  ) : GetOauthAuthorizeResponse()

  @Serializable
  public object GetOauthAuthorizeResponseFailure410 : GetOauthAuthorizeResponse()

  @Serializable
  public data class GetOauthAuthorizeResponseFailure(
    public val body: ValidationError,
  ) : GetOauthAuthorizeResponse()

  @Serializable
  public data class GetOauthAuthorizeResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetOauthAuthorizeResponse()

  @Serializable
  public sealed class PostOauthRevokeResponse

  @Serializable
  public object PostOauthRevokeResponseSuccess : PostOauthRevokeResponse()

  @Serializable
  public data class PostOauthRevokeResponseFailure401(
    public val body: Error,
  ) : PostOauthRevokeResponse()

  @Serializable
  public object PostOauthRevokeResponseFailure410 : PostOauthRevokeResponse()

  @Serializable
  public data class PostOauthRevokeResponseFailure(
    public val body: ValidationError,
  ) : PostOauthRevokeResponse()

  @Serializable
  public data class PostOauthRevokeResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostOauthRevokeResponse()

  @Serializable
  public sealed class PostOauthTokenResponse

  @Serializable
  public data class PostOauthTokenResponseSuccess(
    public val body: Token,
  ) : PostOauthTokenResponse()

  @Serializable
  public data class PostOauthTokenResponseFailure400(
    public val body: Error,
  ) : PostOauthTokenResponse()

  @Serializable
  public object PostOauthTokenResponseFailure410 : PostOauthTokenResponse()

  @Serializable
  public data class PostOauthTokenResponseFailure(
    public val body: ValidationError,
  ) : PostOauthTokenResponse()

  @Serializable
  public data class PostOauthTokenResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostOauthTokenResponse()

  @Serializable
  public sealed class GetOauthUserinfoResponse

  @Serializable
  public object GetOauthUserinfoResponseSuccess : GetOauthUserinfoResponse()

  @Serializable
  public data class GetOauthUserinfoResponseFailure401(
    public val body: Error,
  ) : GetOauthUserinfoResponse()

  @Serializable
  public object GetOauthUserinfoResponseFailure410 : GetOauthUserinfoResponse()

  @Serializable
  public data class GetOauthUserinfoResponseFailure(
    public val body: ValidationError,
  ) : GetOauthUserinfoResponse()

  @Serializable
  public data class GetOauthUserinfoResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetOauthUserinfoResponse()
}
