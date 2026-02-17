package mastodon.api.model

import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class V1Filter(
  public val context: List<FilterContextEnum>,
  @SerialName("expires_at")
  public val expiresAt: String? = null,
  public val id: String,
  public val irreversible: Boolean,
  public val phrase: String,
  @SerialName("whole_word")
  public val wholeWord: Boolean,
)
