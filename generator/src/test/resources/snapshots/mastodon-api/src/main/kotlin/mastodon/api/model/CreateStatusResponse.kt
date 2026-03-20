@file:OptIn(ExperimentalSerializationApi::class)

package mastodon.api.model

import kotlin.OptIn
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable(with = CreateStatusResponse.Companion::class)
public sealed class CreateStatusResponse {
  public companion object : JsonContentPolymorphicSerializer<CreateStatusResponse>(CreateStatusResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CreateStatusResponse> {
      val keys = (element as? JsonObject)?.keys ?: emptySet()
      return when {
        listOf("account", "content", "created_at", "emojis", "favourites_count", "id", "media_attachments", "mentions", "reblogs_count", "replies_count", "sensitive", "spoiler_text", "tags", "uri", "visibility").all { it in keys } -> Status.serializer()
        else -> ScheduledStatus.serializer()
      }
    }
  }
}
