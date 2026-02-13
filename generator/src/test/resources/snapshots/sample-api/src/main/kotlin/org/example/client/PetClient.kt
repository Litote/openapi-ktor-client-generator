package org.example.client

import Status
import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlin.collections.List
import kotlinx.serialization.Serializable
import org.example.client.ClientConfiguration.Companion.defaultClientConfiguration
import org.example.model.Pet

public class PetClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Finds Pets by status
   */
  public suspend fun findPetsByStatus(status: List<Status>): FindPetsByStatusResponse {
    try {
      val response = configuration.client.`get`("pet/findByStatus/MultipleExamples") {
        url {
          parameters.append("status", status.toString())
        }
      }
      return when (response.status.value) {
        200 -> FindPetsByStatusResponseSuccess(response.body<List<Pet>>())
        400 -> FindPetsByStatusResponseFailure
        else -> FindPetsByStatusResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FindPetsByStatusResponseUnknownFailure(500)
    }
  }

  /**
   * Finds Pets by status
   */
  public suspend fun findPetsByStatus(status: List<Status>): FindPetsByStatusResponse {
    try {
      val response = configuration.client.`get`("pet/findByStatus/singleExample") {
        url {
          parameters.append("status", status.toString())
        }
      }
      return when (response.status.value) {
        200 -> FindPetsByStatusResponseSuccess(response.body<List<Pet>>())
        400 -> FindPetsByStatusResponseFailure
        else -> FindPetsByStatusResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FindPetsByStatusResponseUnknownFailure(500)
    }
  }

  /**
   * Add a new pet to the store
   */
  public suspend fun addPet(request: Pet): AddPetResponse {
    try {
      val response = configuration.client.post("pet") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        405 -> AddPetResponseFailure
        else -> AddPetResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return AddPetResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class FindPetsByStatusResponse

  @Serializable
  public data class FindPetsByStatusResponseSuccess(
    public val body: List<Pet>,
  ) : FindPetsByStatusResponse()

  @Serializable
  public object FindPetsByStatusResponseFailure : FindPetsByStatusResponse()

  @Serializable
  public data class FindPetsByStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : FindPetsByStatusResponse()

  @Serializable
  public sealed class FindPetsByStatusResponse

  @Serializable
  public data class FindPetsByStatusResponseSuccess(
    public val body: List<Pet>,
  ) : FindPetsByStatusResponse()

  @Serializable
  public object FindPetsByStatusResponseFailure : FindPetsByStatusResponse()

  @Serializable
  public data class FindPetsByStatusResponseUnknownFailure(
    public val statusCode: Int,
  ) : FindPetsByStatusResponse()

  @Serializable
  public sealed class AddPetResponse

  @Serializable
  public object AddPetResponseFailure : AddPetResponse()

  @Serializable
  public data class AddPetResponseUnknownFailure(
    public val statusCode: Int,
  ) : AddPetResponse()
}
