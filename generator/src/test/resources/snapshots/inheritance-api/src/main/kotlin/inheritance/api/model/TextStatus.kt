package inheritance.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
@SerialName("text")
public data class TextStatus(
  public val text: String,
  public val type: Type,
) : Status() {
  @Serializable
  public enum class Type {
    @SerialName("text")
    TEXT,
    ;

    public fun serialName(): String = Type.serializer().descriptor.getElementName(this.ordinal)
  }
}
