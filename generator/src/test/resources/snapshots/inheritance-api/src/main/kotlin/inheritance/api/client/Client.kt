package inheritance.api.client

import inheritance.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import inheritance.api.model.Status
import inheritance.api.model.StatusCreated
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlinx.serialization.Serializable

public class Client(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  public suspend fun createStatus(request: Status): CreateStatusResponse {
    try {
      val response = configuration.client.post("status") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateStatusResponseSuccess(response.body<StatusCreated>())
        else -> CreateStatusResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateStatusResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class CreateStatusResponse

  @Serializable
  public data class CreateStatusResponseSuccess(
    public val body: StatusCreated,
  ) : CreateStatusResponse()

  @Serializable
  public data class CreateStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateStatusResponse()
}
