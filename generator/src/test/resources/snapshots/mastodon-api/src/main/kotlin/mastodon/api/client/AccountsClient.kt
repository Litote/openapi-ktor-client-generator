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

public interface AccountsClient {
  /**
   * Get multiple accounts
   */
  public suspend fun getAccounts(id: CollectionsList<String>? = null): GetAccountsResponse

  /**
   * Register an account
   */
  public suspend fun createAccount(request: CreateAccountRequest): CreateAccountResponse

  /**
   * Get account
   */
  public suspend fun getAccount(id: String): GetAccountResponse

  /**
   * Block account
   */
  public suspend fun postAccountBlock(id: String): PostAccountBlockResponse

  /**
   * Feature account on your profile
   */
  public suspend fun postAccountEndorse(id: String): PostAccountEndorseResponse

  /**
   * Get featured accounts
   */
  public suspend fun getAccountEndorsements(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    sinceId: String? = null,
  ): GetAccountEndorsementsResponse

  /**
   * Get account's featured tags
   */
  public suspend fun getAccountFeaturedTags(id: String): GetAccountFeaturedTagsResponse

  /**
   * Follow account
   */
  public suspend fun postAccountFollow(request: PostAccountFollowRequest, id: String): PostAccountFollowResponse

  /**
   * Get account's followers
   */
  public suspend fun getAccountFollowers(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetAccountFollowersResponse

  /**
   * Get account's following
   */
  public suspend fun getAccountFollowing(
    id: String,
    limit: Long? = 40,
    maxId: String? = null,
    minId: String? = null,
    sinceId: String? = null,
  ): GetAccountFollowingResponse

  /**
   * Identity proofs
   */
  public suspend fun getAccountIdentityProofs(id: String): GetAccountIdentityProofsResponse

  /**
   * Get lists containing this account
   */
  public suspend fun getAccountLists(id: String): GetAccountListsResponse

  /**
   * Mute account
   */
  public suspend fun postAccountMute(request: PostAccountMuteRequest, id: String): PostAccountMuteResponse

  /**
   * Set private note on profile
   */
  public suspend fun postAccountNote(request: PostAccountNoteRequest, id: String): PostAccountNoteResponse

  /**
   * Feature account on your profile
   */
  public suspend fun postAccountPin(id: String): PostAccountPinResponse

  /**
   * Remove account from followers
   */
  public suspend fun postAccountRemoveFromFollowers(id: String): PostAccountRemoveFromFollowersResponse

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
  ): GetAccountStatusesResponse

  /**
   * Unblock account
   */
  public suspend fun postAccountUnblock(id: String): PostAccountUnblockResponse

  /**
   * Unfeature account from profile
   */
  public suspend fun postAccountUnendorse(id: String): PostAccountUnendorseResponse

  /**
   * Unfollow account
   */
  public suspend fun postAccountUnfollow(id: String): PostAccountUnfollowResponse

  /**
   * Unmute account
   */
  public suspend fun postAccountUnmute(id: String): PostAccountUnmuteResponse

  /**
   * Unfeature account from profile
   */
  public suspend fun postAccountUnpin(id: String): PostAccountUnpinResponse

  /**
   * Find familiar followers
   */
  public suspend fun getAccountsFamiliarFollowers(id: CollectionsList<String>? = null): GetAccountsFamiliarFollowersResponse

  /**
   * Lookup account ID from WebFinger address
   */
  public suspend fun getAccountLookup(acct: String): GetAccountLookupResponse

  /**
   * Check relationships to other accounts
   */
  public suspend fun getAccountRelationships(id: CollectionsList<String>? = null, withSuspended: Boolean? = false): GetAccountRelationshipsResponse

  /**
   * Search for matching accounts
   */
  public suspend fun getAccountSearch(
    q: String,
    following: Boolean? = false,
    limit: Long? = 40,
    offset: Long? = null,
    resolve: Boolean? = false,
  ): GetAccountSearchResponse

  /**
   * Update account credentials
   */
  public suspend fun patchAccountsUpdateCredentials(request: PatchAccountsUpdateCredentialsRequest): PatchAccountsUpdateCredentialsResponse

