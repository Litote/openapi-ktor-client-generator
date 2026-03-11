package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
  public suspend fun putPushSubscription(request: PutPushSubscriptionRequest): PutPushSubscriptionResponse {
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
  public suspend fun createPushSubscription(request: CreatePushSubscriptionRequest): CreatePushSubscriptionResponse {
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
  public data class PutPushSubscriptionRequest(
    public val `data`: Data? = null,
    public val policy: String? = null,
  ) {
    @Serializable
    public data class Data(
      public val alerts: Alerts? = null,
    ) {
      @Serializable
      public data class Alerts(
        @SerialName("admin.report")
        public val adminReport: Boolean? = null,
        @SerialName("admin.sign_up")
        public val adminSignUp: Boolean? = null,
        public val favourite: Boolean? = null,
        public val follow: Boolean? = null,
        @SerialName("follow_request")
        public val followRequest: Boolean? = null,
        public val mention: Boolean? = null,
        public val poll: Boolean? = null,
        public val reblog: Boolean? = null,
        public val status: Boolean? = null,
        public val update: Boolean? = null,
      )
    }
  }

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
  public data class CreatePushSubscriptionRequest(
    public val `data`: Data? = null,
    public val subscription: Subscription,
  ) {
    @Serializable
    public data class Data(
      public val alerts: Alerts? = null,
      public val policy: String? = null,
    ) {
      @Serializable
      public data class Alerts(
        @SerialName("admin.report")
        public val adminReport: Boolean? = null,
        @SerialName("admin.sign_up")
        public val adminSignUp: Boolean? = null,
        public val favourite: Boolean? = null,
        public val follow: Boolean? = null,
        @SerialName("follow_request")
        public val followRequest: Boolean? = null,
        public val mention: Boolean? = null,
        public val poll: Boolean? = null,
        public val quote: Boolean? = null,
        @SerialName("quoted_update")
        public val quotedUpdate: Boolean? = null,
        public val reblog: Boolean? = null,
        public val status: Boolean? = null,
        public val update: Boolean? = null,
      )
    }

    @Serializable
    public data class Subscription(
      public val endpoint: String? = null,
      public val keys: Keys? = null,
      public val standard: Boolean? = null,
    ) {
      @Serializable
      public data class Keys(
        public val auth: String? = null,
        public val p256dh: String? = null,
      )
    }
  }

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
