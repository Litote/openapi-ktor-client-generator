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
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public interface PreferencesClient {
  /**
   * View user preferences
   */
  public suspend fun getPreferences(): GetPreferencesResponse

  @Serializable
  public sealed class GetPreferencesResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetPreferencesResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPreferencesResponse() {
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
  public data class GetPreferencesResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPreferencesResponse()

  @Serializable
  public data class GetPreferencesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPreferencesResponse()

  @Serializable
  public data class GetPreferencesResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPreferencesResponse()

  @Serializable
  public data class GetPreferencesResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPreferencesResponse()
}

public fun PreferencesClient(configuration: ClientConfiguration = defaultClientConfiguration): PreferencesClient = DefaultPreferencesClient(configuration)

public class DefaultPreferencesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : PreferencesClient {
  override suspend fun getPreferences(): PreferencesClient.GetPreferencesResponse {
    try {
      val response = configuration.client.`get`("api/v1/preferences") {
      }
      return when (response.status.value) {
        200 -> PreferencesClient.GetPreferencesResponseSuccess(response.headers)
        401, 404, 429, 503 -> PreferencesClient.GetPreferencesResponseFailure401(response.body<Error>(), response.headers)
        410 -> PreferencesClient.GetPreferencesResponseFailure410(response.headers)
        422 -> PreferencesClient.GetPreferencesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PreferencesClient.GetPreferencesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PreferencesClient.GetPreferencesResponseUnknownFailure(500)
    }
  }
}
