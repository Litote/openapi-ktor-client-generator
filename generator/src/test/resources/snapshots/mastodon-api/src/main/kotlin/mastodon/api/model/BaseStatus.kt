package mastodon.api.model

import kotlin.Boolean
import kotlin.String

public interface BaseStatus {
  public val inReplyToId: String?

  public val language: String?

  public val quoteApprovalPolicy: String?

  public val quotedStatusId: String?

  public val scheduledAt: String?

  public val sensitive: Boolean?

  public val spoilerText: String?

  public val visibility: StatusVisibilityEnum?
}
