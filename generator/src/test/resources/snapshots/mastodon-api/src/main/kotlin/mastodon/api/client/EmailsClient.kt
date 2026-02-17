package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.ValidationError

public class EmailsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Resend confirmation email
   */
  public suspend fun createEmailConfirmations(request: JsonElement): CreateEmailConfirmationsResponse {
    try {
      val response = configuration.client.post("api/v1/emails/confirmations") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateEmailConfirmationsResponseSuccess
        401, 403, 404, 429, 503 -> CreateEmailConfirmationsResponseFailure401(response.body<Error>())
        410 -> CreateEmailConfirmationsResponseFailure410
        422 -> CreateEmailConfirmationsResponseFailure(response.body<ValidationError>())
        else -> CreateEmailConfirmationsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateEmailConfirmationsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class CreateEmailConfirmationsResponse

  @Serializable
  public object CreateEmailConfirmationsResponseSuccess : CreateEmailConfirmationsResponse()

  @Serializable
  public data class CreateEmailConfirmationsResponseFailure401(
    public val body: Error,
  ) : CreateEmailConfirmationsResponse()

  @Serializable
  public object CreateEmailConfirmationsResponseFailure410 : CreateEmailConfirmationsResponse()

  @Serializable
  public data class CreateEmailConfirmationsResponseFailure(
    public val body: ValidationError,
  ) : CreateEmailConfirmationsResponse()

  @Serializable
  public data class CreateEmailConfirmationsResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateEmailConfirmationsResponse()
}
