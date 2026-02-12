package org.example.model

import kotlin.String
import kotlinx.serialization.Serializable

@Serializable
public data class TestRequest(
  public val name: String,
)
