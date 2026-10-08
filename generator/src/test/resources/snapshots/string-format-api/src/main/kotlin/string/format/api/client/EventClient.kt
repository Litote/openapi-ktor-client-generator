package string.format.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.Parameters
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import string.format.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import string.format.api.model.Event

public interface EventClient {
  public suspend fun getEvent(
    eventId: Uuid,
    since: Instant? = null,
    slots: List<Instant>? = null,
    day: String? = "2024-01-31",
    tenantId: Uuid? = ClientConfiguration.PARAMETER_TENANTID_DEFAULT_VALUE,
  ): GetEventResponse

  public suspend fun createEvent(form: CreateEventForm): CreateEventResponse

  @Serializable
  public sealed class GetEventResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetEventResponseSuccess(
    public val body: Event,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetEventResponse()

  @Serializable
  public data class GetEventResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetEventResponse()

  public data class CreateEventForm(
    public val at: Instant,
    public val until: Instant? = null,
  )

  @Serializable
  public sealed class CreateEventResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateEventResponseSuccess(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateEventResponse()

  @Serializable
  public data class CreateEventResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateEventResponse()
}

public fun EventClient(configuration: ClientConfiguration = defaultClientConfiguration): EventClient = DefaultEventClient(configuration)

public class DefaultEventClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : EventClient {
  override suspend fun getEvent(
    eventId: Uuid,
    since: Instant?,
    slots: List<Instant>?,
    day: String?,
    tenantId: Uuid?,
  ): EventClient.GetEventResponse {
    try {
      val response = configuration.client.`get`("events/{eventId}".replace("/{eventId}", "/${eventId.toString().encodeURLPathPart()}")) {
        url {
          if (since != null) {
            parameters.append("since", since.let { kotlin.time.Instant.fromEpochSeconds(it.epochSeconds, it.nanosecondsOfSecond / 1_000_000 * 1_000_000) }.toString())
          }
          if (slots != null) {
            parameters.appendAll("slots", slots.map { value -> value.let { kotlin.time.Instant.fromEpochSeconds(it.epochSeconds, it.nanosecondsOfSecond / 1_000_000 * 1_000_000) }.toString() })
          }
          if (day != null) {
            parameters.append("day", day)
          }
          if (tenantId != null) {
            parameters.append("tenantId", tenantId.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> EventClient.GetEventResponseSuccess(response.body<Event>(), response.headers)
        else -> EventClient.GetEventResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return EventClient.GetEventResponseUnknownFailure(500)
    }
  }

  override suspend fun createEvent(form: EventClient.CreateEventForm): EventClient.CreateEventResponse {
    try {
      val response = configuration.client.post("events") {
        setBody(FormDataContent(Parameters.build {
        append("at", form.at.let { kotlin.time.Instant.fromEpochSeconds(it.epochSeconds, it.nanosecondsOfSecond / 1_000_000 * 1_000_000) }.toString())
        form.until?.let { value ->
          append("until", value.let { kotlin.time.Instant.fromEpochSeconds(it.epochSeconds, it.nanosecondsOfSecond / 1_000_000 * 1_000_000) }.toString())
        }
        }))
      }
      return when (response.status.value) {
        204 -> EventClient.CreateEventResponseSuccess(response.headers)
        else -> EventClient.CreateEventResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return EventClient.CreateEventResponseUnknownFailure(500)
    }
  }
}
