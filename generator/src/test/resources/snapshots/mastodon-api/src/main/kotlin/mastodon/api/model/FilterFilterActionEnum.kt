package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class FilterFilterActionEnum {
  @SerialName("warn")
  WARN,
  @SerialName("hide")
  HIDE,
  @SerialName("blur")
  BLUR,
  ;

  public fun serialName(): String = FilterFilterActionEnum.serializer().descriptor.getElementName(this.ordinal)
}
