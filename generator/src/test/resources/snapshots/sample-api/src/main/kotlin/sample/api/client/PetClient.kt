package sample.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import sample.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import sample.api.model.Pet

public class PetClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * Finds Pets by status
   */
  public suspend fun getPetFindByStatusMultipleExamples(
    status: List<GetPetFindByStatusMultipleExamplesStatus>,
    test: String? = null,
    testInt: Long? = null,
  ): GetPetFindByStatusMultipleExamplesResponse {
    try {
      val response = configuration.client.`get`("pet/findByStatus/MultipleExamples") {
        url {
          parameters.append("status", status.joinToString(",") { it.serialName() })
          if (test != null) {
            parameters.append("test", test)
          }
          if (testInt != null) {
            parameters.append("testInt", testInt.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> GetPetFindByStatusMultipleExamplesResponseSuccess(response.body<List<Pet>>())
        400 -> GetPetFindByStatusMultipleExamplesResponseFailure
        else -> GetPetFindByStatusMultipleExamplesResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetPetFindByStatusMultipleExamplesResponseUnknownFailure(500)
    }
  }

  /**
   * Finds Pets by status
   */
  public suspend fun getPetFindByStatusSingleExample(status: List<GetPetFindByStatusSingleExampleStatus>): GetPetFindByStatusSingleExampleResponse {
    try {
      val response = configuration.client.`get`("pet/findByStatus/singleExample") {
        url {
          parameters.append("status", status.joinToString(",") { it.serialName() })
        }
      }
      return when (response.status.value) {
        200 -> GetPetFindByStatusSingleExampleResponseSuccess(response.body<List<Pet>>())
        400 -> GetPetFindByStatusSingleExampleResponseFailure
        else -> GetPetFindByStatusSingleExampleResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetPetFindByStatusSingleExampleResponseUnknownFailure(500)
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
  public enum class GetPetFindByStatusMultipleExamplesStatus {
    @SerialName("available")
    AVAILABLE,
    @SerialName("pending")
    PENDING,
    @SerialName("sold")
    SOLD,
    ;

    public fun serialName(): String = GetPetFindByStatusMultipleExamplesStatus.serializer().descriptor.getElementName(this.ordinal)
  }

  @Serializable
  public enum class GetPetFindByStatusSingleExampleStatus {
    @SerialName("available")
    AVAILABLE,
    @SerialName("pending")
    PENDING,
    @SerialName("sold")
    SOLD,
    ;

    public fun serialName(): String = GetPetFindByStatusSingleExampleStatus.serializer().descriptor.getElementName(this.ordinal)
  }

  @Serializable
  public sealed class GetPetFindByStatusMultipleExamplesResponse

  @Serializable
  public data class GetPetFindByStatusMultipleExamplesResponseSuccess(
    public val body: List<Pet>,
  ) : GetPetFindByStatusMultipleExamplesResponse()

  @Serializable
  public object GetPetFindByStatusMultipleExamplesResponseFailure : GetPetFindByStatusMultipleExamplesResponse()

  @Serializable
  public data class GetPetFindByStatusMultipleExamplesResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetPetFindByStatusMultipleExamplesResponse()

  @Serializable
  public sealed class GetPetFindByStatusSingleExampleResponse

  @Serializable
  public data class GetPetFindByStatusSingleExampleResponseSuccess(
    public val body: List<Pet>,
  ) : GetPetFindByStatusSingleExampleResponse()

  @Serializable
  public object GetPetFindByStatusSingleExampleResponseFailure : GetPetFindByStatusSingleExampleResponse()

  @Serializable
  public data class GetPetFindByStatusSingleExampleResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetPetFindByStatusSingleExampleResponse()

  @Serializable
  public sealed class AddPetResponse

  @Serializable
  public object AddPetResponseFailure : AddPetResponse()

  @Serializable
  public data class AddPetResponseUnknownFailure(
    public val statusCode: Int,
  ) : AddPetResponse()
}
