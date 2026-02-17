package mastodon.api.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class ReportCategoryEnum {
  @SerialName("spam")
  SPAM,
  @SerialName("legal")
  LEGAL,
  @SerialName("violation")
  VIOLATION,
  @SerialName("other")
  OTHER,
  ;

  public fun serialName(): String = ReportCategoryEnum.serializer().descriptor.getElementName(this.ordinal)
}
