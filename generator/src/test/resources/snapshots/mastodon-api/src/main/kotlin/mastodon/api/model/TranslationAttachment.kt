package mastodon.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class TranslationAttachment(
  public val description: String,
  public val id: String,
)
