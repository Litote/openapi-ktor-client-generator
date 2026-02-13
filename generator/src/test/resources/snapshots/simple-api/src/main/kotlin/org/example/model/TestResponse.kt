package org.example.model

import kotlin.Long
import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class TestResponse(
  public val id: Long,
  public val name: String,
)
