package org.example.client

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlinx.serialization.Serializable
import org.example.client.ClientConfiguration.Companion.defaultClientConfiguration
import org.example.model.TestRequest
import org.example.model.TestResponse

public class Client(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Create a test
   */
  public suspend fun postTest(request: TestRequest): PostTestResponse {
    try {
      val response = configuration.client.post("test") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        201 -> PostTestResponseSuccess(response.body<TestResponse>())
        else -> PostTestResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostTestResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class PostTestResponse

  @Serializable
  public data class PostTestResponseSuccess(
    public val body: TestResponse,
  ) : PostTestResponse()

  @Serializable
  public data class PostTestResponseUnknownFailure(
    public val statusCode: Int,
  ) : PostTestResponse()
}
