package mastodon.api.model

import kotlin.Boolean
import kotlinx.serialization.Serializable

@Serializable
public data class MergedResponse(
  public val merged: Boolean,
)
