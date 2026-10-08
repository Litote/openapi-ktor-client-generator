package `inline`.response.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class Error(
  public val message: String? = null,
)
