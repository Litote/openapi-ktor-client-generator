package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class StatusVisibilityEnum {
  @SerialName("public")
  PUBLIC,
  @SerialName("unlisted")
  UNLISTED,
  @SerialName("private")
  PRIVATE,
  @SerialName("direct")
  DIRECT,
  ;

  public fun serialName(): String = StatusVisibilityEnum.serializer().descriptor.getElementName(this.ordinal)
}
