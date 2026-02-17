package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class FilterKeyword(
  public val id: String,
  public val keyword: String,
  @SerialName("whole_word")
  public val wholeWord: Boolean,
)
