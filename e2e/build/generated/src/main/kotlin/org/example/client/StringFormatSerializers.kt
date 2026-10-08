package org.example.client

import kotlin.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializes OpenAPI `date-time` strings as `kotlin.time.Instant`.
 */
public object DateTimeFormatSerializer : KSerializer<Instant> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("org.example.client.DateTimeFormatSerializer", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, `value`: Instant) {
    encoder.encodeString(value.let { kotlin.time.Instant.fromEpochSeconds(it.epochSeconds, it.nanosecondsOfSecond / 1_000_000 * 1_000_000) }.toString())
  }

  override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}
