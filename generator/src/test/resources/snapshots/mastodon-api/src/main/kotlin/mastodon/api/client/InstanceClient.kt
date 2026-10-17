package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.Unit
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

public interface InstanceClient {
  /**
   * View server information (v1)
   */
  public suspend fun getInstance(): GetInstanceResponse

  /**
   * Weekly activity
   */
  public suspend fun getInstanceActivity(): GetInstanceActivityResponse

  /**
   * View moderated servers
   */
  public suspend fun getInstanceDomainBlocks(): GetInstanceDomainBlocksResponse

  /**
   * View extended description
   */
  public suspend fun getInstanceExtendedDescription(): GetInstanceExtendedDescriptionResponse

  /**
   * List of connected domains
   */
  public suspend fun getInstancePeers(): GetInstancePeersResponse

  /**
   * View privacy policy
   */
  public suspend fun getInstancePrivacyPolicy(): GetInstancePrivacyPolicyResponse

  /**
   * List of rules
   */
  public suspend fun getInstanceRules(): GetInstanceRulesResponse

  /**
   * View terms of service
   */
  public suspend fun getInstanceTermsOfService(): GetInstanceTermsOfServiceResponse

  /**
   * View a specific version of the terms of service
   */
  public suspend fun getInstanceTermsOfServiceByDate(date: String): GetInstanceTermsOfServiceByDateResponse

  /**
   * View translation languages
   */
  public suspend fun getInstanceTranslationLanguages(): GetInstanceTranslationLanguagesResponse

  /**
   * View server information
   */
  public suspend fun getInstanceV2(): GetInstanceV2Response

  @Serializable
  public sealed class GetInstanceResponse : OperationResponse<V1Instance> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstanceResponseSuccess(
    override val body: V1Instance,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceResponse(),
      OperationResponseSuccess<V1Instance> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstanceResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstanceResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstanceResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetInstanceActivityResponse : OperationResponse<List<JsonElement>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstanceActivityResponseSuccess(
    override val body: List<JsonElement>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceActivityResponse(),
      OperationResponseSuccess<List<JsonElement>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceActivityResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstanceActivityResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceActivityResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstanceActivityResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceActivityResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstanceActivityResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceActivityResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetInstanceDomainBlocksResponse : OperationResponse<List<DomainBlock>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstanceDomainBlocksResponseSuccess(
    override val body: List<DomainBlock>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceDomainBlocksResponse(),
      OperationResponseSuccess<List<DomainBlock>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceDomainBlocksResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstanceDomainBlocksResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceDomainBlocksResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstanceDomainBlocksResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceDomainBlocksResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstanceDomainBlocksResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceDomainBlocksResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetInstanceExtendedDescriptionResponse : OperationResponse<ExtendedDescription> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseSuccess(
    override val body: ExtendedDescription,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceExtendedDescriptionResponse(),
      OperationResponseSuccess<ExtendedDescription> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceExtendedDescriptionResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceExtendedDescriptionResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceExtendedDescriptionResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceExtendedDescriptionResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetInstancePeersResponse : OperationResponse<List<String>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstancePeersResponseSuccess(
    override val body: List<String>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePeersResponse(),
      OperationResponseSuccess<List<String>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePeersResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstancePeersResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePeersResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstancePeersResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePeersResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstancePeersResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePeersResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetInstancePrivacyPolicyResponse : OperationResponse<PrivacyPolicy> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstancePrivacyPolicyResponseSuccess(
    override val body: PrivacyPolicy,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePrivacyPolicyResponse(),
      OperationResponseSuccess<PrivacyPolicy> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePrivacyPolicyResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstancePrivacyPolicyResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePrivacyPolicyResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstancePrivacyPolicyResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePrivacyPolicyResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstancePrivacyPolicyResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstancePrivacyPolicyResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetInstanceRulesResponse : OperationResponse<List<Rule>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstanceRulesResponseSuccess(
    override val body: List<Rule>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceRulesResponse(),
      OperationResponseSuccess<List<Rule>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceRulesResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstanceRulesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceRulesResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstanceRulesResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceRulesResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstanceRulesResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceRulesResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetInstanceTermsOfServiceResponse : OperationResponse<TermsOfService> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstanceTermsOfServiceResponseSuccess(
    override val body: TermsOfService,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceResponse(),
      OperationResponseSuccess<TermsOfService> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstanceTermsOfServiceResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstanceTermsOfServiceResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstanceTermsOfServiceResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetInstanceTermsOfServiceByDateResponse : OperationResponse<TermsOfService> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseSuccess(
    override val body: TermsOfService,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceByDateResponse(),
      OperationResponseSuccess<TermsOfService> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceByDateResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceByDateResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceByDateResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTermsOfServiceByDateResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetInstanceTranslationLanguagesResponse : OperationResponse<Unit> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTranslationLanguagesResponse(),
      OperationResponseSuccess<Unit> {
    override val body: Unit
      get() = Unit

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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTranslationLanguagesResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTranslationLanguagesResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTranslationLanguagesResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceTranslationLanguagesResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetInstanceV2Response : OperationResponse<Instance> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetInstanceV2ResponseSuccess(
    override val body: Instance,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceV2Response(),
      OperationResponseSuccess<Instance> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceV2Response(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetInstanceV2ResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceV2Response(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetInstanceV2ResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceV2Response(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetInstanceV2ResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetInstanceV2Response(),
      OperationResponseUnknownFailure
}

public fun InstanceClient(configuration: ClientConfiguration = defaultClientConfiguration): InstanceClient = DefaultInstanceClient(configuration)

public class DefaultInstanceClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : InstanceClient {
  override suspend fun getInstance(): InstanceClient.GetInstanceResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance") {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstanceResponseSuccess(response.body<V1Instance>(), response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstanceResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstanceResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstanceResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstanceResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstanceResponseUnknownFailure(500)
    }
  }

  override suspend fun getInstanceActivity(): InstanceClient.GetInstanceActivityResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/activity") {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstanceActivityResponseSuccess(response.body<List<JsonElement>>(), response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstanceActivityResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstanceActivityResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstanceActivityResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstanceActivityResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstanceActivityResponseUnknownFailure(500)
    }
  }

  override suspend fun getInstanceDomainBlocks(): InstanceClient.GetInstanceDomainBlocksResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/domain_blocks") {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstanceDomainBlocksResponseSuccess(response.body<List<DomainBlock>>(), response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstanceDomainBlocksResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstanceDomainBlocksResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstanceDomainBlocksResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstanceDomainBlocksResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstanceDomainBlocksResponseUnknownFailure(500)
    }
  }

  override suspend fun getInstanceExtendedDescription(): InstanceClient.GetInstanceExtendedDescriptionResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/extended_description") {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstanceExtendedDescriptionResponseSuccess(response.body<ExtendedDescription>(), response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstanceExtendedDescriptionResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstanceExtendedDescriptionResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstanceExtendedDescriptionResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstanceExtendedDescriptionResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstanceExtendedDescriptionResponseUnknownFailure(500)
    }
  }

  override suspend fun getInstancePeers(): InstanceClient.GetInstancePeersResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/peers") {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstancePeersResponseSuccess(response.body<List<String>>(), response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstancePeersResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstancePeersResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstancePeersResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstancePeersResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstancePeersResponseUnknownFailure(500)
    }
  }

  override suspend fun getInstancePrivacyPolicy(): InstanceClient.GetInstancePrivacyPolicyResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/privacy_policy") {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstancePrivacyPolicyResponseSuccess(response.body<PrivacyPolicy>(), response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstancePrivacyPolicyResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstancePrivacyPolicyResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstancePrivacyPolicyResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstancePrivacyPolicyResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstancePrivacyPolicyResponseUnknownFailure(500)
    }
  }

  override suspend fun getInstanceRules(): InstanceClient.GetInstanceRulesResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/rules") {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstanceRulesResponseSuccess(response.body<List<Rule>>(), response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstanceRulesResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstanceRulesResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstanceRulesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstanceRulesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstanceRulesResponseUnknownFailure(500)
    }
  }

