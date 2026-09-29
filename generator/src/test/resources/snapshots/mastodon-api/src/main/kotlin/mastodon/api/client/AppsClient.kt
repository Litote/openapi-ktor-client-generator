package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Application
import mastodon.api.model.CredentialApplication
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class AppsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Create an application
   */
  public suspend fun createApp(request: CreateAppRequest): CreateAppResponse {
    try {
      val response = configuration.client.post("api/v1/apps") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateAppResponseSuccess(response.body<CredentialApplication>(), response.headers)
        401, 404, 422, 429, 503 -> CreateAppResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateAppResponseFailure(response.headers)
        else -> CreateAppResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateAppResponseUnknownFailure(500)
    }
  }

  /**
   * Verify your app works
   */
  public suspend fun getAppsVerifyCredentials(): GetAppsVerifyCredentialsResponse {
    try {
      val response = configuration.client.`get`("api/v1/apps/verify_credentials") {
      }
      return when (response.status.value) {
        200 -> GetAppsVerifyCredentialsResponseSuccess(response.body<Application>(), response.headers)
        401, 404, 429, 503 -> GetAppsVerifyCredentialsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAppsVerifyCredentialsResponseFailure410(response.headers)
        422 -> GetAppsVerifyCredentialsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAppsVerifyCredentialsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAppsVerifyCredentialsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public data class CreateAppRequest(
    @SerialName("client_name")
    public val clientName: String,
    @SerialName("redirect_uris")
    public val redirectUris: List<String>,
    public val scopes: String? = "read",
    public val website: String? = null,
  )

  @Serializable
  public sealed class CreateAppResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateAppResponseSuccess(
    public val body: CredentialApplication,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAppResponse() {
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
  public data class CreateAppResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAppResponse()

  @Serializable
  public data class CreateAppResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAppResponse()

  @Serializable
  public data class CreateAppResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAppResponse()

  @Serializable
  public sealed class GetAppsVerifyCredentialsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAppsVerifyCredentialsResponseSuccess(
    public val body: Application,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAppsVerifyCredentialsResponse() {
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
  public data class GetAppsVerifyCredentialsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAppsVerifyCredentialsResponse()

  @Serializable
  public data class GetAppsVerifyCredentialsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAppsVerifyCredentialsResponse()

  @Serializable
  public data class GetAppsVerifyCredentialsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAppsVerifyCredentialsResponse()

  @Serializable
  public data class GetAppsVerifyCredentialsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAppsVerifyCredentialsResponse()
}
