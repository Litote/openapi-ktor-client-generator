package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Account
import mastodon.api.model.CredentialAccount
import mastodon.api.model.Error
import mastodon.api.model.FamiliarFollowers
import mastodon.api.model.FeaturedTag
import mastodon.api.model.IdentityProof
import mastodon.api.model.Relationship
import mastodon.api.model.Status
import mastodon.api.model.Token
import mastodon.api.model.ValidationError
import kotlin.collections.List as CollectionsList
import mastodon.api.model.List as ModelList

public class AccountsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get multiple accounts
   */
  public suspend fun getAccounts(id: CollectionsList<GetAccountsId>? = null): GetAccountsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts") {
        url {
          if (id != null) {
            parameters.append("id", id.joinToString(","))
          }
        }
      }
      return when (response.status.value) {
        200 -> GetAccountsResponseSuccess(response.body<CollectionsList<Account>>())
        401, 404, 429, 503 -> GetAccountsResponseFailure401(response.body<Error>())
        410 -> GetAccountsResponseFailure410
        422 -> GetAccountsResponseFailure(response.body<ValidationError>())
        else -> GetAccountsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountsResponseUnknownFailure(500)
    }
  }

  /**
   * Register an account
   */
  public suspend fun createAccount(request: JsonElement): CreateAccountResponse {
    try {
      val response = configuration.client.post("api/v1/accounts") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateAccountResponseSuccess(response.body<Token>())
        401, 404, 429, 503 -> CreateAccountResponseFailure401(response.body<Error>())
        410 -> CreateAccountResponseFailure410
        422 -> CreateAccountResponseFailure(response.body<ValidationError>())
        else -> CreateAccountResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateAccountResponseUnknownFailure(500)
    }
  }

  /**
   * Get account
   */
  public suspend fun getAccount(id: String): GetAccountResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetAccountResponseSuccess(response.body<Account>())
        401, 404, 429, 503 -> GetAccountResponseFailure401(response.body<Error>())
        410 -> GetAccountResponseFailure410
        422 -> GetAccountResponseFailure(response.body<ValidationError>())
        else -> GetAccountResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountResponseUnknownFailure(500)
    }
  }

  /**
   * Block account
   */
  public suspend fun postAccountBlock(id: String): PostAccountBlockResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/block".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostAccountBlockResponseSuccess(response.body<Relationship>())
        401, 404, 422, 429, 503 -> PostAccountBlockResponseFailure401(response.body<Error>())
        410 -> PostAccountBlockResponseFailure
        else -> PostAccountBlockResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountBlockResponseUnknownFailure(500)
    }
  }

  /**
   * Feature account on your profile
   */
  public suspend fun postAccountEndorse(id: String): PostAccountEndorseResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/endorse".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostAccountEndorseResponseSuccess(response.body<Relationship>())
        401, 403, 404, 422, 429, 500, 503 -> PostAccountEndorseResponseFailure401(response.body<Error>())
        410 -> PostAccountEndorseResponseFailure
        else -> PostAccountEndorseResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountEndorseResponseUnknownFailure(500)
    }
  }

  /**
   * Get featured accounts
   */
  public suspend fun getAccountEndorsements(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetAccountEndorsementsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}/endorsements".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (sinceId != null) {
            parameters.append("since_id", sinceId)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetAccountEndorsementsResponseSuccess(response.body<CollectionsList<Account>>())
        401, 404, 429, 503 -> GetAccountEndorsementsResponseFailure401(response.body<Error>())
        410 -> GetAccountEndorsementsResponseFailure410
        422 -> GetAccountEndorsementsResponseFailure(response.body<ValidationError>())
        else -> GetAccountEndorsementsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountEndorsementsResponseUnknownFailure(500)
    }
  }

  /**
   * Get account's featured tags
   */
  public suspend fun getAccountFeaturedTags(id: String): GetAccountFeaturedTagsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}/featured_tags".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetAccountFeaturedTagsResponseSuccess(response.body<CollectionsList<FeaturedTag>>())
        401, 404, 429, 503 -> GetAccountFeaturedTagsResponseFailure401(response.body<Error>())
        410 -> GetAccountFeaturedTagsResponseFailure410
        422 -> GetAccountFeaturedTagsResponseFailure(response.body<ValidationError>())
        else -> GetAccountFeaturedTagsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountFeaturedTagsResponseUnknownFailure(500)
    }
  }

  /**
   * Follow account
   */
  public suspend fun postAccountFollow(request: JsonElement, id: String): PostAccountFollowResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/follow".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostAccountFollowResponseSuccess(response.body<Relationship>())
        401, 403, 404, 422, 429, 503 -> PostAccountFollowResponseFailure401(response.body<Error>())
        410 -> PostAccountFollowResponseFailure
        else -> PostAccountFollowResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountFollowResponseUnknownFailure(500)
    }
  }

  /**
   * Get account's followers
   */
  public suspend fun getAccountFollowers(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetAccountFollowersResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}/followers".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (minId != null) {
            parameters.append("min_id", minId)
          }
          if (sinceId != null) {
            parameters.append("since_id", sinceId)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetAccountFollowersResponseSuccess(response.body<CollectionsList<Account>>())
        401, 404, 429, 503 -> GetAccountFollowersResponseFailure401(response.body<Error>())
        410 -> GetAccountFollowersResponseFailure410
        422 -> GetAccountFollowersResponseFailure(response.body<ValidationError>())
        else -> GetAccountFollowersResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountFollowersResponseUnknownFailure(500)
    }
  }

  /**
   * Get account's following
   */
  public suspend fun getAccountFollowing(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetAccountFollowingResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}/following".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        url {
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (minId != null) {
            parameters.append("min_id", minId)
          }
          if (sinceId != null) {
            parameters.append("since_id", sinceId)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetAccountFollowingResponseSuccess(response.body<CollectionsList<Account>>())
        401, 404, 429, 503 -> GetAccountFollowingResponseFailure401(response.body<Error>())
        410 -> GetAccountFollowingResponseFailure410
        422 -> GetAccountFollowingResponseFailure(response.body<ValidationError>())
        else -> GetAccountFollowingResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountFollowingResponseUnknownFailure(500)
    }
  }

  /**
   * Identity proofs
   */
  public suspend fun getAccountIdentityProofs(id: String): GetAccountIdentityProofsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}/identity_proofs".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetAccountIdentityProofsResponseSuccess(response.body<CollectionsList<IdentityProof>>())
        401, 404, 422, 429, 503 -> GetAccountIdentityProofsResponseFailure401(response.body<Error>())
        410 -> GetAccountIdentityProofsResponseFailure
        else -> GetAccountIdentityProofsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountIdentityProofsResponseUnknownFailure(500)
    }
  }

  /**
   * Get lists containing this account
   */
  public suspend fun getAccountLists(id: String): GetAccountListsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}/lists".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetAccountListsResponseSuccess(response.body<CollectionsList<ModelList>>())
        401, 404, 422, 429, 503 -> GetAccountListsResponseFailure401(response.body<Error>())
        410 -> GetAccountListsResponseFailure
        else -> GetAccountListsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountListsResponseUnknownFailure(500)
    }
  }

  /**
   * Mute account
   */
  public suspend fun postAccountMute(request: JsonElement, id: String): PostAccountMuteResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/mute".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostAccountMuteResponseSuccess(response.body<Relationship>())
        401, 404, 422, 429, 503 -> PostAccountMuteResponseFailure401(response.body<Error>())
        410 -> PostAccountMuteResponseFailure
        else -> PostAccountMuteResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountMuteResponseUnknownFailure(500)
    }
  }

  /**
   * Set private note on profile
   */
  public suspend fun postAccountNote(request: JsonElement, id: String): PostAccountNoteResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/note".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostAccountNoteResponseSuccess(response.body<Relationship>())
        401, 404, 422, 429, 503 -> PostAccountNoteResponseFailure401(response.body<Error>())
        410 -> PostAccountNoteResponseFailure
        else -> PostAccountNoteResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountNoteResponseUnknownFailure(500)
    }
  }

  /**
   * Feature account on your profile
   */
  public suspend fun postAccountPin(id: String): PostAccountPinResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/pin".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostAccountPinResponseSuccess(response.body<Relationship>())
        401, 403, 404, 422, 429, 500, 503 -> PostAccountPinResponseFailure401(response.body<Error>())
        410 -> PostAccountPinResponseFailure
        else -> PostAccountPinResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountPinResponseUnknownFailure(500)
    }
  }

  /**
   * Remove account from followers
   */
  public suspend fun postAccountRemoveFromFollowers(id: String): PostAccountRemoveFromFollowersResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/remove_from_followers".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostAccountRemoveFromFollowersResponseSuccess(response.body<Relationship>())
        401, 404, 422, 429, 503 -> PostAccountRemoveFromFollowersResponseFailure401(response.body<Error>())
        410 -> PostAccountRemoveFromFollowersResponseFailure
        else -> PostAccountRemoveFromFollowersResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountRemoveFromFollowersResponseUnknownFailure(500)
    }
  }

  /**
   * Get account's statuses
   */
  public suspend fun getAccountStatuses(
    id: String,
    excludeReblogs: Boolean? = null,
    excludeReplies: Boolean? = null,
    limit: Long? = 20,
    maxId: String? = null,
    minId: String? = null,
    onlyMedia: Boolean? = null,
    pinned: Boolean? = null,
    sinceId: String? = null,
    tagged: String? = null,
  ): GetAccountStatusesResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}/statuses".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        url {
          if (excludeReblogs != null) {
            parameters.append("exclude_reblogs", excludeReblogs.toString())
          }
          if (excludeReplies != null) {
            parameters.append("exclude_replies", excludeReplies.toString())
          }
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (maxId != null) {
            parameters.append("max_id", maxId)
          }
          if (minId != null) {
            parameters.append("min_id", minId)
          }
          if (onlyMedia != null) {
            parameters.append("only_media", onlyMedia.toString())
          }
          if (pinned != null) {
            parameters.append("pinned", pinned.toString())
          }
          if (sinceId != null) {
            parameters.append("since_id", sinceId)
          }
          if (tagged != null) {
            parameters.append("tagged", tagged)
          }
        }
      }
      return when (response.status.value) {
        200 -> GetAccountStatusesResponseSuccess(response.body<CollectionsList<Status>>())
        401, 404, 429, 503 -> GetAccountStatusesResponseFailure401(response.body<Error>())
        410 -> GetAccountStatusesResponseFailure410
        422 -> GetAccountStatusesResponseFailure(response.body<ValidationError>())
        else -> GetAccountStatusesResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountStatusesResponseUnknownFailure(500)
    }
  }

  /**
   * Unblock account
   */
  public suspend fun postAccountUnblock(id: String): PostAccountUnblockResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/unblock".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostAccountUnblockResponseSuccess(response.body<Relationship>())
        401, 404, 422, 429, 503 -> PostAccountUnblockResponseFailure401(response.body<Error>())
        410 -> PostAccountUnblockResponseFailure
        else -> PostAccountUnblockResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountUnblockResponseUnknownFailure(500)
    }
  }

  /**
   * Unfeature account from profile
   */
  public suspend fun postAccountUnendorse(id: String): PostAccountUnendorseResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/unendorse".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostAccountUnendorseResponseSuccess(response.body<Relationship>())
        401, 404, 422, 429, 503 -> PostAccountUnendorseResponseFailure401(response.body<Error>())
        410 -> PostAccountUnendorseResponseFailure
        else -> PostAccountUnendorseResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountUnendorseResponseUnknownFailure(500)
    }
  }

  /**
   * Unfollow account
   */
  public suspend fun postAccountUnfollow(id: String): PostAccountUnfollowResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/unfollow".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostAccountUnfollowResponseSuccess(response.body<Relationship>())
        401, 404, 422, 429, 503 -> PostAccountUnfollowResponseFailure401(response.body<Error>())
        410 -> PostAccountUnfollowResponseFailure
        else -> PostAccountUnfollowResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountUnfollowResponseUnknownFailure(500)
    }
  }

  /**
   * Unmute account
   */
  public suspend fun postAccountUnmute(id: String): PostAccountUnmuteResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/unmute".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostAccountUnmuteResponseSuccess(response.body<Relationship>())
        401, 404, 422, 429, 503 -> PostAccountUnmuteResponseFailure401(response.body<Error>())
        410 -> PostAccountUnmuteResponseFailure
        else -> PostAccountUnmuteResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountUnmuteResponseUnknownFailure(500)
    }
  }

  /**
   * Unfeature account from profile
   */
  public suspend fun postAccountUnpin(id: String): PostAccountUnpinResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/unpin".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> PostAccountUnpinResponseSuccess(response.body<Relationship>())
        401, 404, 422, 429, 503 -> PostAccountUnpinResponseFailure401(response.body<Error>())
        410 -> PostAccountUnpinResponseFailure
        else -> PostAccountUnpinResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountUnpinResponseUnknownFailure(500)
    }
  }

  /**
   * Find familiar followers
   */
  public suspend fun getAccountsFamiliarFollowers(id: CollectionsList<GetAccountsFamiliarFollowersId>? = null): GetAccountsFamiliarFollowersResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/familiar_followers") {
        url {
          if (id != null) {
            parameters.append("id", id.joinToString(","))
          }
        }
      }
      return when (response.status.value) {
        200 -> GetAccountsFamiliarFollowersResponseSuccess(response.body<CollectionsList<FamiliarFollowers>>())
        401, 404, 422, 429, 503 -> GetAccountsFamiliarFollowersResponseFailure401(response.body<Error>())
        410 -> GetAccountsFamiliarFollowersResponseFailure
        else -> GetAccountsFamiliarFollowersResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountsFamiliarFollowersResponseUnknownFailure(500)
    }
  }

  /**
   * Lookup account ID from WebFinger address
   */
  public suspend fun getAccountLookup(acct: String): GetAccountLookupResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/lookup") {
        url {
          parameters.append("acct", acct)
        }
      }
      return when (response.status.value) {
        200 -> GetAccountLookupResponseSuccess(response.body<Account>())
        401, 404, 429, 503 -> GetAccountLookupResponseFailure401(response.body<Error>())
        410 -> GetAccountLookupResponseFailure410
        422 -> GetAccountLookupResponseFailure(response.body<ValidationError>())
        else -> GetAccountLookupResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountLookupResponseUnknownFailure(500)
    }
  }

  /**
   * Check relationships to other accounts
   */
  public suspend fun getAccountRelationships(id: CollectionsList<GetAccountRelationshipsId>? = null, withSuspended: Boolean? = false): GetAccountRelationshipsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/relationships") {
        url {
          if (id != null) {
            parameters.append("id", id.joinToString(","))
          }
          if (withSuspended != null) {
            parameters.append("with_suspended", withSuspended.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetAccountRelationshipsResponseSuccess(response.body<CollectionsList<Relationship>>())
        401, 404, 422, 429, 503 -> GetAccountRelationshipsResponseFailure401(response.body<Error>())
        410 -> GetAccountRelationshipsResponseFailure
        else -> GetAccountRelationshipsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountRelationshipsResponseUnknownFailure(500)
    }
  }

  /**
   * Search for matching accounts
   */
  public suspend fun getAccountSearch(
    q: String,
    following: Boolean? = false,
    limit: Long? = 40,
    offset: Long? = null,
    resolve: Boolean? = false,
  ): GetAccountSearchResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/search") {
        url {
          parameters.append("q", q)
          if (following != null) {
            parameters.append("following", following.toString())
          }
          if (limit != null) {
            parameters.append("limit", limit.toString())
          }
          if (offset != null) {
            parameters.append("offset", offset.toString())
          }
          if (resolve != null) {
            parameters.append("resolve", resolve.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetAccountSearchResponseSuccess(response.body<CollectionsList<Account>>())
        401, 404, 429, 503 -> GetAccountSearchResponseFailure401(response.body<Error>())
        410 -> GetAccountSearchResponseFailure410
        422 -> GetAccountSearchResponseFailure(response.body<ValidationError>())
        else -> GetAccountSearchResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountSearchResponseUnknownFailure(500)
    }
  }

  /**
   * Update account credentials
   */
  public suspend fun patchAccountsUpdateCredentials(request: JsonElement): PatchAccountsUpdateCredentialsResponse {
    try {
      val response = configuration.client.patch("api/v1/accounts/update_credentials") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PatchAccountsUpdateCredentialsResponseSuccess(response.body<CredentialAccount>())
        401, 404, 422, 429, 503 -> PatchAccountsUpdateCredentialsResponseFailure401(response.body<Error>())
        410 -> PatchAccountsUpdateCredentialsResponseFailure
        else -> PatchAccountsUpdateCredentialsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PatchAccountsUpdateCredentialsResponseUnknownFailure(500)
    }
  }

  /**
   * Verify account credentials
   */
  public suspend fun getAccountsVerifyCredentials(): GetAccountsVerifyCredentialsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/verify_credentials") {
      }
      return when (response.status.value) {
        200 -> GetAccountsVerifyCredentialsResponseSuccess(response.body<CredentialAccount>())
        401, 403, 404, 422, 429, 503 -> GetAccountsVerifyCredentialsResponseFailure401(response.body<Error>())
        410 -> GetAccountsVerifyCredentialsResponseFailure
        else -> GetAccountsVerifyCredentialsResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountsVerifyCredentialsResponseUnknownFailure(500)
    }
  }

  @Serializable
  public object GetAccountsId

  @Serializable
  public object GetAccountsFamiliarFollowersId

  @Serializable
  public object GetAccountRelationshipsId

  @Serializable
  public sealed class GetAccountsResponse

  @Serializable
  public data class GetAccountsResponseSuccess(
    public val body: CollectionsList<Account>,
  ) : GetAccountsResponse()

  @Serializable
  public data class GetAccountsResponseFailure401(
    public val body: Error,
  ) : GetAccountsResponse()

  @Serializable
  public object GetAccountsResponseFailure410 : GetAccountsResponse()

  @Serializable
  public data class GetAccountsResponseFailure(
    public val body: ValidationError,
  ) : GetAccountsResponse()

  @Serializable
  public data class GetAccountsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountsResponse()

  @Serializable
  public sealed class CreateAccountResponse

  @Serializable
  public data class CreateAccountResponseSuccess(
    public val body: Token,
  ) : CreateAccountResponse()

  @Serializable
  public data class CreateAccountResponseFailure401(
    public val body: Error,
  ) : CreateAccountResponse()

  @Serializable
  public object CreateAccountResponseFailure410 : CreateAccountResponse()

  @Serializable
  public data class CreateAccountResponseFailure(
    public val body: ValidationError,
  ) : CreateAccountResponse()

  @Serializable
  public data class CreateAccountResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateAccountResponse()

  @Serializable
  public sealed class GetAccountResponse

  @Serializable
  public data class GetAccountResponseSuccess(
    public val body: Account,
  ) : GetAccountResponse()

  @Serializable
  public data class GetAccountResponseFailure401(
    public val body: Error,
  ) : GetAccountResponse()

  @Serializable
  public object GetAccountResponseFailure410 : GetAccountResponse()

  @Serializable
  public data class GetAccountResponseFailure(
    public val body: ValidationError,
  ) : GetAccountResponse()

  @Serializable
  public data class GetAccountResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountResponse()

  @Serializable
  public sealed class PostAccountBlockResponse

  @Serializable
  public data class PostAccountBlockResponseSuccess(
    public val body: Relationship,
  ) : PostAccountBlockResponse()

  @Serializable
  public data class PostAccountBlockResponseFailure401(
    public val body: Error,
  ) : PostAccountBlockResponse()

  @Serializable
  public object PostAccountBlockResponseFailure : PostAccountBlockResponse()

  @Serializable
  public data class PostAccountBlockResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountBlockResponse()

  @Serializable
  public sealed class PostAccountEndorseResponse

  @Serializable
  public data class PostAccountEndorseResponseSuccess(
    public val body: Relationship,
  ) : PostAccountEndorseResponse()

  @Serializable
  public data class PostAccountEndorseResponseFailure401(
    public val body: Error,
  ) : PostAccountEndorseResponse()

  @Serializable
  public object PostAccountEndorseResponseFailure : PostAccountEndorseResponse()

  @Serializable
  public data class PostAccountEndorseResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountEndorseResponse()

  @Serializable
  public sealed class GetAccountEndorsementsResponse

  @Serializable
  public data class GetAccountEndorsementsResponseSuccess(
    public val body: CollectionsList<Account>,
  ) : GetAccountEndorsementsResponse()

  @Serializable
  public data class GetAccountEndorsementsResponseFailure401(
    public val body: Error,
  ) : GetAccountEndorsementsResponse()

  @Serializable
  public object GetAccountEndorsementsResponseFailure410 : GetAccountEndorsementsResponse()

  @Serializable
  public data class GetAccountEndorsementsResponseFailure(
    public val body: ValidationError,
  ) : GetAccountEndorsementsResponse()

  @Serializable
  public data class GetAccountEndorsementsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountEndorsementsResponse()

  @Serializable
  public sealed class GetAccountFeaturedTagsResponse

  @Serializable
  public data class GetAccountFeaturedTagsResponseSuccess(
    public val body: CollectionsList<FeaturedTag>,
  ) : GetAccountFeaturedTagsResponse()

  @Serializable
  public data class GetAccountFeaturedTagsResponseFailure401(
    public val body: Error,
  ) : GetAccountFeaturedTagsResponse()

  @Serializable
  public object GetAccountFeaturedTagsResponseFailure410 : GetAccountFeaturedTagsResponse()

  @Serializable
  public data class GetAccountFeaturedTagsResponseFailure(
    public val body: ValidationError,
  ) : GetAccountFeaturedTagsResponse()

  @Serializable
  public data class GetAccountFeaturedTagsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountFeaturedTagsResponse()

  @Serializable
  public sealed class PostAccountFollowResponse

  @Serializable
  public data class PostAccountFollowResponseSuccess(
    public val body: Relationship,
  ) : PostAccountFollowResponse()

  @Serializable
  public data class PostAccountFollowResponseFailure401(
    public val body: Error,
  ) : PostAccountFollowResponse()

  @Serializable
  public object PostAccountFollowResponseFailure : PostAccountFollowResponse()

  @Serializable
  public data class PostAccountFollowResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountFollowResponse()

  @Serializable
  public sealed class GetAccountFollowersResponse

  @Serializable
  public data class GetAccountFollowersResponseSuccess(
    public val body: CollectionsList<Account>,
  ) : GetAccountFollowersResponse()

  @Serializable
  public data class GetAccountFollowersResponseFailure401(
    public val body: Error,
  ) : GetAccountFollowersResponse()

  @Serializable
  public object GetAccountFollowersResponseFailure410 : GetAccountFollowersResponse()

  @Serializable
  public data class GetAccountFollowersResponseFailure(
    public val body: ValidationError,
  ) : GetAccountFollowersResponse()

  @Serializable
  public data class GetAccountFollowersResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountFollowersResponse()

  @Serializable
  public sealed class GetAccountFollowingResponse

  @Serializable
  public data class GetAccountFollowingResponseSuccess(
    public val body: CollectionsList<Account>,
  ) : GetAccountFollowingResponse()

  @Serializable
  public data class GetAccountFollowingResponseFailure401(
    public val body: Error,
  ) : GetAccountFollowingResponse()

  @Serializable
  public object GetAccountFollowingResponseFailure410 : GetAccountFollowingResponse()

  @Serializable
  public data class GetAccountFollowingResponseFailure(
    public val body: ValidationError,
  ) : GetAccountFollowingResponse()

  @Serializable
  public data class GetAccountFollowingResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountFollowingResponse()

  @Serializable
  public sealed class GetAccountIdentityProofsResponse

  @Serializable
  public data class GetAccountIdentityProofsResponseSuccess(
    public val body: CollectionsList<IdentityProof>,
  ) : GetAccountIdentityProofsResponse()

  @Serializable
  public data class GetAccountIdentityProofsResponseFailure401(
    public val body: Error,
  ) : GetAccountIdentityProofsResponse()

  @Serializable
  public object GetAccountIdentityProofsResponseFailure : GetAccountIdentityProofsResponse()

  @Serializable
  public data class GetAccountIdentityProofsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountIdentityProofsResponse()

  @Serializable
  public sealed class GetAccountListsResponse

  @Serializable
  public data class GetAccountListsResponseSuccess(
    public val body: CollectionsList<ModelList>,
  ) : GetAccountListsResponse()

  @Serializable
  public data class GetAccountListsResponseFailure401(
    public val body: Error,
  ) : GetAccountListsResponse()

  @Serializable
  public object GetAccountListsResponseFailure : GetAccountListsResponse()

  @Serializable
  public data class GetAccountListsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountListsResponse()

  @Serializable
  public sealed class PostAccountMuteResponse

  @Serializable
  public data class PostAccountMuteResponseSuccess(
    public val body: Relationship,
  ) : PostAccountMuteResponse()

  @Serializable
  public data class PostAccountMuteResponseFailure401(
    public val body: Error,
  ) : PostAccountMuteResponse()

  @Serializable
  public object PostAccountMuteResponseFailure : PostAccountMuteResponse()

  @Serializable
  public data class PostAccountMuteResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountMuteResponse()

  @Serializable
  public sealed class PostAccountNoteResponse

  @Serializable
  public data class PostAccountNoteResponseSuccess(
    public val body: Relationship,
  ) : PostAccountNoteResponse()

  @Serializable
  public data class PostAccountNoteResponseFailure401(
    public val body: Error,
  ) : PostAccountNoteResponse()

  @Serializable
  public object PostAccountNoteResponseFailure : PostAccountNoteResponse()

  @Serializable
  public data class PostAccountNoteResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountNoteResponse()

  @Serializable
  public sealed class PostAccountPinResponse

  @Serializable
  public data class PostAccountPinResponseSuccess(
    public val body: Relationship,
  ) : PostAccountPinResponse()

  @Serializable
  public data class PostAccountPinResponseFailure401(
    public val body: Error,
  ) : PostAccountPinResponse()

  @Serializable
  public object PostAccountPinResponseFailure : PostAccountPinResponse()

  @Serializable
  public data class PostAccountPinResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountPinResponse()

  @Serializable
  public sealed class PostAccountRemoveFromFollowersResponse

  @Serializable
  public data class PostAccountRemoveFromFollowersResponseSuccess(
    public val body: Relationship,
  ) : PostAccountRemoveFromFollowersResponse()

  @Serializable
  public data class PostAccountRemoveFromFollowersResponseFailure401(
    public val body: Error,
  ) : PostAccountRemoveFromFollowersResponse()

  @Serializable
  public object PostAccountRemoveFromFollowersResponseFailure : PostAccountRemoveFromFollowersResponse()

  @Serializable
  public data class PostAccountRemoveFromFollowersResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountRemoveFromFollowersResponse()

  @Serializable
  public sealed class GetAccountStatusesResponse

  @Serializable
  public data class GetAccountStatusesResponseSuccess(
    public val body: CollectionsList<Status>,
  ) : GetAccountStatusesResponse()

  @Serializable
  public data class GetAccountStatusesResponseFailure401(
    public val body: Error,
  ) : GetAccountStatusesResponse()

  @Serializable
  public object GetAccountStatusesResponseFailure410 : GetAccountStatusesResponse()

  @Serializable
  public data class GetAccountStatusesResponseFailure(
    public val body: ValidationError,
  ) : GetAccountStatusesResponse()

  @Serializable
  public data class GetAccountStatusesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountStatusesResponse()

  @Serializable
  public sealed class PostAccountUnblockResponse

  @Serializable
  public data class PostAccountUnblockResponseSuccess(
    public val body: Relationship,
  ) : PostAccountUnblockResponse()

  @Serializable
  public data class PostAccountUnblockResponseFailure401(
    public val body: Error,
  ) : PostAccountUnblockResponse()

  @Serializable
  public object PostAccountUnblockResponseFailure : PostAccountUnblockResponse()

  @Serializable
  public data class PostAccountUnblockResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountUnblockResponse()

  @Serializable
  public sealed class PostAccountUnendorseResponse

  @Serializable
  public data class PostAccountUnendorseResponseSuccess(
    public val body: Relationship,
  ) : PostAccountUnendorseResponse()

  @Serializable
  public data class PostAccountUnendorseResponseFailure401(
    public val body: Error,
  ) : PostAccountUnendorseResponse()

  @Serializable
  public object PostAccountUnendorseResponseFailure : PostAccountUnendorseResponse()

  @Serializable
  public data class PostAccountUnendorseResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountUnendorseResponse()

  @Serializable
  public sealed class PostAccountUnfollowResponse

  @Serializable
  public data class PostAccountUnfollowResponseSuccess(
    public val body: Relationship,
  ) : PostAccountUnfollowResponse()

  @Serializable
  public data class PostAccountUnfollowResponseFailure401(
    public val body: Error,
  ) : PostAccountUnfollowResponse()

  @Serializable
  public object PostAccountUnfollowResponseFailure : PostAccountUnfollowResponse()

  @Serializable
  public data class PostAccountUnfollowResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountUnfollowResponse()

  @Serializable
  public sealed class PostAccountUnmuteResponse

  @Serializable
  public data class PostAccountUnmuteResponseSuccess(
    public val body: Relationship,
  ) : PostAccountUnmuteResponse()

  @Serializable
  public data class PostAccountUnmuteResponseFailure401(
    public val body: Error,
  ) : PostAccountUnmuteResponse()

  @Serializable
  public object PostAccountUnmuteResponseFailure : PostAccountUnmuteResponse()

  @Serializable
  public data class PostAccountUnmuteResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountUnmuteResponse()

  @Serializable
  public sealed class PostAccountUnpinResponse

  @Serializable
  public data class PostAccountUnpinResponseSuccess(
    public val body: Relationship,
  ) : PostAccountUnpinResponse()

  @Serializable
  public data class PostAccountUnpinResponseFailure401(
    public val body: Error,
  ) : PostAccountUnpinResponse()

  @Serializable
  public object PostAccountUnpinResponseFailure : PostAccountUnpinResponse()

  @Serializable
  public data class PostAccountUnpinResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostAccountUnpinResponse()

  @Serializable
  public sealed class GetAccountsFamiliarFollowersResponse

  @Serializable
  public data class GetAccountsFamiliarFollowersResponseSuccess(
    public val body: CollectionsList<FamiliarFollowers>,
  ) : GetAccountsFamiliarFollowersResponse()

  @Serializable
  public data class GetAccountsFamiliarFollowersResponseFailure401(
    public val body: Error,
  ) : GetAccountsFamiliarFollowersResponse()

  @Serializable
  public object GetAccountsFamiliarFollowersResponseFailure : GetAccountsFamiliarFollowersResponse()

  @Serializable
  public data class GetAccountsFamiliarFollowersResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountsFamiliarFollowersResponse()

  @Serializable
  public sealed class GetAccountLookupResponse

  @Serializable
  public data class GetAccountLookupResponseSuccess(
    public val body: Account,
  ) : GetAccountLookupResponse()

  @Serializable
  public data class GetAccountLookupResponseFailure401(
    public val body: Error,
  ) : GetAccountLookupResponse()

  @Serializable
  public object GetAccountLookupResponseFailure410 : GetAccountLookupResponse()

  @Serializable
  public data class GetAccountLookupResponseFailure(
    public val body: ValidationError,
  ) : GetAccountLookupResponse()

  @Serializable
  public data class GetAccountLookupResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountLookupResponse()

  @Serializable
  public sealed class GetAccountRelationshipsResponse

  @Serializable
  public data class GetAccountRelationshipsResponseSuccess(
    public val body: CollectionsList<Relationship>,
  ) : GetAccountRelationshipsResponse()

  @Serializable
  public data class GetAccountRelationshipsResponseFailure401(
    public val body: Error,
  ) : GetAccountRelationshipsResponse()

  @Serializable
  public object GetAccountRelationshipsResponseFailure : GetAccountRelationshipsResponse()

  @Serializable
  public data class GetAccountRelationshipsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountRelationshipsResponse()

  @Serializable
  public sealed class GetAccountSearchResponse

  @Serializable
  public data class GetAccountSearchResponseSuccess(
    public val body: CollectionsList<Account>,
  ) : GetAccountSearchResponse()

  @Serializable
  public data class GetAccountSearchResponseFailure401(
    public val body: Error,
  ) : GetAccountSearchResponse()

  @Serializable
  public object GetAccountSearchResponseFailure410 : GetAccountSearchResponse()

  @Serializable
  public data class GetAccountSearchResponseFailure(
    public val body: ValidationError,
  ) : GetAccountSearchResponse()

  @Serializable
  public data class GetAccountSearchResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountSearchResponse()

  @Serializable
  public sealed class PatchAccountsUpdateCredentialsResponse

  @Serializable
  public data class PatchAccountsUpdateCredentialsResponseSuccess(
    public val body: CredentialAccount,
  ) : PatchAccountsUpdateCredentialsResponse()

  @Serializable
  public data class PatchAccountsUpdateCredentialsResponseFailure401(
    public val body: Error,
  ) : PatchAccountsUpdateCredentialsResponse()

  @Serializable
  public object PatchAccountsUpdateCredentialsResponseFailure : PatchAccountsUpdateCredentialsResponse()

  @Serializable
  public data class PatchAccountsUpdateCredentialsResponseUnknownFailure(
    public val statusCode: Int,
  ) : PatchAccountsUpdateCredentialsResponse()

  @Serializable
  public sealed class GetAccountsVerifyCredentialsResponse

  @Serializable
  public data class GetAccountsVerifyCredentialsResponseSuccess(
    public val body: CredentialAccount,
  ) : GetAccountsVerifyCredentialsResponse()

  @Serializable
  public data class GetAccountsVerifyCredentialsResponseFailure401(
    public val body: Error,
  ) : GetAccountsVerifyCredentialsResponse()

  @Serializable
  public object GetAccountsVerifyCredentialsResponseFailure : GetAccountsVerifyCredentialsResponse()

  @Serializable
  public data class GetAccountsVerifyCredentialsResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetAccountsVerifyCredentialsResponse()
}
