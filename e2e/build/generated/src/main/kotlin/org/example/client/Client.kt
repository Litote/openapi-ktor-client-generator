package org.example.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.cookie
import io.ktor.http.Headers
import io.ktor.http.ParametersBuilder
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.example.client.ClientConfiguration.Companion.defaultClientConfiguration
import org.example.model.TestRange
import org.example.model.TestStatusEnum
import org.example.model.TestStatusResponse
import io.ktor.client.request.`header` as setHeader

public class Client(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get test status
   */
  public suspend fun getTestStatus(
    runId: Uuid? = null,
    day: LocalDate? = LocalDate.parse("2024-01-31"),
    runIds: List<Uuid>? = null,
    statuses: List<Statuses>? = null,
    filter: Filter? = null,
    range: TestRange? = null,
    compactRange: TestRange? = null,
    states: List<TestStatusEnum>? = null,
    xTraceIds: List<String>? = null,
    session: String,
  ): GetTestStatusResponse {
    try {
      val response = configuration.client.`get`("test-status") {
        if (xTraceIds != null) {
          setHeader("X-Trace-Ids", xTraceIds.joinToString(","))
        }
        cookie("session", session)
        url {
          if (runId != null) {
            parameters.append("runId", runId.toString())
          }
          if (day != null) {
            parameters.append("day", day.toString())
          }
          if (runIds != null) {
            parameters.appendAll("runIds", runIds.map { it.toString() })
          }
          if (statuses != null) {
            parameters.append("statuses", statuses.joinToString("|") { it.serialName() })
          }
          if (filter != null) {
            parameters.appendDeepObject("filter", configuration.json.encodeToJsonElement(filter))
          }
          if (range != null) {
            parameters.appendExplodedObject(configuration.json.encodeToJsonElement(range).jsonObject)
          }
          if (compactRange != null) {
            parameters.append("compactRange", configuration.json.encodeToJsonElement(compactRange).jsonObject.toDelimitedString(","))
          }
          if (states != null) {
            parameters.appendAll("states", states.map { it.serialName() })
          }
        }
      }
      return when (response.status.value) {
        200 -> GetTestStatusResponseSuccess(response.body<TestStatusResponse>(), response.headers)
        else -> GetTestStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTestStatusResponseUnknownFailure(500)
    }
  }

  private fun ParametersBuilder.appendExplodedObject(`value`: JsonObject) {
    value.forEach { (key, element) -> appendAll(key, element.toParameterValues()) }
  }

  private fun ParametersBuilder.appendDeepObject(name: String, `value`: JsonElement) {
    when (value) {
      is JsonObject -> value.forEach { (key, element) -> appendDeepObject("$name[$key]", element) }
      is JsonArray -> value.forEach { appendDeepObject(name, it) }
      else -> value.toParameterValues().forEach { append(name, it) }
    }
  }

  private fun JsonObject.toDelimitedString(separator: String, keyValueSeparator: String = separator): String = entries.filter { it.value !is JsonNull }.joinToString(separator) { (key, element) -> key + keyValueSeparator + element.toParameterValues().joinToString(",") }

  private fun JsonElement.toParameterValues(): List<String> = when (this) {
    is JsonNull -> emptyList()
    is JsonPrimitive -> listOf(content)
    is JsonArray -> flatMap { it.toParameterValues() }
    is JsonObject -> listOf(toString())
  }

  @Serializable
  public enum class Statuses {
    @SerialName("PENDING")
    PENDING,
    @SerialName("RUNNING")
    RUNNING,
    ;

    public fun serialName(): String = Statuses.serializer().descriptor.getElementName(this.ordinal)
  }

  @Serializable
  public data class Filter(
    public val minId: Long? = null,
    public val name: String? = null,
  )

  @Serializable
  public sealed class GetTestStatusResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetTestStatusResponseSuccess(
    public val body: TestStatusResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTestStatusResponse()

  @Serializable
  public data class GetTestStatusResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetTestStatusResponse()
}
