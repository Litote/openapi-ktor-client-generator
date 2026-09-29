package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
        200 -> GetOauthAuthorizeResponseSuccess(response.headers)
        400, 401, 404, 429, 503 -> GetOauthAuthorizeResponseFailure400(response.body<Error>(), response.headers)
        410 -> GetOauthAuthorizeResponseFailure410(response.headers)
        422 -> GetOauthAuthorizeResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetOauthAuthorizeResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetOauthAuthorizeResponseUnknownFailure(500)
    }
  }

  /**
   * Revoke a token
   */
  public suspend fun postOauthRevoke(request: PostOauthRevokeRequest): PostOauthRevokeResponse {
    try {
      val response = configuration.client.post("oauth/revoke") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostOauthRevokeResponseSuccess(response.headers)
        401, 403, 404, 429, 503 -> PostOauthRevokeResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostOauthRevokeResponseFailure410(response.headers)
        422 -> PostOauthRevokeResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostOauthRevokeResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostOauthRevokeResponseUnknownFailure(500)
    }
  }

  /**
   * Obtain a token
   */
  public suspend fun postOauthToken(request: PostOauthTokenRequest): PostOauthTokenResponse {
    try {
      val response = configuration.client.post("oauth/token") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostOauthTokenResponseSuccess(response.body<Token>(), response.headers)
        400, 401, 404, 429, 503 -> PostOauthTokenResponseFailure400(response.body<Error>(), response.headers)
        410 -> PostOauthTokenResponseFailure410(response.headers)
        422 -> PostOauthTokenResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PostOauthTokenResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetOauthUserinfoResponseSuccess(response.headers)
        401, 403, 404, 429, 503 -> GetOauthUserinfoResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetOauthUserinfoResponseFailure410(response.headers)
        422 -> GetOauthUserinfoResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetOauthUserinfoResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetOauthUserinfoResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetOauthAuthorizeResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetOauthAuthorizeResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetOauthAuthorizeResponse() {
    /**
     * Number of requests permitted per time period
     */
    public val xRateLimitLimit: Int?
      get() = headers["X-RateLimit-Limit"]?.toIntOrNull()

    /**
     * Number of requests you can still make
     */
    public val xRateLimitRemaining: Int?
      get() = headers["X-RateLimit-Remaining"]?.toIntOrNull()

    /**
     * Timestamp when your rate limit will reset
     */
    public val xRateLimitReset: String?
      get() = headers["X-RateLimit-Reset"]
  }

  @Serializable
  public data class GetOauthAuthorizeResponseFailure400(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetOauthAuthorizeResponse()

  @Serializable
  public data class GetOauthAuthorizeResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetOauthAuthorizeResponse()

  @Serializable
  public data class GetOauthAuthorizeResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetOauthAuthorizeResponse()

  @Serializable
  public data class GetOauthAuthorizeResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetOauthAuthorizeResponse()

  @Serializable
  public data class PostOauthRevokeRequest(
    @SerialName("client_id")
    public val clientId: String,
    @SerialName("client_secret")
    public val clientSecret: String,
    public val token: String,
  )

  @Serializable
  public sealed class PostOauthRevokeResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostOauthRevokeResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostOauthRevokeResponse() {
    /**
     * Number of requests permitted per time period
     */
    public val xRateLimitLimit: Int?
      get() = headers["X-RateLimit-Limit"]?.toIntOrNull()

    /**
     * Number of requests you can still make
     */
    public val xRateLimitRemaining: Int?
      get() = headers["X-RateLimit-Remaining"]?.toIntOrNull()

    /**
     * Timestamp when your rate limit will reset
     */
    public val xRateLimitReset: String?
      get() = headers["X-RateLimit-Reset"]
  }

  @Serializable
  public data class PostOauthRevokeResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostOauthRevokeResponse()

  @Serializable
  public data class PostOauthRevokeResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostOauthRevokeResponse()

  @Serializable
  public data class PostOauthRevokeResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostOauthRevokeResponse()

  @Serializable
  public data class PostOauthRevokeResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostOauthRevokeResponse()

  @Serializable
  public data class PostOauthTokenRequest(
    @SerialName("client_id")
    public val clientId: String,
    @SerialName("client_secret")
    public val clientSecret: String,
    public val code: String,
    @SerialName("code_verifier")
    public val codeVerifier: String? = null,
    @SerialName("grant_type")
    public val grantType: String,
    @SerialName("redirect_uri")
    public val redirectUri: String,
    public val scope: String? = "read",
  )

  @Serializable
  public sealed class PostOauthTokenResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostOauthTokenResponseSuccess(
    public val body: Token,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostOauthTokenResponse() {
    /**
     * Number of requests permitted per time period
     */
    public val xRateLimitLimit: Int?
      get() = headers["X-RateLimit-Limit"]?.toIntOrNull()

    /**
     * Number of requests you can still make
     */
    public val xRateLimitRemaining: Int?
      get() = headers["X-RateLimit-Remaining"]?.toIntOrNull()

    /**
     * Timestamp when your rate limit will reset
     */
    public val xRateLimitReset: String?
      get() = headers["X-RateLimit-Reset"]
  }

  @Serializable
  public data class PostOauthTokenResponseFailure400(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostOauthTokenResponse()

  @Serializable
  public data class PostOauthTokenResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostOauthTokenResponse()

  @Serializable
  public data class PostOauthTokenResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostOauthTokenResponse()

  @Serializable
  public data class PostOauthTokenResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostOauthTokenResponse()

  @Serializable
  public sealed class GetOauthUserinfoResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetOauthUserinfoResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetOauthUserinfoResponse() {
    /**
     * Number of requests permitted per time period
     */
    public val xRateLimitLimit: Int?
      get() = headers["X-RateLimit-Limit"]?.toIntOrNull()

    /**
     * Number of requests you can still make
     */
    public val xRateLimitRemaining: Int?
      get() = headers["X-RateLimit-Remaining"]?.toIntOrNull()

    /**
     * Timestamp when your rate limit will reset
     */
    public val xRateLimitReset: String?
      get() = headers["X-RateLimit-Reset"]
  }

  @Serializable
  public data class GetOauthUserinfoResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetOauthUserinfoResponse()

  @Serializable
  public data class GetOauthUserinfoResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetOauthUserinfoResponse()

  @Serializable
  public data class GetOauthUserinfoResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetOauthUserinfoResponse()

  @Serializable
  public data class GetOauthUserinfoResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetOauthUserinfoResponse()
}
