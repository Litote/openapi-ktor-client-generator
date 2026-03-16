package org.litote.openapi.ktor.client.generator.adapter.renderer

import org.litote.openapi.ktor.client.generator.domain.GeneratedFileSpec
import org.litote.openapi.ktor.client.generator.port.ApiFileSystemWriter

internal class YamlContentConverterGenerator(
    private val clientPackage: String,
    private val outputDirectory: String,
    private val fileSystemWriter: ApiFileSystemWriter,
) {
    fun render() {
        fileSystemWriter.write(buildFile(), outputDirectory)
    }

    private fun buildFile(): GeneratedFileSpec =
        GeneratedFileSpec(
            packageName = clientPackage,
            fileName = "YamlContentConverter",
            content = buildContent(),
        )

    private fun buildContent(): String =
        """
package $clientPackage

import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.ContentConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml as SnakeYaml
import java.nio.charset.Charset

internal class YamlContentConverter(private val json: Json) : ContentConverter {
    private val snakeYaml: SnakeYaml = run {
        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        }
        SnakeYaml(options)
    }

    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Any? {
        val kotlinType = typeInfo.kotlinType ?: return null
        val bytes = content.readRemaining().readByteArray()
        val yamlText = bytes.toString(charset)
        val raw = snakeYaml.load<Any>(yamlText) ?: return null
        val jsonElement = anyToJsonElement(raw)
        val serializer = json.serializersModule.serializer(kotlinType)
        return json.decodeFromJsonElement(serializer, jsonElement)
    }

    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?,
    ): OutgoingContent? {
        if (value == null) return null
        val kotlinType = typeInfo.kotlinType ?: return null
        @Suppress("UNCHECKED_CAST")
        val serializer = json.serializersModule.serializer(kotlinType) as SerializationStrategy<Any>
        val jsonString = json.encodeToString(serializer, value)
        val raw = snakeYaml.load<Any>(jsonString)
        val yamlText = snakeYaml.dump(raw)
        return ByteArrayContent(yamlText.toByteArray(charset), contentType)
    }

    private fun anyToJsonElement(obj: Any?): JsonElement =
        when (obj) {
            null -> JsonNull
            is Map<*, *> -> JsonObject(obj.entries.associate { (k, v) -> k.toString() to anyToJsonElement(v) })
            is List<*> -> JsonArray(obj.map { anyToJsonElement(it) })
            is Boolean -> JsonPrimitive(obj)
            is Number -> JsonPrimitive(obj)
            is String -> JsonPrimitive(obj)
            else -> JsonPrimitive(obj.toString())
        }
}
        """.trimIndent() + "\n"
}
