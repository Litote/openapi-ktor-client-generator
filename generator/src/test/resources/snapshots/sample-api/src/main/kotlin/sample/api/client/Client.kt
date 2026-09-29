package sample.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import kotlin.Int
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import sample.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import sample.api.model.Vehicle

public class Client(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Get all vehicles
   */
  public suspend fun getVehicles(): GetVehiclesResponse {
    try {
      val response = configuration.client.`get`("vehicles") {
      }
      return when (response.status.value) {
        200 -> GetVehiclesResponseSuccess(response.body<List<Vehicle>>(), response.headers)
        else -> GetVehiclesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetVehiclesResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetVehiclesResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetVehiclesResponseSuccess(
    public val body: List<Vehicle>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetVehiclesResponse()

  @Serializable
  public data class GetVehiclesResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetVehiclesResponse()
}
