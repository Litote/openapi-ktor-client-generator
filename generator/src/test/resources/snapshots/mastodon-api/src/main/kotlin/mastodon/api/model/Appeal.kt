package mastodon.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class Appeal(
  public val state: AppealStateEnum,
  public val text: String,
)
