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
import kotlin.Unit
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
  public sealed class GetAccountsResponse : OperationResponse<CollectionsList<Account>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountsResponseSuccess(
    override val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsResponse(),
      OperationResponseSuccess<CollectionsList<Account>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountsResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetAccountsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsResponse(),
      OperationResponseUnknownFailure

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
  public sealed class CreateAccountResponse : OperationResponse<Token> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class CreateAccountResponseSuccess(
    override val body: Token,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAccountResponse(),
      OperationResponseSuccess<Token> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAccountResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class CreateAccountResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAccountResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class CreateAccountResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAccountResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class CreateAccountResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateAccountResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountResponse : OperationResponse<Account> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountResponseSuccess(
    override val body: Account,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountResponse(),
      OperationResponseSuccess<Account> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetAccountResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostAccountBlockResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountBlockResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountBlockResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountBlockResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountBlockResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountBlockResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountBlockResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountBlockResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostAccountEndorseResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountEndorseResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountEndorseResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountEndorseResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountEndorseResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountEndorseResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountEndorseResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountEndorseResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountEndorsementsResponse : OperationResponse<CollectionsList<Account>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountEndorsementsResponseSuccess(
    override val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountEndorsementsResponse(),
      OperationResponseSuccess<CollectionsList<Account>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountEndorsementsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountEndorsementsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountEndorsementsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountEndorsementsResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountEndorsementsResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetAccountEndorsementsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountEndorsementsResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountFeaturedTagsResponse : OperationResponse<CollectionsList<FeaturedTag>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountFeaturedTagsResponseSuccess(
    override val body: CollectionsList<FeaturedTag>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFeaturedTagsResponse(),
      OperationResponseSuccess<CollectionsList<FeaturedTag>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFeaturedTagsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountFeaturedTagsResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFeaturedTagsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountFeaturedTagsResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFeaturedTagsResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetAccountFeaturedTagsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFeaturedTagsResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public data class PostAccountFollowRequest(
    public val languages: CollectionsList<String>? = null,
    public val notify: Boolean? = false,
    public val reblogs: Boolean? = true,
  )

  @Serializable
  public sealed class PostAccountFollowResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountFollowResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountFollowResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountFollowResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountFollowResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountFollowResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountFollowResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountFollowResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountFollowersResponse : OperationResponse<CollectionsList<Account>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountFollowersResponseSuccess(
    override val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowersResponse(),
      OperationResponseSuccess<CollectionsList<Account>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowersResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountFollowersResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowersResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountFollowersResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowersResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetAccountFollowersResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowersResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountFollowingResponse : OperationResponse<CollectionsList<Account>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountFollowingResponseSuccess(
    override val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowingResponse(),
      OperationResponseSuccess<CollectionsList<Account>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowingResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountFollowingResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowingResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountFollowingResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowingResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetAccountFollowingResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountFollowingResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountIdentityProofsResponse : OperationResponse<CollectionsList<IdentityProof>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountIdentityProofsResponseSuccess(
    override val body: CollectionsList<IdentityProof>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountIdentityProofsResponse(),
      OperationResponseSuccess<CollectionsList<IdentityProof>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountIdentityProofsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountIdentityProofsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountIdentityProofsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountIdentityProofsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountIdentityProofsResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountListsResponse : OperationResponse<CollectionsList<ModelList>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountListsResponseSuccess(
    override val body: CollectionsList<ModelList>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountListsResponse(),
      OperationResponseSuccess<CollectionsList<ModelList>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountListsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountListsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountListsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountListsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountListsResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public data class PostAccountMuteRequest(
    public val duration: Long? = 0,
    public val notifications: Boolean? = true,
  )

  @Serializable
  public sealed class PostAccountMuteResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountMuteResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountMuteResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountMuteResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountMuteResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountMuteResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountMuteResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountMuteResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public data class PostAccountNoteRequest(
    public val comment: String? = null,
  )

  @Serializable
  public sealed class PostAccountNoteResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountNoteResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountNoteResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountNoteResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountNoteResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountNoteResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountNoteResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountNoteResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostAccountPinResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountPinResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountPinResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountPinResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountPinResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountPinResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountPinResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountPinResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostAccountRemoveFromFollowersResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountRemoveFromFollowersResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountRemoveFromFollowersResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountRemoveFromFollowersResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountRemoveFromFollowersResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountRemoveFromFollowersResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountRemoveFromFollowersResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountRemoveFromFollowersResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountStatusesResponse : OperationResponse<CollectionsList<Status>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountStatusesResponseSuccess(
    override val body: CollectionsList<Status>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountStatusesResponse(),
      OperationResponseSuccess<CollectionsList<Status>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountStatusesResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountStatusesResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountStatusesResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountStatusesResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountStatusesResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetAccountStatusesResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountStatusesResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostAccountUnblockResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountUnblockResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnblockResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnblockResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountUnblockResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnblockResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountUnblockResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnblockResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostAccountUnendorseResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountUnendorseResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnendorseResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnendorseResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountUnendorseResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnendorseResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountUnendorseResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnendorseResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostAccountUnfollowResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountUnfollowResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnfollowResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnfollowResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountUnfollowResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnfollowResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountUnfollowResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnfollowResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostAccountUnmuteResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountUnmuteResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnmuteResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnmuteResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountUnmuteResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnmuteResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountUnmuteResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnmuteResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class PostAccountUnpinResponse : OperationResponse<Relationship> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PostAccountUnpinResponseSuccess(
    override val body: Relationship,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnpinResponse(),
      OperationResponseSuccess<Relationship> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnpinResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PostAccountUnpinResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnpinResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PostAccountUnpinResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostAccountUnpinResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountsFamiliarFollowersResponse : OperationResponse<CollectionsList<FamiliarFollowers>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountsFamiliarFollowersResponseSuccess(
    override val body: CollectionsList<FamiliarFollowers>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsFamiliarFollowersResponse(),
      OperationResponseSuccess<CollectionsList<FamiliarFollowers>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsFamiliarFollowersResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountsFamiliarFollowersResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsFamiliarFollowersResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountsFamiliarFollowersResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsFamiliarFollowersResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountLookupResponse : OperationResponse<Account> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountLookupResponseSuccess(
    override val body: Account,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountLookupResponse(),
      OperationResponseSuccess<Account> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountLookupResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountLookupResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountLookupResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountLookupResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountLookupResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetAccountLookupResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountLookupResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountRelationshipsResponse : OperationResponse<CollectionsList<Relationship>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountRelationshipsResponseSuccess(
    override val body: CollectionsList<Relationship>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountRelationshipsResponse(),
      OperationResponseSuccess<CollectionsList<Relationship>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountRelationshipsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountRelationshipsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountRelationshipsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountRelationshipsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountRelationshipsResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountSearchResponse : OperationResponse<CollectionsList<Account>> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountSearchResponseSuccess(
    override val body: CollectionsList<Account>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountSearchResponse(),
      OperationResponseSuccess<CollectionsList<Account>> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountSearchResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountSearchResponseFailure410(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountSearchResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountSearchResponseFailure(
    override val body: ValidationError,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountSearchResponse(),
      OperationResponseFailure<ValidationError>

  @Serializable
  public data class GetAccountSearchResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountSearchResponse(),
      OperationResponseUnknownFailure

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
  public sealed class PatchAccountsUpdateCredentialsResponse : OperationResponse<CredentialAccount> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PatchAccountsUpdateCredentialsResponseSuccess(
    override val body: CredentialAccount,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PatchAccountsUpdateCredentialsResponse(),
      OperationResponseSuccess<CredentialAccount> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PatchAccountsUpdateCredentialsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PatchAccountsUpdateCredentialsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PatchAccountsUpdateCredentialsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class PatchAccountsUpdateCredentialsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PatchAccountsUpdateCredentialsResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class GetAccountsVerifyCredentialsResponse : OperationResponse<CredentialAccount> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class GetAccountsVerifyCredentialsResponseSuccess(
    override val body: CredentialAccount,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsVerifyCredentialsResponse(),
      OperationResponseSuccess<CredentialAccount> {
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
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsVerifyCredentialsResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class GetAccountsVerifyCredentialsResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsVerifyCredentialsResponse(),
      OperationResponseFailure<Unit> {
    override val body: Unit
      get() = Unit
  }

  @Serializable
  public data class GetAccountsVerifyCredentialsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetAccountsVerifyCredentialsResponse(),
      OperationResponseUnknownFailure
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
