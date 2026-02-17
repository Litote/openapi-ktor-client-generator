package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class Translation(
  public val content: String,
  @SerialName("detected_source_language")
  public val detectedSourceLanguage: String,
  public val language: String,
  @SerialName("media_attachments")
  public val mediaAttachments: List<TranslationAttachment>,
  public val poll: JsonElement? = null,
  public val provider: String,
  @SerialName("spoiler_text")
  public val spoilerText: String,
)