  override suspend fun getInstanceTermsOfService(): InstanceClient.GetInstanceTermsOfServiceResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/terms_of_service") {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstanceTermsOfServiceResponseSuccess(response.body<TermsOfService>(), response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstanceTermsOfServiceResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstanceTermsOfServiceResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstanceTermsOfServiceResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstanceTermsOfServiceResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstanceTermsOfServiceResponseUnknownFailure(500)
    }
  }

  override suspend fun getInstanceTermsOfServiceByDate(date: String): InstanceClient.GetInstanceTermsOfServiceByDateResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/terms_of_service/{date}".replace("/{date}", "/${date.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstanceTermsOfServiceByDateResponseSuccess(response.body<TermsOfService>(), response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstanceTermsOfServiceByDateResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstanceTermsOfServiceByDateResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstanceTermsOfServiceByDateResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstanceTermsOfServiceByDateResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstanceTermsOfServiceByDateResponseUnknownFailure(500)
    }
  }

  override suspend fun getInstanceTranslationLanguages(): InstanceClient.GetInstanceTranslationLanguagesResponse {
    try {
      val response = configuration.client.`get`("api/v1/instance/translation_languages") {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstanceTranslationLanguagesResponseSuccess(response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstanceTranslationLanguagesResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstanceTranslationLanguagesResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstanceTranslationLanguagesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstanceTranslationLanguagesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstanceTranslationLanguagesResponseUnknownFailure(500)
    }
  }

  override suspend fun getInstanceV2(): InstanceClient.GetInstanceV2Response {
    try {
      val response = configuration.client.`get`("api/v2/instance") {
      }
      return when (response.status.value) {
        200 -> InstanceClient.GetInstanceV2ResponseSuccess(response.body<Instance>(), response.headers)
        401, 404, 429, 503 -> InstanceClient.GetInstanceV2ResponseFailure401(response.body<Error>(), response.headers)
        410 -> InstanceClient.GetInstanceV2ResponseFailure410(response.headers)
        422 -> InstanceClient.GetInstanceV2ResponseFailure(response.body<ValidationError>(), response.headers)
        else -> InstanceClient.GetInstanceV2ResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return InstanceClient.GetInstanceV2ResponseUnknownFailure(500)
    }
  }
}
