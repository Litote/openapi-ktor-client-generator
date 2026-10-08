package org.example.model

import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.example.client.DateTimeFormatSerializer

@Serializable
public data class TestStatusResponse(
  public val checkedAt: @Serializable(with = DateTimeFormatSerializer::class) Instant? = null,
  public val day: LocalDate? = null,
  public val runId: Uuid? = null,
  public val status: TestStatusEnum = TestStatusEnum.UNKNOWN_,
)
