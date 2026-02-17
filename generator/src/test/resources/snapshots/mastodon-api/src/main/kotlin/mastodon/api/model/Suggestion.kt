package mastodon.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public data class Suggestion(
  public val account: Account,
  public val sources: List<Sources>? = null,
) {
  @Serializable
  public enum class Sources {
    @SerialName("featured")
    FEATURED,
    @SerialName("most_followed")
    MOST_FOLLOWED,
    @SerialName("most_interactions")
    MOST_INTERACTIONS,
    @SerialName("similar_to_recently_followed")
    SIMILAR_TO_RECENTLY_FOLLOWED,
    @SerialName("friends_of_friends")
    FRIENDS_OF_FRIENDS,
    ;

    public fun serialName(): String = Sources.serializer().descriptor.getElementName(this.ordinal)
  }
}
