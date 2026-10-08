package string.format.api.model

import kotlin.String
import kotlin.collections.List
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import string.format.api.client.DateTimeFormatSerializer

@Serializable
public data class Event(
  public val attachment: String? = null,
  public val contact: String? = null,
  public val createdAt: @Serializable(with = DateTimeFormatSerializer::class) Instant,
  public val day: String,
  public val history: List<@Serializable(with = DateTimeFormatSerializer::class) Instant>? = null,
  public val id: Uuid,
  public val relatedIds: List<Uuid>? = null,
  public val startDay: String? = "2024-01-01",
  public val updatedAt: @Serializable(with = DateTimeFormatSerializer::class) Instant? = null,
)
