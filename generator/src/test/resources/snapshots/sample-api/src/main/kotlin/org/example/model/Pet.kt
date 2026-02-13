package org.example.model

import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.Serializable

@Serializable
public data class Pet(
  public val category: Category? = null,
  public val id: Long? = null,
  public val name: String,
  public val photoUrls: List<String>,
  public val status: Status? = null,
  public val tags: List<Tag>? = null,
) {
  public enum class Status {
    available,
    pending,
    sold,
  }
}