  /**
   * Verify account credentials
   */
  public suspend fun getAccountsVerifyCredentials(): GetAccountsVerifyCredentialsResponse

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

public fun AccountsClient(configuration: ClientConfiguration = defaultClientConfiguration): AccountsClient = DefaultAccountsClient(configuration)

public class DefaultAccountsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : AccountsClient {
  override suspend fun getAccounts(id: CollectionsList<String>?): AccountsClient.GetAccountsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts") {
        url {
          if (id != null) {
            parameters.appendAll("id", id)
          }
        }
      }
      return when (response.status.value) {
        200 -> AccountsClient.GetAccountsResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> AccountsClient.GetAccountsResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountsResponseFailure410(response.headers)
        422 -> AccountsClient.GetAccountsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> AccountsClient.GetAccountsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountsResponseUnknownFailure(500)
    }
  }

  override suspend fun createAccount(request: AccountsClient.CreateAccountRequest): AccountsClient.CreateAccountResponse {
    try {
      val response = configuration.client.post("api/v1/accounts") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> AccountsClient.CreateAccountResponseSuccess(response.body<Token>(), response.headers)
        401, 404, 429, 503 -> AccountsClient.CreateAccountResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.CreateAccountResponseFailure410(response.headers)
        422 -> AccountsClient.CreateAccountResponseFailure(response.body<ValidationError>(), response.headers)
        else -> AccountsClient.CreateAccountResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.CreateAccountResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccount(id: String): AccountsClient.GetAccountResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.GetAccountResponseSuccess(response.body<Account>(), response.headers)
        401, 404, 429, 503 -> AccountsClient.GetAccountResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountResponseFailure410(response.headers)
        422 -> AccountsClient.GetAccountResponseFailure(response.body<ValidationError>(), response.headers)
        else -> AccountsClient.GetAccountResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountBlock(id: String): AccountsClient.PostAccountBlockResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/block".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountBlockResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.PostAccountBlockResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountBlockResponseFailure(response.headers)
        else -> AccountsClient.PostAccountBlockResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountBlockResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountEndorse(id: String): AccountsClient.PostAccountEndorseResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/endorse".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountEndorseResponseSuccess(response.body<Relationship>(), response.headers)
        401, 403, 404, 422, 429, 500, 503 -> AccountsClient.PostAccountEndorseResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountEndorseResponseFailure(response.headers)
        else -> AccountsClient.PostAccountEndorseResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountEndorseResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountEndorsements(
    id: String,
    limit: Long?,
    maxId: String?,
    sinceId: String?,
  ): AccountsClient.GetAccountEndorsementsResponse {
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
        200 -> AccountsClient.GetAccountEndorsementsResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> AccountsClient.GetAccountEndorsementsResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountEndorsementsResponseFailure410(response.headers)
        422 -> AccountsClient.GetAccountEndorsementsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> AccountsClient.GetAccountEndorsementsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountEndorsementsResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountFeaturedTags(id: String): AccountsClient.GetAccountFeaturedTagsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}/featured_tags".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.GetAccountFeaturedTagsResponseSuccess(response.body<CollectionsList<FeaturedTag>>(), response.headers)
        401, 404, 429, 503 -> AccountsClient.GetAccountFeaturedTagsResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountFeaturedTagsResponseFailure410(response.headers)
        422 -> AccountsClient.GetAccountFeaturedTagsResponseFailure(response.body<ValidationError>(), response.headers)
        else -> AccountsClient.GetAccountFeaturedTagsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountFeaturedTagsResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountFollow(request: AccountsClient.PostAccountFollowRequest, id: String): AccountsClient.PostAccountFollowResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/follow".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountFollowResponseSuccess(response.body<Relationship>(), response.headers)
        401, 403, 404, 422, 429, 503 -> AccountsClient.PostAccountFollowResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountFollowResponseFailure(response.headers)
        else -> AccountsClient.PostAccountFollowResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountFollowResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountFollowers(
    id: String,
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): AccountsClient.GetAccountFollowersResponse {
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
        200 -> AccountsClient.GetAccountFollowersResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> AccountsClient.GetAccountFollowersResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountFollowersResponseFailure410(response.headers)
        422 -> AccountsClient.GetAccountFollowersResponseFailure(response.body<ValidationError>(), response.headers)
        else -> AccountsClient.GetAccountFollowersResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountFollowersResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountFollowing(
    id: String,
    limit: Long?,
    maxId: String?,
    minId: String?,
    sinceId: String?,
  ): AccountsClient.GetAccountFollowingResponse {
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
        200 -> AccountsClient.GetAccountFollowingResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> AccountsClient.GetAccountFollowingResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountFollowingResponseFailure410(response.headers)
        422 -> AccountsClient.GetAccountFollowingResponseFailure(response.body<ValidationError>(), response.headers)
        else -> AccountsClient.GetAccountFollowingResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountFollowingResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountIdentityProofs(id: String): AccountsClient.GetAccountIdentityProofsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}/identity_proofs".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.GetAccountIdentityProofsResponseSuccess(response.body<CollectionsList<IdentityProof>>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.GetAccountIdentityProofsResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountIdentityProofsResponseFailure(response.headers)
        else -> AccountsClient.GetAccountIdentityProofsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountIdentityProofsResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountLists(id: String): AccountsClient.GetAccountListsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/{id}/lists".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.GetAccountListsResponseSuccess(response.body<CollectionsList<ModelList>>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.GetAccountListsResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountListsResponseFailure(response.headers)
        else -> AccountsClient.GetAccountListsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountListsResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountMute(request: AccountsClient.PostAccountMuteRequest, id: String): AccountsClient.PostAccountMuteResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/mute".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountMuteResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.PostAccountMuteResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountMuteResponseFailure(response.headers)
        else -> AccountsClient.PostAccountMuteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountMuteResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountNote(request: AccountsClient.PostAccountNoteRequest, id: String): AccountsClient.PostAccountNoteResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/note".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountNoteResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.PostAccountNoteResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountNoteResponseFailure(response.headers)
        else -> AccountsClient.PostAccountNoteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountNoteResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountPin(id: String): AccountsClient.PostAccountPinResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/pin".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountPinResponseSuccess(response.body<Relationship>(), response.headers)
        401, 403, 404, 422, 429, 500, 503 -> AccountsClient.PostAccountPinResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountPinResponseFailure(response.headers)
        else -> AccountsClient.PostAccountPinResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountPinResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountRemoveFromFollowers(id: String): AccountsClient.PostAccountRemoveFromFollowersResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/remove_from_followers".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountRemoveFromFollowersResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.PostAccountRemoveFromFollowersResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountRemoveFromFollowersResponseFailure(response.headers)
        else -> AccountsClient.PostAccountRemoveFromFollowersResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountRemoveFromFollowersResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountStatuses(
    id: String,
    excludeReblogs: Boolean?,
    excludeReplies: Boolean?,
    limit: Long?,
    maxId: String?,
    minId: String?,
    onlyMedia: Boolean?,
    pinned: Boolean?,
    sinceId: String?,
    tagged: String?,
  ): AccountsClient.GetAccountStatusesResponse {
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
        200 -> AccountsClient.GetAccountStatusesResponseSuccess(response.body<CollectionsList<Status>>(), response.headers)
        401, 404, 429, 503 -> AccountsClient.GetAccountStatusesResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountStatusesResponseFailure410(response.headers)
        422 -> AccountsClient.GetAccountStatusesResponseFailure(response.body<ValidationError>(), response.headers)
        else -> AccountsClient.GetAccountStatusesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountStatusesResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountUnblock(id: String): AccountsClient.PostAccountUnblockResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/unblock".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountUnblockResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.PostAccountUnblockResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountUnblockResponseFailure(response.headers)
        else -> AccountsClient.PostAccountUnblockResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountUnblockResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountUnendorse(id: String): AccountsClient.PostAccountUnendorseResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/unendorse".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountUnendorseResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.PostAccountUnendorseResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountUnendorseResponseFailure(response.headers)
        else -> AccountsClient.PostAccountUnendorseResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountUnendorseResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountUnfollow(id: String): AccountsClient.PostAccountUnfollowResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/unfollow".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountUnfollowResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.PostAccountUnfollowResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountUnfollowResponseFailure(response.headers)
        else -> AccountsClient.PostAccountUnfollowResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountUnfollowResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountUnmute(id: String): AccountsClient.PostAccountUnmuteResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/unmute".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountUnmuteResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.PostAccountUnmuteResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountUnmuteResponseFailure(response.headers)
        else -> AccountsClient.PostAccountUnmuteResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountUnmuteResponseUnknownFailure(500)
    }
  }

  override suspend fun postAccountUnpin(id: String): AccountsClient.PostAccountUnpinResponse {
    try {
      val response = configuration.client.post("api/v1/accounts/{id}/unpin".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> AccountsClient.PostAccountUnpinResponseSuccess(response.body<Relationship>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.PostAccountUnpinResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PostAccountUnpinResponseFailure(response.headers)
        else -> AccountsClient.PostAccountUnpinResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PostAccountUnpinResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountsFamiliarFollowers(id: CollectionsList<String>?): AccountsClient.GetAccountsFamiliarFollowersResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/familiar_followers") {
        url {
          if (id != null) {
            parameters.appendAll("id", id)
          }
        }
      }
      return when (response.status.value) {
        200 -> AccountsClient.GetAccountsFamiliarFollowersResponseSuccess(response.body<CollectionsList<FamiliarFollowers>>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.GetAccountsFamiliarFollowersResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountsFamiliarFollowersResponseFailure(response.headers)
        else -> AccountsClient.GetAccountsFamiliarFollowersResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountsFamiliarFollowersResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountLookup(acct: String): AccountsClient.GetAccountLookupResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/lookup") {
        url {
          parameters.append("acct", acct)
        }
      }
      return when (response.status.value) {
        200 -> AccountsClient.GetAccountLookupResponseSuccess(response.body<Account>(), response.headers)
        401, 404, 429, 503 -> AccountsClient.GetAccountLookupResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountLookupResponseFailure410(response.headers)
        422 -> AccountsClient.GetAccountLookupResponseFailure(response.body<ValidationError>(), response.headers)
        else -> AccountsClient.GetAccountLookupResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountLookupResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountRelationships(id: CollectionsList<String>?, withSuspended: Boolean?): AccountsClient.GetAccountRelationshipsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/relationships") {
        url {
          if (id != null) {
            parameters.appendAll("id", id)
          }
          if (withSuspended != null) {
            parameters.append("with_suspended", withSuspended.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> AccountsClient.GetAccountRelationshipsResponseSuccess(response.body<CollectionsList<Relationship>>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.GetAccountRelationshipsResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountRelationshipsResponseFailure(response.headers)
        else -> AccountsClient.GetAccountRelationshipsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountRelationshipsResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountSearch(
    q: String,
    following: Boolean?,
    limit: Long?,
    offset: Long?,
    resolve: Boolean?,
  ): AccountsClient.GetAccountSearchResponse {
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
        200 -> AccountsClient.GetAccountSearchResponseSuccess(response.body<CollectionsList<Account>>(), response.headers)
        401, 404, 429, 503 -> AccountsClient.GetAccountSearchResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountSearchResponseFailure410(response.headers)
        422 -> AccountsClient.GetAccountSearchResponseFailure(response.body<ValidationError>(), response.headers)
        else -> AccountsClient.GetAccountSearchResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountSearchResponseUnknownFailure(500)
    }
  }

  override suspend fun patchAccountsUpdateCredentials(request: AccountsClient.PatchAccountsUpdateCredentialsRequest): AccountsClient.PatchAccountsUpdateCredentialsResponse {
    try {
      val response = configuration.client.patch("api/v1/accounts/update_credentials") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> AccountsClient.PatchAccountsUpdateCredentialsResponseSuccess(response.body<CredentialAccount>(), response.headers)
        401, 404, 422, 429, 503 -> AccountsClient.PatchAccountsUpdateCredentialsResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.PatchAccountsUpdateCredentialsResponseFailure(response.headers)
        else -> AccountsClient.PatchAccountsUpdateCredentialsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.PatchAccountsUpdateCredentialsResponseUnknownFailure(500)
    }
  }

  override suspend fun getAccountsVerifyCredentials(): AccountsClient.GetAccountsVerifyCredentialsResponse {
    try {
      val response = configuration.client.`get`("api/v1/accounts/verify_credentials") {
      }
      return when (response.status.value) {
        200 -> AccountsClient.GetAccountsVerifyCredentialsResponseSuccess(response.body<CredentialAccount>(), response.headers)
        401, 403, 404, 422, 429, 503 -> AccountsClient.GetAccountsVerifyCredentialsResponseFailure401(response.body<Error>(), response.headers)
        410 -> AccountsClient.GetAccountsVerifyCredentialsResponseFailure(response.headers)
        else -> AccountsClient.GetAccountsVerifyCredentialsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AccountsClient.GetAccountsVerifyCredentialsResponseUnknownFailure(500)
    }
  }
}
