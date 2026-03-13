package yaml.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class Document(
  public val content: String? = null,
  public val id: String? = null,
  public val title: String? = null,
)
