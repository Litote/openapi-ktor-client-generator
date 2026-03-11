package inheritance.api.model

import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
@SerialName("poll")
public data class PollStatus(
  public val pollOptions: List<String>,
  public val type: Type,
) : Status() {
  @Serializable
  public enum class Type {
    @SerialName("poll")
    POLL,
    ;

    public fun serialName(): String = Type.serializer().descriptor.getElementName(this.ordinal)
  }
}
