package org.example.model

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public enum class TestStatusEnum {
  @SerialName("PENDING")
  PENDING,
  @SerialName("RUNNING")
  RUNNING,
  @SerialName("SUCCESS")
  SUCCESS,
  @SerialName("FAILED")
  FAILED,
  UNKNOWN_,
  ;

  public fun serialName(): String = TestStatusEnum.serializer().descriptor.getElementName(this.ordinal)
}
