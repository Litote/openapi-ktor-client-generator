package org.example.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Int
import kotlinx.serialization.Serializable
import org.example.client.ClientConfiguration.Companion.defaultClientConfiguration
import org.example.model.TestStatusResponse

public class Client(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get test status
   */
  public suspend fun getTestStatus(): GetTestStatusResponse {
    try {
      val response = configuration.client.`get`("test-status") {
      }
      return when (response.status.value) {
        200 -> GetTestStatusResponseSuccess(response.body<TestStatusResponse>())
        else -> GetTestStatusResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetTestStatusResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetTestStatusResponse

  @Serializable
  public data class GetTestStatusResponseSuccess(
    public val body: TestStatusResponse,
  ) : GetTestStatusResponse()

  @Serializable
  public data class GetTestStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetTestStatusResponse()
}
