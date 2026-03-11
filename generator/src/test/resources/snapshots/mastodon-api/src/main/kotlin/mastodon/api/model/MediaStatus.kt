package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MediaStatus(
  @SerialName("media_ids")
  public val mediaIds: List<String>,
  public val status: String? = null,
) : CreateStatusRequest()
