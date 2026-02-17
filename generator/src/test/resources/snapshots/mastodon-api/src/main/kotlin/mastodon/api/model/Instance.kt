package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class Instance(
  @SerialName("api_versions")
  public val apiVersions: JsonElement? = null,
  public val configuration: JsonElement,
  public val contact: JsonElement,
  public val description: String,
  public val domain: String,
  public val icon: List<InstanceIcon>? = null,
  public val languages: List<String>,
  public val registrations: JsonElement,
  public val rules: List<Rule>,
  @SerialName("source_url")
  public val sourceUrl: String,
  public val thumbnail: JsonElement,
  public val title: String,
  public val usage: JsonElement,
  public val version: String,
)
