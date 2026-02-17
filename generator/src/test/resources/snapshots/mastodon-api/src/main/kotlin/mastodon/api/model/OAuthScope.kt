package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class OAuthScope {
  @SerialName("profile")
  PROFILE,
  @SerialName("read")
  READ,
  @SerialName("write")
  WRITE,
  @SerialName("push")
  PUSH,
  @SerialName("follow")
  FOLLOW,
  @SerialName("admin:read")
  ADMIN_READ,
  @SerialName("admin:write")
  ADMIN_WRITE,
  @SerialName("read:accounts")
  READ_ACCOUNTS,
  @SerialName("read:blocks")
  READ_BLOCKS,
  @SerialName("read:bookmarks")
  READ_BOOKMARKS,
  @SerialName("read:favourites")
  READ_FAVOURITES,
  @SerialName("read:filters")
  READ_FILTERS,
  @SerialName("read:follows")
  READ_FOLLOWS,
  @SerialName("read:lists")
  READ_LISTS,
  @SerialName("read:mutes")
  READ_MUTES,
  @SerialName("read:notifications")
  READ_NOTIFICATIONS,
  @SerialName("read:search")
  READ_SEARCH,
  @SerialName("read:statuses")
  READ_STATUSES,
  @SerialName("write:accounts")
  WRITE_ACCOUNTS,
  @SerialName("write:blocks")
  WRITE_BLOCKS,
  @SerialName("write:bookmarks")
  WRITE_BOOKMARKS,
  @SerialName("write:conversations")
  WRITE_CONVERSATIONS,
  @SerialName("write:favourites")
  WRITE_FAVOURITES,
  @SerialName("write:filters")
  WRITE_FILTERS,
  @SerialName("write:follows")
  WRITE_FOLLOWS,
  @SerialName("write:lists")
  WRITE_LISTS,
  @SerialName("write:media")
  WRITE_MEDIA,
  @SerialName("write:mutes")
  WRITE_MUTES,
  @SerialName("write:notifications")
  WRITE_NOTIFICATIONS,
  @SerialName("write:reports")
  WRITE_REPORTS,
  @SerialName("write:statuses")
  WRITE_STATUSES,
  @SerialName("admin:read:accounts")
  ADMIN_READ_ACCOUNTS,
  @SerialName("admin:read:canonical_email_blocks")
  ADMIN_READ_CANONICAL_EMAIL_BLOCKS,
  @SerialName("admin:read:domain_allows")
  ADMIN_READ_DOMAIN_ALLOWS,
  @SerialName("admin:read:domain_blocks")
  ADMIN_READ_DOMAIN_BLOCKS,
  @SerialName("admin:read:email_domain_blocks")
  ADMIN_READ_EMAIL_DOMAIN_BLOCKS,
  @SerialName("admin:read:ip_blocks")
  ADMIN_READ_IP_BLOCKS,
  @SerialName("admin:read:reports")
  ADMIN_READ_REPORTS,
  @SerialName("admin:write:accounts")
  ADMIN_WRITE_ACCOUNTS,
  @SerialName("admin:write:canonical_email_blocks")
  ADMIN_WRITE_CANONICAL_EMAIL_BLOCKS,
  @SerialName("admin:write:domain_allows")
  ADMIN_WRITE_DOMAIN_ALLOWS,
  @SerialName("admin:write:domain_blocks")
  ADMIN_WRITE_DOMAIN_BLOCKS,
  @SerialName("admin:write:email_domain_blocks")
  ADMIN_WRITE_EMAIL_DOMAIN_BLOCKS,
  @SerialName("admin:write:ip_blocks")
  ADMIN_WRITE_IP_BLOCKS,
  @SerialName("admin:write:reports")
  ADMIN_WRITE_REPORTS,
  ;

  public fun serialName(): String = OAuthScope.serializer().descriptor.getElementName(this.ordinal)
}
