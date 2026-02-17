package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.ValidationError
import mastodon.api.model.WebPushSubscription

public class PushClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get current subscription
   */
  public suspend fun getPushSubscription(): GetPushSubscriptionResponse {
    try {
      val response = configuration.client.`get`("api/v1/push/subscription") {
      }
      return when (response.status.value) {
        200 -> GetPushSubscriptionResponseSuccess(response.body<WebPushSubscription>())
        401, 404, 429, 503 -> GetPushSubscriptionResponseFailure401(response.body<Error>())
        410 -> GetPushSubscriptionResponseFailure410
        422 -> GetPushSubscriptionResponseFailure(response.body<ValidationError>())
        else -> GetPushSubscriptionResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetPushSubscriptionResponseUnknownFailure(500)
    }
  }

  /**
   * Change types of notifications
   */
  public suspend fun putPushSubscription(request: JsonElement): PutPushSubscriptionResponse {
    try {
      val response = configuration.client.put("api/v1/push/subscription") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PutPushSubscriptionResponseSuccess(response.body<WebPushSubscription>())
        401, 404, 429, 503 -> PutPushSubscriptionResponseFailure401(response.body<Error>())
        410 -> PutPushSubscriptionResponseFailure410
        422 -> PutPushSubscriptionResponseFailure(response.body<ValidationError>())
        else -> PutPushSubscriptionResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PutPushSubscriptionResponseUnknownFailure(500)
    }
  }

  /**
   * Subscribe to push notifications
   */
  public suspend fun createPushSubscription(request: JsonElement): CreatePushSubscriptionResponse {
    try {
      val response = configuration.client.post("api/v1/push/subscription") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreatePushSubscriptionResponseSuccess(response.body<WebPushSubscription>())
        401, 404, 429, 503 -> CreatePushSubscriptionResponseFailure401(response.body<Error>())
        410 -> CreatePushSubscriptionResponseFailure410
        422 -> CreatePushSubscriptionResponseFailure(response.body<ValidationError>())
        else -> CreatePushSubscriptionResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreatePushSubscriptionResponseUnknownFailure(500)
    }
  }

  /**
   * Remove current subscription
   */
  public suspend fun deletePushSubscription(): DeletePushSubscriptionResponse {
    try {
      val response = configuration.client.delete("api/v1/push/subscription") {
      }
      return when (response.status.value) {
        200 -> DeletePushSubscriptionResponseSuccess
        401, 404, 429, 503 -> DeletePushSubscriptionResponseFailure401(response.body<Error>())
        410 -> DeletePushSubscriptionResponseFailure410
        422 -> DeletePushSubscriptionResponseFailure(response.body<ValidationError>())
        else -> DeletePushSubscriptionResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DeletePushSubscriptionResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetPushSubscriptionResponse

  @Serializable
  public data class GetPushSubscriptionResponseSuccess(
    public val body: WebPushSubscription,
  ) : GetPushSubscriptionResponse()

  @Serializable
  public data class GetPushSubscriptionResponseFailure401(
    public val body: Error,
  ) : GetPushSubscriptionResponse()

  @Serializable
  public object GetPushSubscriptionResponseFailure410 : GetPushSubscriptionResponse()

  @Serializable
  public data class GetPushSubscriptionResponseFailure(
    public val body: ValidationError,
  ) : GetPushSubscriptionResponse()

  @Serializable
  public data class GetPushSubscriptionResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetPushSubscriptionResponse()

  @Serializable
  public sealed class PutPushSubscriptionResponse

  @Serializable
  public data class PutPushSubscriptionResponseSuccess(
    public val body: WebPushSubscription,
  ) : PutPushSubscriptionResponse()

  @Serializable
  public data class PutPushSubscriptionResponseFailure401(
    public val body: Error,
  ) : PutPushSubscriptionResponse()

  @Serializable
  public object PutPushSubscriptionResponseFailure410 : PutPushSubscriptionResponse()

  @Serializable
  public data class PutPushSubscriptionResponseFailure(
    public val body: ValidationError,
  ) : PutPushSubscriptionResponse()

  @Serializable
  public data class PutPushSubscriptionResponseUnknownFailure(
    public val statusCode: Int,
  ) : PutPushSubscriptionResponse()

  @Serializable
  public sealed class CreatePushSubscriptionResponse

  @Serializable
  public data class CreatePushSubscriptionResponseSuccess(
    public val body: WebPushSubscription,
  ) : CreatePushSubscriptionResponse()

  @Serializable
  public data class CreatePushSubscriptionResponseFailure401(
    public val body: Error,
  ) : CreatePushSubscriptionResponse()

  @Serializable
  public object CreatePushSubscriptionResponseFailure410 : CreatePushSubscriptionResponse()

  @Serializable
  public data class CreatePushSubscriptionResponseFailure(
    public val body: ValidationError,
  ) : CreatePushSubscriptionResponse()

  @Serializable
  public data class CreatePushSubscriptionResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreatePushSubscriptionResponse()

  @Serializable
  public sealed class DeletePushSubscriptionResponse

  @Serializable
  public object DeletePushSubscriptionResponseSuccess : DeletePushSubscriptionResponse()

  @Serializable
  public data class DeletePushSubscriptionResponseFailure401(
    public val body: Error,
  ) : DeletePushSubscriptionResponse()

  @Serializable
  public object DeletePushSubscriptionResponseFailure410 : DeletePushSubscriptionResponse()

  @Serializable
  public data class DeletePushSubscriptionResponseFailure(
    public val body: ValidationError,
  ) : DeletePushSubscriptionResponse()

  @Serializable
  public data class DeletePushSubscriptionResponseUnknownFailure(
    public val statusCode: Int,
  ) : DeletePushSubscriptionResponse()
}
