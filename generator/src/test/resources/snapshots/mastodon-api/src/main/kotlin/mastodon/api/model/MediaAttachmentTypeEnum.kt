package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class MediaAttachmentTypeEnum {
  @SerialName("unknown")
  UNKNOWN,
  @SerialName("image")
  IMAGE,
  @SerialName("gifv")
  GIFV,
  @SerialName("video")
  VIDEO,
  @SerialName("audio")
  AUDIO,
  ;

  public fun serialName(): String = MediaAttachmentTypeEnum.serializer().descriptor.getElementName(this.ordinal)
}
