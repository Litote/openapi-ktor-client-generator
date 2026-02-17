package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
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
  public suspend fun createApp(request: JsonElement): CreateAppResponse {
    try {
      val response = configuration.client.post("api/v1/apps") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateAppResponseSuccess(response.body<CredentialApplication>())
        401, 404, 422, 429, 503 -> CreateAppResponseFailure401(response.body<Error>())
        410 -> CreateAppResponseFailure
        else -> CreateAppResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetAppsVerifyCredentialsResponseSuccess(response.body<Application>())
        401, 404, 429, 503 -> GetAppsVerifyCredentialsResponseFailure401(response.body<Error>())
        410 -> GetAppsVerifyCredentialsResponseFailure410
        422 -> GetAppsVerifyCredentialsResponseFailure(response.body<ValidationError>())
        else -> GetAppsVerifyCredentialsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAppsVerifyCredentialsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class CreateAppResponse

  @Serializable
  public data class CreateAppResponseSuccess(
    public val body: CredentialApplication,
  ) : CreateAppResponse()

  @Serializable
  public data class CreateAppResponseFailure401(
    public val body: Error,
  ) : CreateAppResponse()

  @Serializable
  public object CreateAppResponseFailure : CreateAppResponse()

  @Serializable
  public data class CreateAppResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateAppResponse()

  @Serializable
  public sealed class GetAppsVerifyCredentialsResponse

  @Serializable
  public data class GetAppsVerifyCredentialsResponseSuccess(
    public val body: Application,
  ) : GetAppsVerifyCredentialsResponse()

  @Serializable
  public data class GetAppsVerifyCredentialsResponseFailure401(
    public val body: Error,
  ) : GetAppsVerifyCredentialsResponse()

  @Serializable
  public object GetAppsVerifyCredentialsResponseFailure410 : GetAppsVerifyCredentialsResponse()

  @Serializable
  public data class GetAppsVerifyCredentialsResponseFailure(
    public val body: ValidationError,
  ) : GetAppsVerifyCredentialsResponse()

  @Serializable
  public data class GetAppsVerifyCredentialsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAppsVerifyCredentialsResponse()
}
