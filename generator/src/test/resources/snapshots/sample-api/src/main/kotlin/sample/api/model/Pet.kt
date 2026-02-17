package sample.api.model

import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public data class Pet(
  public val category: Category? = null,
  public val id: Long? = null,
  public val name: String,
  public val photoUrls: List<String>,
  public val status: Status? = null,
  public val tags: List<Tag>? = null,
) {
  @Serializable
  public enum class Status {
    @SerialName("available")
    AVAILABLE,
    @SerialName("pending")
    PENDING,
    @SerialName("sold")
    SOLD,
    ;

    public fun serialName(): String = Status.serializer().descriptor.getElementName(this.ordinal)
  }
}
