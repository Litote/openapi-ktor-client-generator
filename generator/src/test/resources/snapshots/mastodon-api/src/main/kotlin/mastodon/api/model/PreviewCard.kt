package mastodon.api.model

import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PreviewCard(
  @SerialName("author_name")
  public val authorName: String? = null,
  @SerialName("author_url")
  public val authorUrl: String? = null,
  public val authors: List<PreviewCardAuthor>? = null,
  public val blurhash: String? = null,
  public val description: String,
  @SerialName("embed_url")
  public val embedUrl: String,
  public val height: Long,
  public val html: String,
  public val image: String? = null,
  @SerialName("provider_name")
  public val providerName: String,
  @SerialName("provider_url")
  public val providerUrl: String,
  public val title: String,
  public val type: TrendsLinkTypeEnum,
  public val url: String,
  public val width: Long,
)
