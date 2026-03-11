@file:OptIn(ExperimentalSerializationApi::class)

package mastodon.api.model

import kotlin.OptIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
public sealed class CreateStatusRequest
