package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
import mastodon.api.model.StatusVisibilityEnum
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
        200 -> GetAccountsResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> GetAccountsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountsResponseFailure410(response.headers)
        422 -> GetAccountsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAccountsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountsResponseUnknownFailure(500)
    }
  }

  /**
   * Register an account
   */
  public suspend fun createAccount(request: CreateAccountRequest): CreateAccountResponse {
    try {
      val response = configuration.client.post("api/v1/accounts") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateAccountResponseSuccess(response.body<Token>(), response.headers)
        401, 404, 429, 503 -> CreateAccountResponseFailure401(response.body<Error>(), response.headers)
        410 -> CreateAccountResponseFailure410(response.headers)
        422 -> CreateAccountResponseFailure(response.body<ValidationError>(), response.headers)
        else -> CreateAccountResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountResponseSuccess(response.body<Account>(), response.headers)
        401, 404, 429, 503 -> GetAccountResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountResponseFailure410(response.headers)
        422 -> GetAccountResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAccountResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> PostAccountBlockResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> PostAccountBlockResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountBlockResponseFailure(response.headers)
        else -> PostAccountBlockResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> PostAccountEndorseResponseSuccess(response.body<Relationship>(), response.headers)
        401, 403, 404, 422, 429, 500, 503 -> PostAccountEndorseResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountEndorseResponseFailure(response.headers)
        else -> PostAccountEndorseResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountEndorsementsResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> GetAccountEndorsementsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountEndorsementsResponseFailure410(response.headers)
        422 -> GetAccountEndorsementsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAccountEndorsementsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountFeaturedTagsResponseSuccess(response.body<CollectionsList<FeaturedTag>>(), response.headers)
        401, 404, 429, 503 -> GetAccountFeaturedTagsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountFeaturedTagsResponseFailure410(response.headers)
        422 -> GetAccountFeaturedTagsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAccountFeaturedTagsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountFeaturedTagsResponseUnknownFailure(500)
    }
  }

  /**
   * Follow account
   */
  public suspend fun postAccountFollow(request: PostAccountFollowRequest, id: String): PostAccountFollowResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/follow".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostAccountFollowResponseSuccess(response.body<Relationship>(), response.headers)
        401, 403, 404, 422, 429, 503 -> PostAccountFollowResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountFollowResponseFailure(response.headers)
        else -> PostAccountFollowResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountFollowersResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> GetAccountFollowersResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountFollowersResponseFailure410(response.headers)
        422 -> GetAccountFollowersResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAccountFollowersResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountFollowingResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> GetAccountFollowingResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountFollowingResponseFailure410(response.headers)
        422 -> GetAccountFollowingResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAccountFollowingResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountIdentityProofsResponseSuccess(response.body<CollectionsList<IdentityProof>>(), response.headers)
        401, 404, 422, 429, 503 -> GetAccountIdentityProofsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountIdentityProofsResponseFailure(response.headers)
        else -> GetAccountIdentityProofsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountListsResponseSuccess(response.body<CollectionsList<ModelList>>(), response.headers)
        401, 404, 422, 429, 503 -> GetAccountListsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountListsResponseFailure(response.headers)
        else -> GetAccountListsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountListsResponseUnknownFailure(500)
    }
  }

  /**
   * Mute account
   */
  public suspend fun postAccountMute(request: PostAccountMuteRequest, id: String): PostAccountMuteResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/mute".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostAccountMuteResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> PostAccountMuteResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountMuteResponseFailure(response.headers)
        else -> PostAccountMuteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostAccountMuteResponseUnknownFailure(500)
    }
  }

  /**
   * Set private note on profile
   */
  public suspend fun postAccountNote(request: PostAccountNoteRequest, id: String): PostAccountNoteResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/note".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PostAccountNoteResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> PostAccountNoteResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountNoteResponseFailure(response.headers)
        else -> PostAccountNoteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> PostAccountPinResponseSuccess(response.body<Relationship>(), response.headers)
        401, 403, 404, 422, 429, 500, 503 -> PostAccountPinResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountPinResponseFailure(response.headers)
        else -> PostAccountPinResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> PostAccountRemoveFromFollowersResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> PostAccountRemoveFromFollowersResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountRemoveFromFollowersResponseFailure(response.headers)
        else -> PostAccountRemoveFromFollowersResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountStatusesResponseSuccess(response.body<CollectionsList<Status>>(), response.headers)
        401, 404, 429, 503 -> GetAccountStatusesResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountStatusesResponseFailure410(response.headers)
        422 -> GetAccountStatusesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAccountStatusesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> PostAccountUnblockResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> PostAccountUnblockResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountUnblockResponseFailure(response.headers)
        else -> PostAccountUnblockResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> PostAccountUnendorseResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> PostAccountUnendorseResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountUnendorseResponseFailure(response.headers)
        else -> PostAccountUnendorseResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> PostAccountUnfollowResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> PostAccountUnfollowResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountUnfollowResponseFailure(response.headers)
        else -> PostAccountUnfollowResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> PostAccountUnmuteResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> PostAccountUnmuteResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountUnmuteResponseFailure(response.headers)
        else -> PostAccountUnmuteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> PostAccountUnpinResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> PostAccountUnpinResponseFailure401(response.body<Error>(), response.headers)
        410 -> PostAccountUnpinResponseFailure(response.headers)
        else -> PostAccountUnpinResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountsFamiliarFollowersResponseSuccess(response.body<CollectionsList<FamiliarFollowers>>(), response.headers)
        401, 404, 422, 429, 503 -> GetAccountsFamiliarFollowersResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountsFamiliarFollowersResponseFailure(response.headers)
        else -> GetAccountsFamiliarFollowersResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountLookupResponseSuccess(response.body<Account>(), response.headers)
        401, 404, 429, 503 -> GetAccountLookupResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountLookupResponseFailure410(response.headers)
        422 -> GetAccountLookupResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAccountLookupResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountRelationshipsResponseSuccess(response.body<CollectionsList<Relationship>>(), response.headers)
        401, 404, 422, 429, 503 -> GetAccountRelationshipsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountRelationshipsResponseFailure(response.headers)
        else -> GetAccountRelationshipsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountSearchResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> GetAccountSearchResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountSearchResponseFailure410(response.headers)
        422 -> GetAccountSearchResponseFailure(response.body<ValidationError>(), response.headers)
        else -> GetAccountSearchResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetAccountSearchResponseUnknownFailure(500)
    }
  }

  /**
   * Update account credentials
   */
  public suspend fun patchAccountsUpdateCredentials(request: PatchAccountsUpdateCredentialsRequest): PatchAccountsUpdateCredentialsResponse {
    try {
      val response = configuration.client.patch("api/v1/accounts/update_credentials") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> PatchAccountsUpdateCredentialsResponseSuccess(response.body<CredentialAccount>(), response.headers)
        401, 404, 422, 429, 503 -> PatchAccountsUpdateCredentialsResponseFailure401(response.body<Error>(), response.headers)
        410 -> PatchAccountsUpdateCredentialsResponseFailure(response.headers)
        else -> PatchAccountsUpdateCredentialsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
        200 -> GetAccountsVerifyCredentialsResponseSuccess(response.body<CredentialAccount>(), response.headers)
        401, 403, 404, 422, 429, 503 -> GetAccountsVerifyCredentialsResponseFailure401(response.body<Error>(), response.headers)
        410 -> GetAccountsVerifyCredentialsResponseFailure(response.headers)
        else -> GetAccountsVerifyCredentialsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
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
  public sealed class GetAccountsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountsResponseSuccess(
    public val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsResponse() {
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
  public data class GetAccountsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsResponse()

  @Serializable
  public data class GetAccountsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsResponse()

  @Serializable
  public data class GetAccountsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsResponse()

  @Serializable
  public data class GetAccountsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsResponse()

  @Serializable
  public data class CreateAccountRequest(
    public val agreement: Boolean,
    @SerialName("date_of_birth")
    public val dateOfBirth: String? = null,
    public val email: String,
    public val locale: String,
    public val password: String,
    public val reason: String? = null,
    public val username: String,
  )

  @Serializable
  public sealed class CreateAccountResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateAccountResponseSuccess(
    public val body: Token,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAccountResponse() {
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
  public data class CreateAccountResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAccountResponse()

  @Serializable
  public data class CreateAccountResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAccountResponse()

  @Serializable
  public data class CreateAccountResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAccountResponse()

  @Serializable
  public data class CreateAccountResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAccountResponse()

  @Serializable
  public sealed class GetAccountResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountResponseSuccess(
    public val body: Account,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountResponse() {
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
  public data class GetAccountResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountResponse()

  @Serializable
  public data class GetAccountResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountResponse()

  @Serializable
  public data class GetAccountResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountResponse()

  @Serializable
  public data class GetAccountResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountResponse()

  @Serializable
  public sealed class PostAccountBlockResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountBlockResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountBlockResponse() {
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
  public data class PostAccountBlockResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountBlockResponse()

  @Serializable
  public data class PostAccountBlockResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountBlockResponse()

  @Serializable
  public data class PostAccountBlockResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountBlockResponse()

  @Serializable
  public sealed class PostAccountEndorseResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountEndorseResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountEndorseResponse() {
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
  public data class PostAccountEndorseResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountEndorseResponse()

  @Serializable
  public data class PostAccountEndorseResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountEndorseResponse()

  @Serializable
  public data class PostAccountEndorseResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountEndorseResponse()

  @Serializable
  public sealed class GetAccountEndorsementsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountEndorsementsResponseSuccess(
    public val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountEndorsementsResponse() {
    /**
     * Pagination links for browsing older or newer results. Format: Link: <https://mastodon.example/api/v1/endpoint?max_id=7163058>; rel="next", <https://mastodon.example/api/v1/endpoint?min_id=7275607>; rel="prev". See [RFC 8288](https://www.rfc-editor.org/rfc/rfc8288) for more information.
     */
    public val link: String?
      get() = headers["Link"]

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
  public data class GetAccountEndorsementsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountEndorsementsResponse()

  @Serializable
  public data class GetAccountEndorsementsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountEndorsementsResponse()

  @Serializable
  public data class GetAccountEndorsementsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountEndorsementsResponse()

  @Serializable
  public data class GetAccountEndorsementsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountEndorsementsResponse()

  @Serializable
  public sealed class GetAccountFeaturedTagsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountFeaturedTagsResponseSuccess(
    public val body: CollectionsList<FeaturedTag>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFeaturedTagsResponse() {
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
  public data class GetAccountFeaturedTagsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFeaturedTagsResponse()

  @Serializable
  public data class GetAccountFeaturedTagsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFeaturedTagsResponse()

  @Serializable
  public data class GetAccountFeaturedTagsResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFeaturedTagsResponse()

  @Serializable
  public data class GetAccountFeaturedTagsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFeaturedTagsResponse()

  @Serializable
  public data class PostAccountFollowRequest(
    public val languages: CollectionsList<String>? = null,
    public val notify: Boolean? = false,
    public val reblogs: Boolean? = true,
  )

  @Serializable
  public sealed class PostAccountFollowResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountFollowResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountFollowResponse() {
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
  public data class PostAccountFollowResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountFollowResponse()

  @Serializable
  public data class PostAccountFollowResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountFollowResponse()

  @Serializable
  public data class PostAccountFollowResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountFollowResponse()

  @Serializable
  public sealed class GetAccountFollowersResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountFollowersResponseSuccess(
    public val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowersResponse() {
    /**
     * Pagination links for browsing older or newer results. Format: Link: <https://mastodon.example/api/v1/endpoint?max_id=7163058>; rel="next", <https://mastodon.example/api/v1/endpoint?min_id=7275607>; rel="prev". See [RFC 8288](https://www.rfc-editor.org/rfc/rfc8288) for more information.
     */
    public val link: String?
      get() = headers["Link"]

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
  public data class GetAccountFollowersResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowersResponse()

  @Serializable
  public data class GetAccountFollowersResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowersResponse()

  @Serializable
  public data class GetAccountFollowersResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowersResponse()

  @Serializable
  public data class GetAccountFollowersResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowersResponse()

  @Serializable
  public sealed class GetAccountFollowingResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountFollowingResponseSuccess(
    public val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowingResponse() {
    /**
     * Pagination links for browsing older or newer results. Format: Link: <https://mastodon.example/api/v1/endpoint?max_id=7163058>; rel="next", <https://mastodon.example/api/v1/endpoint?min_id=7275607>; rel="prev". See [RFC 8288](https://www.rfc-editor.org/rfc/rfc8288) for more information.
     */
    public val link: String?
      get() = headers["Link"]

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
  public data class GetAccountFollowingResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowingResponse()

  @Serializable
  public data class GetAccountFollowingResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowingResponse()

  @Serializable
  public data class GetAccountFollowingResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowingResponse()

  @Serializable
  public data class GetAccountFollowingResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowingResponse()

  @Serializable
  public sealed class GetAccountIdentityProofsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountIdentityProofsResponseSuccess(
    public val body: CollectionsList<IdentityProof>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountIdentityProofsResponse() {
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
  public data class GetAccountIdentityProofsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountIdentityProofsResponse()

  @Serializable
  public data class GetAccountIdentityProofsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountIdentityProofsResponse()

  @Serializable
  public data class GetAccountIdentityProofsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountIdentityProofsResponse()

  @Serializable
  public sealed class GetAccountListsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountListsResponseSuccess(
    public val body: CollectionsList<ModelList>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountListsResponse() {
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
  public data class GetAccountListsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountListsResponse()

  @Serializable
  public data class GetAccountListsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountListsResponse()

  @Serializable
  public data class GetAccountListsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountListsResponse()

  @Serializable
  public data class PostAccountMuteRequest(
    public val duration: Long? = 0,
    public val notifications: Boolean? = true,
  )

  @Serializable
  public sealed class PostAccountMuteResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountMuteResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountMuteResponse() {
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
  public data class PostAccountMuteResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountMuteResponse()

  @Serializable
  public data class PostAccountMuteResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountMuteResponse()

  @Serializable
  public data class PostAccountMuteResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountMuteResponse()

  @Serializable
  public data class PostAccountNoteRequest(
    public val comment: String? = null,
  )

  @Serializable
  public sealed class PostAccountNoteResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountNoteResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountNoteResponse() {
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
  public data class PostAccountNoteResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountNoteResponse()

  @Serializable
  public data class PostAccountNoteResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountNoteResponse()

  @Serializable
  public data class PostAccountNoteResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountNoteResponse()

  @Serializable
  public sealed class PostAccountPinResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountPinResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountPinResponse() {
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
  public data class PostAccountPinResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountPinResponse()

  @Serializable
  public data class PostAccountPinResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountPinResponse()

  @Serializable
  public data class PostAccountPinResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountPinResponse()

  @Serializable
  public sealed class PostAccountRemoveFromFollowersResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountRemoveFromFollowersResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountRemoveFromFollowersResponse() {
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
  public data class PostAccountRemoveFromFollowersResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountRemoveFromFollowersResponse()

  @Serializable
  public data class PostAccountRemoveFromFollowersResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountRemoveFromFollowersResponse()

  @Serializable
  public data class PostAccountRemoveFromFollowersResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountRemoveFromFollowersResponse()

  @Serializable
  public sealed class GetAccountStatusesResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountStatusesResponseSuccess(
    public val body: CollectionsList<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountStatusesResponse() {
    /**
     * Pagination links for browsing older or newer results. Format: Link: <https://mastodon.example/api/v1/endpoint?max_id=7163058>; rel="next", <https://mastodon.example/api/v1/endpoint?min_id=7275607>; rel="prev". See [RFC 8288](https://www.rfc-editor.org/rfc/rfc8288) for more information.
     */
    public val link: String?
      get() = headers["Link"]

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
  public data class GetAccountStatusesResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountStatusesResponse()

  @Serializable
  public data class GetAccountStatusesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountStatusesResponse()

  @Serializable
  public data class GetAccountStatusesResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountStatusesResponse()

  @Serializable
  public data class GetAccountStatusesResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountStatusesResponse()

  @Serializable
  public sealed class PostAccountUnblockResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountUnblockResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnblockResponse() {
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
  public data class PostAccountUnblockResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnblockResponse()

  @Serializable
  public data class PostAccountUnblockResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnblockResponse()

  @Serializable
  public data class PostAccountUnblockResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnblockResponse()

  @Serializable
  public sealed class PostAccountUnendorseResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountUnendorseResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnendorseResponse() {
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
  public data class PostAccountUnendorseResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnendorseResponse()

  @Serializable
  public data class PostAccountUnendorseResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnendorseResponse()

  @Serializable
  public data class PostAccountUnendorseResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnendorseResponse()

  @Serializable
  public sealed class PostAccountUnfollowResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountUnfollowResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnfollowResponse() {
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
  public data class PostAccountUnfollowResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnfollowResponse()

  @Serializable
  public data class PostAccountUnfollowResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnfollowResponse()

  @Serializable
  public data class PostAccountUnfollowResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnfollowResponse()

  @Serializable
  public sealed class PostAccountUnmuteResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountUnmuteResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnmuteResponse() {
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
  public data class PostAccountUnmuteResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnmuteResponse()

  @Serializable
  public data class PostAccountUnmuteResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnmuteResponse()

  @Serializable
  public data class PostAccountUnmuteResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnmuteResponse()

  @Serializable
  public sealed class PostAccountUnpinResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostAccountUnpinResponseSuccess(
    public val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnpinResponse() {
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
  public data class PostAccountUnpinResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnpinResponse()

  @Serializable
  public data class PostAccountUnpinResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnpinResponse()

  @Serializable
  public data class PostAccountUnpinResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnpinResponse()

  @Serializable
  public sealed class GetAccountsFamiliarFollowersResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountsFamiliarFollowersResponseSuccess(
    public val body: CollectionsList<FamiliarFollowers>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsFamiliarFollowersResponse() {
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
  public data class GetAccountsFamiliarFollowersResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsFamiliarFollowersResponse()

  @Serializable
  public data class GetAccountsFamiliarFollowersResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsFamiliarFollowersResponse()

  @Serializable
  public data class GetAccountsFamiliarFollowersResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsFamiliarFollowersResponse()

  @Serializable
  public sealed class GetAccountLookupResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountLookupResponseSuccess(
    public val body: Account,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountLookupResponse() {
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
  public data class GetAccountLookupResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountLookupResponse()

  @Serializable
  public data class GetAccountLookupResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountLookupResponse()

  @Serializable
  public data class GetAccountLookupResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountLookupResponse()

  @Serializable
  public data class GetAccountLookupResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountLookupResponse()

  @Serializable
  public sealed class GetAccountRelationshipsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountRelationshipsResponseSuccess(
    public val body: CollectionsList<Relationship>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountRelationshipsResponse() {
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
  public data class GetAccountRelationshipsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountRelationshipsResponse()

  @Serializable
  public data class GetAccountRelationshipsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountRelationshipsResponse()

  @Serializable
  public data class GetAccountRelationshipsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountRelationshipsResponse()

  @Serializable
  public sealed class GetAccountSearchResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountSearchResponseSuccess(
    public val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountSearchResponse() {
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
  public data class GetAccountSearchResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountSearchResponse()

  @Serializable
  public data class GetAccountSearchResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountSearchResponse()

  @Serializable
  public data class GetAccountSearchResponseFailure(
    public val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountSearchResponse()

  @Serializable
  public data class GetAccountSearchResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountSearchResponse()

  @Serializable
  public data class PatchAccountsUpdateCredentialsRequest(
    @SerialName("attribution_domains")
    public val attributionDomains: CollectionsList<String>? = null,
    public val avatar: String? = null,
    public val bot: Boolean? = null,
    public val discoverable: Boolean? = null,
    @SerialName("display_name")
    public val displayName: String? = null,
    @SerialName("fields_attributes")
    public val fieldsAttributes: JsonElement? = null,
    public val `header`: String? = null,
    @SerialName("hide_collections")
    public val hideCollections: Boolean? = null,
    public val indexable: Boolean? = null,
    public val locked: Boolean? = null,
    public val note: String? = null,
    public val source: Source? = null,
  ) {
    @Serializable
    public data class Source(
      public val language: String? = null,
      public val privacy: StatusVisibilityEnum? = null,
      @SerialName("quote_policy")
      public val quotePolicy: String? = null,
      public val sensitive: Boolean? = null,
    )
  }

  @Serializable
  public sealed class PatchAccountsUpdateCredentialsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PatchAccountsUpdateCredentialsResponseSuccess(
    public val body: CredentialAccount,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PatchAccountsUpdateCredentialsResponse() {
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
  public data class PatchAccountsUpdateCredentialsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PatchAccountsUpdateCredentialsResponse()

  @Serializable
  public data class PatchAccountsUpdateCredentialsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PatchAccountsUpdateCredentialsResponse()

  @Serializable
  public data class PatchAccountsUpdateCredentialsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PatchAccountsUpdateCredentialsResponse()

  @Serializable
  public sealed class GetAccountsVerifyCredentialsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetAccountsVerifyCredentialsResponseSuccess(
    public val body: CredentialAccount,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsVerifyCredentialsResponse() {
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
  public data class GetAccountsVerifyCredentialsResponseFailure401(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsVerifyCredentialsResponse()

  @Serializable
  public data class GetAccountsVerifyCredentialsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsVerifyCredentialsResponse()

  @Serializable
  public data class GetAccountsVerifyCredentialsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsVerifyCredentialsResponse()
}
