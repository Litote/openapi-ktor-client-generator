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
    MOSTFOLLOWED,
    @SerialName("most_interactions")
    MOSTINTERACTIONS,
    @SerialName("similar_to_recently_followed")
    SIMILARTORECENTLYFOLLOWED,
    @SerialName("friends_of_friends")
    FRIENDSOFFRIENDS,
    ;

    public fun serialName(): String = Sources.serializer().descriptor.getElementName(this.ordinal)
  }
}
