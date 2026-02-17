package mastodon.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class DomainBlock(
  public val comment: String? = null,
  public val digest: String,
  public val domain: String,
  public val severity: DomainBlockSeverityEnum,
)
