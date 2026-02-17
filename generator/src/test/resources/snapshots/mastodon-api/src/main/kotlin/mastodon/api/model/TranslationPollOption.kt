package mastodon.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class TranslationPollOption(
  public val title: String,
)
