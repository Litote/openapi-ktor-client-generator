package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
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
import mastodon.api.model.ValidationError
import mastodon.api.model.WebPushSubscription

public interface PushClient {
  /**
   * Get current subscription
   */
  public suspend fun getPushSubscription(): GetPushSubscriptionResponse

  /**
   * Change types of notifications
   */
  public suspend fun putPushSubscription(request: PutPushSubscriptionRequest): PutPushSubscriptionResponse

  /**
   * Subscribe to push notifications
   */
  public suspend fun createPushSubscription(request: CreatePushSubscriptionRequest): CreatePushSubscriptionResponse

  /**
   * Remove current subscription
   */
  public suspend fun deletePushSubscription(): DeletePushSubscriptionResponse

  @Serializable
  public sealed class GetPushSubscriptionResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetPushSubscriptionResponseSuccess(
    public val body: WebPushSubscription,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPushSubscriptionResponse() {
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
  public data class GetPushSubscriptionResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPushSubscriptionResponse()

  @Serializable
  public data class GetPushSubscriptionResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPushSubscriptionResponse()

  @Serializable
  public data class GetPushSubscriptionResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPushSubscriptionResponse()

  @Serializable
  public data class GetPushSubscriptionResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
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
  public sealed class PutPushSubscriptionResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PutPushSubscriptionResponseSuccess(
    public val body: WebPushSubscription,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PutPushSubscriptionResponse() {
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
  public data class PutPushSubscriptionResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PutPushSubscriptionResponse()

  @Serializable
  public data class PutPushSubscriptionResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PutPushSubscriptionResponse()

  @Serializable
  public data class PutPushSubscriptionResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PutPushSubscriptionResponse()

  @Serializable
  public data class PutPushSubscriptionResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
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
  public sealed class CreatePushSubscriptionResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreatePushSubscriptionResponseSuccess(
    public val body: WebPushSubscription,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreatePushSubscriptionResponse() {
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
  public data class CreatePushSubscriptionResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreatePushSubscriptionResponse()

  @Serializable
  public data class CreatePushSubscriptionResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreatePushSubscriptionResponse()

  @Serializable
  public data class CreatePushSubscriptionResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreatePushSubscriptionResponse()

  @Serializable
  public data class CreatePushSubscriptionResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreatePushSubscriptionResponse()

  @Serializable
  public sealed class DeletePushSubscriptionResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DeletePushSubscriptionResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeletePushSubscriptionResponse() {
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
  public data class DeletePushSubscriptionResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeletePushSubscriptionResponse()

  @Serializable
  public data class DeletePushSubscriptionResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeletePushSubscriptionResponse()

  @Serializable
  public data class DeletePushSubscriptionResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeletePushSubscriptionResponse()

  @Serializable
  public data class DeletePushSubscriptionResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DeletePushSubscriptionResponse()
}

public fun PushClient(configuration: ClientConfiguration = defaultClientConfiguration): PushClient = DefaultPushClient(configuration)

public class DefaultPushClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : PushClient {
  override suspend fun getPushSubscription(): PushClient.GetPushSubscriptionResponse {
    try {
      val response = configuration.client.`get`("api/v1/push/subscription") {
      }
      return when (response.status.value) {
        200 -> PushClient.GetPushSubscriptionResponseSuccess(response.body<WebPushSubscription>(), response.headers)
        401, 404, 429, 503 -> PushClient.GetPushSubscriptionResponseFailure401(response.body<Error>(), response.headers)
        410 -> PushClient.GetPushSubscriptionResponseFailure410(response.headers)
        422 -> PushClient.GetPushSubscriptionResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PushClient.GetPushSubscriptionResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PushClient.GetPushSubscriptionResponseUnknownFailure(500)
    }
  }

  override suspend fun putPushSubscription(request: PushClient.PutPushSubscriptionRequest): PushClient.PutPushSubscriptionResponse {
    try {
      val response = configuration.client.put("api/v1/push/subscription") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PushClient.PutPushSubscriptionResponseSuccess(response.body<WebPushSubscription>(), response.headers)
        401, 404, 429, 503 -> PushClient.PutPushSubscriptionResponseFailure401(response.body<Error>(), response.headers)
        410 -> PushClient.PutPushSubscriptionResponseFailure410(response.headers)
        422 -> PushClient.PutPushSubscriptionResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PushClient.PutPushSubscriptionResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PushClient.PutPushSubscriptionResponseUnknownFailure(500)
    }
  }

  override suspend fun createPushSubscription(request: PushClient.CreatePushSubscriptionRequest): PushClient.CreatePushSubscriptionResponse {
    try {
      val response = configuration.client.post("api/v1/push/subscription") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PushClient.CreatePushSubscriptionResponseSuccess(response.body<WebPushSubscription>(), response.headers)
        401, 404, 429, 503 -> PushClient.CreatePushSubscriptionResponseFailure401(response.body<Error>(), response.headers)
        410 -> PushClient.CreatePushSubscriptionResponseFailure410(response.headers)
        422 -> PushClient.CreatePushSubscriptionResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PushClient.CreatePushSubscriptionResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PushClient.CreatePushSubscriptionResponseUnknownFailure(500)
    }
  }

  override suspend fun deletePushSubscription(): PushClient.DeletePushSubscriptionResponse {
    try {
      val response = configuration.client.delete("api/v1/push/subscription") {
      }
      return when (response.status.value) {
        200 -> PushClient.DeletePushSubscriptionResponseSuccess(response.headers)
        401, 404, 429, 503 -> PushClient.DeletePushSubscriptionResponseFailure401(response.body<Error>(), response.headers)
        410 -> PushClient.DeletePushSubscriptionResponseFailure410(response.headers)
        422 -> PushClient.DeletePushSubscriptionResponseFailure(response.body<ValidationError>(), response.headers)
        else -> PushClient.DeletePushSubscriptionResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PushClient.DeletePushSubscriptionResponseUnknownFailure(500)
    }
  }
}
