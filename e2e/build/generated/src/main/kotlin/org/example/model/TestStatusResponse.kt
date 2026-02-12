package org.example.model

import kotlinx.serialization.Serializable

@Serializable
public data class TestStatusResponse(
  public val status: TestStatusEnum = TestStatusEnum.UNKNOWN_,
)
