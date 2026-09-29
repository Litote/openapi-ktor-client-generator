package simple.api.client

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlin.Int
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import simple.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import simple.api.model.TestRequest
import simple.api.model.TestResponse

public class Client(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Create a test
   */
  public suspend fun postTestWithTestId(
    request: TestRequest,
    testId: String,
    skip: Int? = null,
  ): PostTestWithTestIdResponse {
    try {
      val response = configuration.client.post("test/{testId}".replace("/{testId}", "/${testId.encodeURLPathPart()}")) {
        url {
          if (skip != null) {
            parameters.append("skip", skip.toString())
          }
        }
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        201 -> PostTestWithTestIdResponseSuccess(response.body<TestResponse>(), response.headers)
        else -> PostTestWithTestIdResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PostTestWithTestIdResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class PostTestWithTestIdResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class PostTestWithTestIdResponseSuccess(
    public val body: TestResponse,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTestWithTestIdResponse()

  @Serializable
  public data class PostTestWithTestIdResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PostTestWithTestIdResponse()
}
