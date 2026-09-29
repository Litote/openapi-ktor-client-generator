package inheritance.api.client

import inheritance.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import inheritance.api.model.Status
import inheritance.api.model.StatusCreated
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import kotlin.Int
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

public interface Client {
  public suspend fun createStatus(request: Status): CreateStatusResponse

  @Serializable
  public sealed class CreateStatusResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateStatusResponseSuccess(
    public val body: StatusCreated,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateStatusResponse()

  @Serializable
  public data class CreateStatusResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateStatusResponse()
}

public fun Client(configuration: ClientConfiguration = defaultClientConfiguration): Client = DefaultClient(configuration)

public class DefaultClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : Client {
  override suspend fun createStatus(request: Status): Client.CreateStatusResponse {
    try {
      val response = configuration.client.post("status") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> Client.CreateStatusResponseSuccess(response.body<StatusCreated>(), response.headers)
        else -> Client.CreateStatusResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return Client.CreateStatusResponseUnknownFailure(500)
    }
  }
}
