package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.DomainBlock
import mastodon.api.model.Error
import mastodon.api.model.ExtendedDescription
import mastodon.api.model.Instance
import mastodon.api.model.PrivacyPolicy
import mastodon.api.model.Rule
import mastodon.api.model.TermsOfService
import mastodon.api.model.V1Instance
import mastodon.api.model.ValidationError

public class InstanceClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * View server information (v1)
   */
  public suspend fun getInstance(): GetInstanceResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance") {
      }
      return when (response.status.value) {
        200 -> GetInstanceResponseSuccess(response.body<V1Instance>(), response.headers)
        401, 404, 429, 503 -> GetInstanceResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstanceResponseFailure410(response.headers)
        422 -> GetInstanceResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstanceResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstanceResponseUnknownFailure(500)
    }
  }

  /**
   * Weekly activity
   */
  public suspend fun getInstanceActivity(): GetInstanceActivityResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/activity") {
      }
      return when (response.status.value) {
        200 -> GetInstanceActivityResponseSuccess(response.body<List<JsonElement>>(), response.headers)
        401, 404, 429, 503 -> GetInstanceActivityResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstanceActivityResponseFailure410(response.headers)
        422 -> GetInstanceActivityResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstanceActivityResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstanceActivityResponseUnknownFailure(500)
    }
  }

  /**
   * View moderated servers
   */
  public suspend fun getInstanceDomainBlocks(): GetInstanceDomainBlocksResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/domain_blocks") {
      }
      return when (response.status.value) {
        200 -> GetInstanceDomainBlocksResponseSuccess(response.body<List<DomainBlock>>(), response.headers)
        401, 404, 429, 503 -> GetInstanceDomainBlocksResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstanceDomainBlocksResponseFailure410(response.headers)
        422 -> GetInstanceDomainBlocksResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstanceDomainBlocksResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstanceDomainBlocksResponseUnknownFailure(500)
    }
  }

  /**
   * View extended description
   */
  public suspend fun getInstanceExtendedDescription(): GetInstanceExtendedDescriptionResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/extended_description") {
      }
      return when (response.status.value) {
        200 -> GetInstanceExtendedDescriptionResponseSuccess(response.body<ExtendedDescription>(), response.headers)
        401, 404, 429, 503 -> GetInstanceExtendedDescriptionResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstanceExtendedDescriptionResponseFailure410(response.headers)
        422 -> GetInstanceExtendedDescriptionResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstanceExtendedDescriptionResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstanceExtendedDescriptionResponseUnknownFailure(500)
    }
  }

  /**
   * List of connected domains
   */
  public suspend fun getInstancePeers(): GetInstancePeersResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/peers") {
      }
      return when (response.status.value) {
        200 -> GetInstancePeersResponseSuccess(response.body<List<String>>(), response.headers)
        401, 404, 429, 503 -> GetInstancePeersResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstancePeersResponseFailure410(response.headers)
        422 -> GetInstancePeersResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstancePeersResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstancePeersResponseUnknownFailure(500)
    }
  }

  /**
   * View privacy policy
   */
  public suspend fun getInstancePrivacyPolicy(): GetInstancePrivacyPolicyResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/privacy_policy") {
      }
      return when (response.status.value) {
        200 -> GetInstancePrivacyPolicyResponseSuccess(response.body<PrivacyPolicy>(), response.headers)
        401, 404, 429, 503 -> GetInstancePrivacyPolicyResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstancePrivacyPolicyResponseFailure410(response.headers)
        422 -> GetInstancePrivacyPolicyResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstancePrivacyPolicyResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstancePrivacyPolicyResponseUnknownFailure(500)
    }
  }

  /**
   * List of rules
   */
  public suspend fun getInstanceRules(): GetInstanceRulesResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/rules") {
      }
      return when (response.status.value) {
        200 -> GetInstanceRulesResponseSuccess(response.body<List<Rule>>(), response.headers)
        401, 404, 429, 503 -> GetInstanceRulesResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstanceRulesResponseFailure410(response.headers)
        422 -> GetInstanceRulesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstanceRulesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstanceRulesResponseUnknownFailure(500)
    }
  }

  /**
   * View terms of service
   */
  public suspend fun getInstanceTermsOfService(): GetInstanceTermsOfServiceResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/terms_of_service") {
      }
      return when (response.status.value) {
        200 -> GetInstanceTermsOfServiceResponseSuccess(response.body<TermsOfService>(), response.headers)
        401, 404, 429, 503 -> GetInstanceTermsOfServiceResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstanceTermsOfServiceResponseFailure410(response.headers)
        422 -> GetInstanceTermsOfServiceResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstanceTermsOfServiceResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstanceTermsOfServiceResponseUnknownFailure(500)
    }
  }

  /**
   * View a specific version of the terms of service
   */
  public suspend fun getInstanceTermsOfServiceByDate(date: String): GetInstanceTermsOfServiceByDateResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/terms_of_service/{date}".replace("/{date}", "/${date.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetInstanceTermsOfServiceByDateResponseSuccess(response.body<TermsOfService>(), response.headers)
        401, 404, 429, 503 -> GetInstanceTermsOfServiceByDateResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstanceTermsOfServiceByDateResponseFailure410(response.headers)
        422 -> GetInstanceTermsOfServiceByDateResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstanceTermsOfServiceByDateResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstanceTermsOfServiceByDateResponseUnknownFailure(500)
    }
  }

  /**
   * View translation languages
   */
  public suspend fun getInstanceTranslationLanguages(): GetInstanceTranslationLanguagesResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/translation_languages") {
      }
      return when (response.status.value) {
        200 -> GetInstanceTranslationLanguagesResponseSuccess(response.headers)
        401, 404, 429, 503 -> GetInstanceTranslationLanguagesResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstanceTranslationLanguagesResponseFailure410(response.headers)
        422 -> GetInstanceTranslationLanguagesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstanceTranslationLanguagesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstanceTranslationLanguagesResponseUnknownFailure(500)
    }
  }

  /**
   * View server information
   */
  public suspend fun getInstanceV2(): GetInstanceV2Response {
    try {
      val response = configuration.client.`get`("api/v2/instance") {
      }
      return when (response.status.value) {
        200 -> GetInstanceV2ResponseSuccess(response.body<Instance>(), response.headers)
        401, 404, 429, 503 -> GetInstanceV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetInstanceV2ResponseFailure410(response.headers)
        422 -> GetInstanceV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetInstanceV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstanceV2ResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetInstanceResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstanceResponseSuccess(
    public val body: V1Instance,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceResponse() {
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
  public data class GetInstanceResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceResponse()

  @Serializable
  public data class GetInstanceResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceResponse()

  @Serializable
  public data class GetInstanceResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceResponse()

  @Serializable
  public data class GetInstanceResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceResponse()

  @Serializable
  public sealed class GetInstanceActivityResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstanceActivityResponseSuccess(
    public val body: List<JsonElement>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceActivityResponse() {
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
  public data class GetInstanceActivityResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceActivityResponse()

  @Serializable
  public data class GetInstanceActivityResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceActivityResponse()

  @Serializable
  public data class GetInstanceActivityResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceActivityResponse()

  @Serializable
  public data class GetInstanceActivityResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceActivityResponse()

  @Serializable
  public sealed class GetInstanceDomainBlocksResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstanceDomainBlocksResponseSuccess(
    public val body: List<DomainBlock>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceDomainBlocksResponse() {
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
  public data class GetInstanceDomainBlocksResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceDomainBlocksResponse()

  @Serializable
  public data class GetInstanceDomainBlocksResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceDomainBlocksResponse()

  @Serializable
  public data class GetInstanceDomainBlocksResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceDomainBlocksResponse()

  @Serializable
  public data class GetInstanceDomainBlocksResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceDomainBlocksResponse()

  @Serializable
  public sealed class GetInstanceExtendedDescriptionResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseSuccess(
    public val body: ExtendedDescription,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceExtendedDescriptionResponse() {
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
  public data class GetInstanceExtendedDescriptionResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceExtendedDescriptionResponse()

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceExtendedDescriptionResponse()

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceExtendedDescriptionResponse()

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceExtendedDescriptionResponse()

  @Serializable
  public sealed class GetInstancePeersResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstancePeersResponseSuccess(
    public val body: List<String>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePeersResponse() {
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
  public data class GetInstancePeersResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePeersResponse()

  @Serializable
  public data class GetInstancePeersResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePeersResponse()

  @Serializable
  public data class GetInstancePeersResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePeersResponse()

  @Serializable
  public data class GetInstancePeersResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePeersResponse()

  @Serializable
  public sealed class GetInstancePrivacyPolicyResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstancePrivacyPolicyResponseSuccess(
    public val body: PrivacyPolicy,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePrivacyPolicyResponse() {
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
  public data class GetInstancePrivacyPolicyResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePrivacyPolicyResponse()

  @Serializable
  public data class GetInstancePrivacyPolicyResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePrivacyPolicyResponse()

  @Serializable
  public data class GetInstancePrivacyPolicyResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePrivacyPolicyResponse()

  @Serializable
  public data class GetInstancePrivacyPolicyResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePrivacyPolicyResponse()

  @Serializable
  public sealed class GetInstanceRulesResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstanceRulesResponseSuccess(
    public val body: List<Rule>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceRulesResponse() {
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
  public data class GetInstanceRulesResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceRulesResponse()

  @Serializable
  public data class GetInstanceRulesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceRulesResponse()

  @Serializable
  public data class GetInstanceRulesResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceRulesResponse()

  @Serializable
  public data class GetInstanceRulesResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceRulesResponse()

  @Serializable
  public sealed class GetInstanceTermsOfServiceResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstanceTermsOfServiceResponseSuccess(
    public val body: TermsOfService,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceResponse() {
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
  public data class GetInstanceTermsOfServiceResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceResponse()

  @Serializable
  public sealed class GetInstanceTermsOfServiceByDateResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseSuccess(
    public val body: TermsOfService,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceByDateResponse() {
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
  public data class GetInstanceTermsOfServiceByDateResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceByDateResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceByDateResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceByDateResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceByDateResponse()

  @Serializable
  public sealed class GetInstanceTranslationLanguagesResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTranslationLanguagesResponse() {
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
  public data class GetInstanceTranslationLanguagesResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTranslationLanguagesResponse()

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTranslationLanguagesResponse()

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTranslationLanguagesResponse()

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTranslationLanguagesResponse()

  @Serializable
  public sealed class GetInstanceV2Response {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetInstanceV2ResponseSuccess(
    public val body: Instance,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceV2Response() {
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
  public data class GetInstanceV2ResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceV2Response()

  @Serializable
  public data class GetInstanceV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceV2Response()

  @Serializable
  public data class GetInstanceV2ResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceV2Response()

  @Serializable
  public data class GetInstanceV2ResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceV2Response()
}
