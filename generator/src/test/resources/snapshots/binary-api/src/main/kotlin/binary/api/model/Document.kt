package binary.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class Document(
  public val id: String? = null,
  public val title: String? = null,
)
