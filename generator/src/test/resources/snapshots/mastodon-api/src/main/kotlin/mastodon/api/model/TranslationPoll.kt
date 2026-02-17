package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class TranslationPoll(
  public val id: String,
  public val options: List<TranslationPollOption>,
)
