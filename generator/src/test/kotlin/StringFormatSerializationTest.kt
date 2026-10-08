package org.litote.openapi.ktor.client.generator

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import string.format.api.model.Event
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlin.uuid.Uuid

class StringFormatSerializationTest {
    private val json = Json

    private val event =
        Event(
            id = Uuid.parse("123e4567-e89b-12d3-a456-426614174000"),
            createdAt = Instant.parse("2026-10-09T15:23:39.886124921Z"),
            day = "2026-10-09",
            history = listOf(Instant.parse("2026-10-09T15:23:39.000000001Z")),
        )

    @Test
    fun `GIVEN an instant with nanoseconds WHEN encoding a model THEN it is truncated to milliseconds`() {
        val encoded = json.encodeToJsonElement(Event.serializer(), event).jsonObject

        assertEquals("2026-10-09T15:23:39.886Z", encoded.getValue("createdAt").jsonPrimitive.content)
        assertEquals("[\"2026-10-09T15:23:39Z\"]", encoded.getValue("history").toString())
    }

    @Test
    fun `GIVEN an instant with nanoseconds WHEN decoding a model THEN the full precision is kept`() {
        val decoded =
            json.decodeFromString(
                Event.serializer(),
                """{"id":"123e4567-e89b-12d3-a456-426614174000","createdAt":"2026-10-09T15:23:39.886124921Z","day":"2026-10-09"}""",
            )

        assertEquals(Instant.parse("2026-10-09T15:23:39.886124921Z"), decoded.createdAt)
    }
}
