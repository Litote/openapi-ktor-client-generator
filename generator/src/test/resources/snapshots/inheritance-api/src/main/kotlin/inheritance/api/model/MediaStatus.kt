package inheritance.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
@SerialName("media")
public data class MediaStatus(
  public val mediaUrl: String,
  public val type: Type,
) : Status() {
  @Serializable
  public enum class Type {
    @SerialName("media")
    MEDIA,
    ;

    public fun serialName(): String = Type.serializer().descriptor.getElementName(this.ordinal)
  }
}
