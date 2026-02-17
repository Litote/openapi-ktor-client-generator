package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable
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
        200 -> GetInstanceResponseSuccess(response.body<V1Instance>())
        401, 404, 429, 503 -> GetInstanceResponseFailure401(response.body<Error>())
        410 -> GetInstanceResponseFailure410
        422 -> GetInstanceResponseFailure(response.body<ValidationError>())
        else -> GetInstanceResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetInstanceActivityResponseSuccess(response.body<List<JsonElement>>())
        401, 404, 429, 503 -> GetInstanceActivityResponseFailure401(response.body<Error>())
        410 -> GetInstanceActivityResponseFailure410
        422 -> GetInstanceActivityResponseFailure(response.body<ValidationError>())
        else -> GetInstanceActivityResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetInstanceDomainBlocksResponseSuccess(response.body<List<DomainBlock>>())
        401, 404, 429, 503 -> GetInstanceDomainBlocksResponseFailure401(response.body<Error>())
        410 -> GetInstanceDomainBlocksResponseFailure410
        422 -> GetInstanceDomainBlocksResponseFailure(response.body<ValidationError>())
        else -> GetInstanceDomainBlocksResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetInstanceExtendedDescriptionResponseSuccess(response.body<ExtendedDescription>())
        401, 404, 429, 503 -> GetInstanceExtendedDescriptionResponseFailure401(response.body<Error>())
        410 -> GetInstanceExtendedDescriptionResponseFailure410
        422 -> GetInstanceExtendedDescriptionResponseFailure(response.body<ValidationError>())
        else -> GetInstanceExtendedDescriptionResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetInstancePeersResponseSuccess(response.body<List<String>>())
        401, 404, 429, 503 -> GetInstancePeersResponseFailure401(response.body<Error>())
        410 -> GetInstancePeersResponseFailure410
        422 -> GetInstancePeersResponseFailure(response.body<ValidationError>())
        else -> GetInstancePeersResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetInstancePrivacyPolicyResponseSuccess(response.body<PrivacyPolicy>())
        401, 404, 429, 503 -> GetInstancePrivacyPolicyResponseFailure401(response.body<Error>())
        410 -> GetInstancePrivacyPolicyResponseFailure410
        422 -> GetInstancePrivacyPolicyResponseFailure(response.body<ValidationError>())
        else -> GetInstancePrivacyPolicyResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetInstanceRulesResponseSuccess(response.body<List<Rule>>())
        401, 404, 429, 503 -> GetInstanceRulesResponseFailure401(response.body<Error>())
        410 -> GetInstanceRulesResponseFailure410
        422 -> GetInstanceRulesResponseFailure(response.body<ValidationError>())
        else -> GetInstanceRulesResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetInstanceTermsOfServiceResponseSuccess(response.body<TermsOfService>())
        401, 404, 429, 503 -> GetInstanceTermsOfServiceResponseFailure401(response.body<Error>())
        410 -> GetInstanceTermsOfServiceResponseFailure410
        422 -> GetInstanceTermsOfServiceResponseFailure(response.body<ValidationError>())
        else -> GetInstanceTermsOfServiceResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetInstanceTermsOfServiceByDateResponseSuccess(response.body<TermsOfService>())
        401, 404, 429, 503 -> GetInstanceTermsOfServiceByDateResponseFailure401(response.body<Error>())
        410 -> GetInstanceTermsOfServiceByDateResponseFailure410
        422 -> GetInstanceTermsOfServiceByDateResponseFailure(response.body<ValidationError>())
        else -> GetInstanceTermsOfServiceByDateResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetInstanceTranslationLanguagesResponseSuccess
        401, 404, 429, 503 -> GetInstanceTranslationLanguagesResponseFailure401(response.body<Error>())
        410 -> GetInstanceTranslationLanguagesResponseFailure410
        422 -> GetInstanceTranslationLanguagesResponseFailure(response.body<ValidationError>())
        else -> GetInstanceTranslationLanguagesResponseUnknownFailure(response.status.value)
      }
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
        200 -> GetInstanceV2ResponseSuccess(response.body<Instance>())
        401, 404, 429, 503 -> GetInstanceV2ResponseFailure401(response.body<Error>())
        410 -> GetInstanceV2ResponseFailure410
        422 -> GetInstanceV2ResponseFailure(response.body<ValidationError>())
        else -> GetInstanceV2ResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetInstanceV2ResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetInstanceResponse

  @Serializable
  public data class GetInstanceResponseSuccess(
    public val body: V1Instance,
  ) : GetInstanceResponse()

  @Serializable
  public data class GetInstanceResponseFailure401(
    public val body: Error,
  ) : GetInstanceResponse()

  @Serializable
  public object GetInstanceResponseFailure410 : GetInstanceResponse()

  @Serializable
  public data class GetInstanceResponseFailure(
    public val body: ValidationError,
  ) : GetInstanceResponse()

  @Serializable
  public data class GetInstanceResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstanceResponse()

  @Serializable
  public sealed class GetInstanceActivityResponse

  @Serializable
  public data class GetInstanceActivityResponseSuccess(
    public val body: List<JsonElement>,
  ) : GetInstanceActivityResponse()

  @Serializable
  public data class GetInstanceActivityResponseFailure401(
    public val body: Error,
  ) : GetInstanceActivityResponse()

  @Serializable
  public object GetInstanceActivityResponseFailure410 : GetInstanceActivityResponse()

  @Serializable
  public data class GetInstanceActivityResponseFailure(
    public val body: ValidationError,
  ) : GetInstanceActivityResponse()

  @Serializable
  public data class GetInstanceActivityResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstanceActivityResponse()

  @Serializable
  public sealed class GetInstanceDomainBlocksResponse

  @Serializable
  public data class GetInstanceDomainBlocksResponseSuccess(
    public val body: List<DomainBlock>,
  ) : GetInstanceDomainBlocksResponse()

  @Serializable
  public data class GetInstanceDomainBlocksResponseFailure401(
    public val body: Error,
  ) : GetInstanceDomainBlocksResponse()

  @Serializable
  public object GetInstanceDomainBlocksResponseFailure410 : GetInstanceDomainBlocksResponse()

  @Serializable
  public data class GetInstanceDomainBlocksResponseFailure(
    public val body: ValidationError,
  ) : GetInstanceDomainBlocksResponse()

  @Serializable
  public data class GetInstanceDomainBlocksResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstanceDomainBlocksResponse()

  @Serializable
  public sealed class GetInstanceExtendedDescriptionResponse

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseSuccess(
    public val body: ExtendedDescription,
  ) : GetInstanceExtendedDescriptionResponse()

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseFailure401(
    public val body: Error,
  ) : GetInstanceExtendedDescriptionResponse()

  @Serializable
  public object GetInstanceExtendedDescriptionResponseFailure410 : GetInstanceExtendedDescriptionResponse()

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseFailure(
    public val body: ValidationError,
  ) : GetInstanceExtendedDescriptionResponse()

  @Serializable
  public data class GetInstanceExtendedDescriptionResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstanceExtendedDescriptionResponse()

  @Serializable
  public sealed class GetInstancePeersResponse

  @Serializable
  public data class GetInstancePeersResponseSuccess(
    public val body: List<String>,
  ) : GetInstancePeersResponse()

  @Serializable
  public data class GetInstancePeersResponseFailure401(
    public val body: Error,
  ) : GetInstancePeersResponse()

  @Serializable
  public object GetInstancePeersResponseFailure410 : GetInstancePeersResponse()

  @Serializable
  public data class GetInstancePeersResponseFailure(
    public val body: ValidationError,
  ) : GetInstancePeersResponse()

  @Serializable
  public data class GetInstancePeersResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstancePeersResponse()

  @Serializable
  public sealed class GetInstancePrivacyPolicyResponse

  @Serializable
  public data class GetInstancePrivacyPolicyResponseSuccess(
    public val body: PrivacyPolicy,
  ) : GetInstancePrivacyPolicyResponse()

  @Serializable
  public data class GetInstancePrivacyPolicyResponseFailure401(
    public val body: Error,
  ) : GetInstancePrivacyPolicyResponse()

  @Serializable
  public object GetInstancePrivacyPolicyResponseFailure410 : GetInstancePrivacyPolicyResponse()

  @Serializable
  public data class GetInstancePrivacyPolicyResponseFailure(
    public val body: ValidationError,
  ) : GetInstancePrivacyPolicyResponse()

  @Serializable
  public data class GetInstancePrivacyPolicyResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstancePrivacyPolicyResponse()

  @Serializable
  public sealed class GetInstanceRulesResponse

  @Serializable
  public data class GetInstanceRulesResponseSuccess(
    public val body: List<Rule>,
  ) : GetInstanceRulesResponse()

  @Serializable
  public data class GetInstanceRulesResponseFailure401(
    public val body: Error,
  ) : GetInstanceRulesResponse()

  @Serializable
  public object GetInstanceRulesResponseFailure410 : GetInstanceRulesResponse()

  @Serializable
  public data class GetInstanceRulesResponseFailure(
    public val body: ValidationError,
  ) : GetInstanceRulesResponse()

  @Serializable
  public data class GetInstanceRulesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstanceRulesResponse()

  @Serializable
  public sealed class GetInstanceTermsOfServiceResponse

  @Serializable
  public data class GetInstanceTermsOfServiceResponseSuccess(
    public val body: TermsOfService,
  ) : GetInstanceTermsOfServiceResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceResponseFailure401(
    public val body: Error,
  ) : GetInstanceTermsOfServiceResponse()

  @Serializable
  public object GetInstanceTermsOfServiceResponseFailure410 : GetInstanceTermsOfServiceResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceResponseFailure(
    public val body: ValidationError,
  ) : GetInstanceTermsOfServiceResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstanceTermsOfServiceResponse()

  @Serializable
  public sealed class GetInstanceTermsOfServiceByDateResponse

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseSuccess(
    public val body: TermsOfService,
  ) : GetInstanceTermsOfServiceByDateResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseFailure401(
    public val body: Error,
  ) : GetInstanceTermsOfServiceByDateResponse()

  @Serializable
  public object GetInstanceTermsOfServiceByDateResponseFailure410 : GetInstanceTermsOfServiceByDateResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseFailure(
    public val body: ValidationError,
  ) : GetInstanceTermsOfServiceByDateResponse()

  @Serializable
  public data class GetInstanceTermsOfServiceByDateResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstanceTermsOfServiceByDateResponse()

  @Serializable
  public sealed class GetInstanceTranslationLanguagesResponse

  @Serializable
  public object GetInstanceTranslationLanguagesResponseSuccess : GetInstanceTranslationLanguagesResponse()

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseFailure401(
    public val body: Error,
  ) : GetInstanceTranslationLanguagesResponse()

  @Serializable
  public object GetInstanceTranslationLanguagesResponseFailure410 : GetInstanceTranslationLanguagesResponse()

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseFailure(
    public val body: ValidationError,
  ) : GetInstanceTranslationLanguagesResponse()

  @Serializable
  public data class GetInstanceTranslationLanguagesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstanceTranslationLanguagesResponse()

  @Serializable
  public sealed class GetInstanceV2Response

  @Serializable
  public data class GetInstanceV2ResponseSuccess(
    public val body: Instance,
  ) : GetInstanceV2Response()

  @Serializable
  public data class GetInstanceV2ResponseFailure401(
    public val body: Error,
  ) : GetInstanceV2Response()

  @Serializable
  public object GetInstanceV2ResponseFailure410 : GetInstanceV2Response()

  @Serializable
  public data class GetInstanceV2ResponseFailure(
    public val body: ValidationError,
  ) : GetInstanceV2Response()

  @Serializable
  public data class GetInstanceV2ResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetInstanceV2Response()
}
