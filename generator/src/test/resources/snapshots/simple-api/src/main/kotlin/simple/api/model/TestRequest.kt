package simple.api.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class TestRequest(
  public val name: String,
)
