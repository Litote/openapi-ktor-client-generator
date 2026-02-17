package mastodon.api.model

import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AsyncRefresh(
  public val id: String,
  @SerialName("result_count")
  public val resultCount: Long? = null,
  public val status: AsyncRefreshStatusEnum,
)
