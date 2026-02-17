package mastodon.api.model

import kotlin.Long
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OEmbedResponse(
  @SerialName("author_name")
  public val authorName: String,
  @SerialName("author_url")
  public val authorUrl: String,
  @SerialName("cache_age")
  public val cacheAge: Long,
  public val height: String? = null,
  public val html: String,
  @SerialName("provider_name")
  public val providerName: String,
  @SerialName("provider_url")
  public val providerUrl: String,
  public val title: String,
  public val type: String,
  public val version: String,
  public val width: Long,
)
