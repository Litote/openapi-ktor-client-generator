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
  ADMINREAD,
  @SerialName("admin:write")
  ADMINWRITE,
  @SerialName("read:accounts")
  READACCOUNTS,
  @SerialName("read:blocks")
  READBLOCKS,
  @SerialName("read:bookmarks")
  READBOOKMARKS,
  @SerialName("read:favourites")
  READFAVOURITES,
  @SerialName("read:filters")
  READFILTERS,
  @SerialName("read:follows")
  READFOLLOWS,
  @SerialName("read:lists")
  READLISTS,
  @SerialName("read:mutes")
  READMUTES,
  @SerialName("read:notifications")
  READNOTIFICATIONS,
  @SerialName("read:search")
  READSEARCH,
  @SerialName("read:statuses")
  READSTATUSES,
  @SerialName("write:accounts")
  WRITEACCOUNTS,
  @SerialName("write:blocks")
  WRITEBLOCKS,
  @SerialName("write:bookmarks")
  WRITEBOOKMARKS,
  @SerialName("write:conversations")
  WRITECONVERSATIONS,
  @SerialName("write:favourites")
  WRITEFAVOURITES,
  @SerialName("write:filters")
  WRITEFILTERS,
  @SerialName("write:follows")
  WRITEFOLLOWS,
  @SerialName("write:lists")
  WRITELISTS,
  @SerialName("write:media")
  WRITEMEDIA,
  @SerialName("write:mutes")
  WRITEMUTES,
  @SerialName("write:notifications")
  WRITENOTIFICATIONS,
  @SerialName("write:reports")
  WRITEREPORTS,
  @SerialName("write:statuses")
  WRITESTATUSES,
  @SerialName("admin:read:accounts")
  ADMINREADACCOUNTS,
  @SerialName("admin:read:canonical_email_blocks")
  ADMINREADCANONICALEMAILBLOCKS,
  @SerialName("admin:read:domain_allows")
  ADMINREADDOMAINALLOWS,
  @SerialName("admin:read:domain_blocks")
  ADMINREADDOMAINBLOCKS,
  @SerialName("admin:read:email_domain_blocks")
  ADMINREADEMAILDOMAINBLOCKS,
  @SerialName("admin:read:ip_blocks")
  ADMINREADIPBLOCKS,
  @SerialName("admin:read:reports")
  ADMINREADREPORTS,
  @SerialName("admin:write:accounts")
  ADMINWRITEACCOUNTS,
  @SerialName("admin:write:canonical_email_blocks")
  ADMINWRITECANONICALEMAILBLOCKS,
  @SerialName("admin:write:domain_allows")
  ADMINWRITEDOMAINALLOWS,
  @SerialName("admin:write:domain_blocks")
  ADMINWRITEDOMAINBLOCKS,
  @SerialName("admin:write:email_domain_blocks")
  ADMINWRITEEMAILDOMAINBLOCKS,
  @SerialName("admin:write:ip_blocks")
  ADMINWRITEIPBLOCKS,
  @SerialName("admin:write:reports")
  ADMINWRITEREPORTS,
  ;

  public fun serialName(): String = OAuthScope.serializer().descriptor.getElementName(this.ordinal)
}
