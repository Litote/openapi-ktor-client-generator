package `inline`.response.api.model

import kotlin.Long
import kotlinx.serialization.Serializable

@Serializable
public data class Itinerary(
  public val duration: Long,
)
