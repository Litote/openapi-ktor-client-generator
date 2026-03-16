package org.litote.openapi.ktor.client.generator

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import sample.api.model.Vehicle
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class PayloadTest {
    @Test
    fun `GIVEN vehicule payload WHEN deserializing THEN output matches snapshot`() {
        val json = Json { ignoreUnknownKeys = true }
        val inputStream =
            PayloadTest::class.java.getResourceAsStream("/payloads/sample-vehicule.json")
                ?: error("Resource /payloads/sample-vehicule.json not found")
        val originalContent = inputStream.bufferedReader().readText()
        val result =
            json.decodeFromString<List<Vehicle>>(originalContent)
        val serialized = json.encodeToString(result)
        assertEquals(
            json.parseToJsonElement(originalContent),
            json.parseToJsonElement(serialized),
            "serialization has to be equal to initial content",
        )
    }
}
