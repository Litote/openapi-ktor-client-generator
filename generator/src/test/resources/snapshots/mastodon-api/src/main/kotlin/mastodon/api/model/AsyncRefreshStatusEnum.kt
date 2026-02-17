package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class AsyncRefreshStatusEnum {
  @SerialName("running")
  RUNNING,
  @SerialName("finished")
  FINISHED,
  ;

  public fun serialName(): String = AsyncRefreshStatusEnum.serializer().descriptor.getElementName(this.ordinal)
}
