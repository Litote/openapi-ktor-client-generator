package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import kotlin.Int
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.DiscoverOauthServerConfigurationResponse
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class WellKnownClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Discover OAuth Server Configuration
   */
  public suspend fun getWellKnownOauthAuthorizationServer(): GetWellKnownOauthAuthorizationServerResponse {
    try {
      val response = configuration.client.`get`(".well-known/oauth-authorization-server") {
      }
      return when (response.status.value) {
        200 -> GetWellKnownOauthAuthorizationServerResponseSuccess(response.body<DiscoverOauthServerConfigurationResponse>(), response.headers)
        401, 404, 429, 503 -> GetWellKnownOauthAuthorizationServerResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetWellKnownOauthAuthorizationServerResponseFailure410(response.headers)
        422 -> GetWellKnownOauthAuthorizationServerResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetWellKnownOauthAuthorizationServerResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetWellKnownOauthAuthorizationServerResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetWellKnownOauthAuthorizationServerResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetWellKnownOauthAuthorizationServerResponseSuccess(
    public val body: DiscoverOauthServerConfigurationResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetWellKnownOauthAuthorizationServerResponse() {
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
  public data class GetWellKnownOauthAuthorizationServerResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetWellKnownOauthAuthorizationServerResponse()

  @Serializable
  public data class GetWellKnownOauthAuthorizationServerResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetWellKnownOauthAuthorizationServerResponse()

  @Serializable
  public data class GetWellKnownOauthAuthorizationServerResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetWellKnownOauthAuthorizationServerResponse()

  @Serializable
  public data class GetWellKnownOauthAuthorizationServerResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetWellKnownOauthAuthorizationServerResponse()
}
