package org.example.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import kotlin.Int
import kotlin.coroutines.cancellation.CancellationException
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.example.client.ClientConfiguration.Companion.defaultClientConfiguration
import org.example.model.TestStatusResponse

public class Client(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get test status
   */
  public suspend fun getTestStatus(runId: Uuid? = null, day: LocalDate? = LocalDate.parse("2024-01-31")): GetTestStatusResponse {
    try {
      val response = configuration.client.`get`("test-status") {
        url {
          if (runId != null) {
            parameters.append("runId", runId.toString())
          }
          if (day != null) {
            parameters.append("day", day.toString())
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
