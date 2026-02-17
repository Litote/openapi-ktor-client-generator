package sample.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import kotlin.Int
import kotlin.collections.List
import kotlinx.serialization.Serializable
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
        200 -> GetVehiclesResponseSuccess(response.body<List<Vehicle>>())
        else -> GetVehiclesResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetVehiclesResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class GetVehiclesResponse

  @Serializable
  public data class GetVehiclesResponseSuccess(
    public val body: List<Vehicle>,
  ) : GetVehiclesResponse()

  @Serializable
  public data class GetVehiclesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetVehiclesResponse()
}
