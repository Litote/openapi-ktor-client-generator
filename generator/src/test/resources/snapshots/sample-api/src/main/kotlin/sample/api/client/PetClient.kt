package sample.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import sample.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import sample.api.model.Pet

public interface PetClient {
  /**
   * Finds Pets by status
   */
  public suspend fun getPetFindByStatusMultipleExamples(
    status: List<GetPetFindByStatusMultipleExamplesStatus>,
    test: String? = null,
    testInt: Long? = null,
  ): GetPetFindByStatusMultipleExamplesResponse

  /**
   * Finds Pets by status
   */
  public suspend fun getPetFindByStatusSingleExample(status: List<GetPetFindByStatusSingleExampleStatus>): GetPetFindByStatusSingleExampleResponse

  /**
   * Add a new pet to the store
   */
  public suspend fun addPet(request: Pet): AddPetResponse

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
  public sealed class GetPetFindByStatusMultipleExamplesResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetPetFindByStatusMultipleExamplesResponseSuccess(
    public val body: List<Pet>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPetFindByStatusMultipleExamplesResponse()

  @Serializable
  public data class GetPetFindByStatusMultipleExamplesResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPetFindByStatusMultipleExamplesResponse()

  @Serializable
  public data class GetPetFindByStatusMultipleExamplesResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPetFindByStatusMultipleExamplesResponse()

  @Serializable
  public sealed class GetPetFindByStatusSingleExampleResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetPetFindByStatusSingleExampleResponseSuccess(
    public val body: List<Pet>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPetFindByStatusSingleExampleResponse()

  @Serializable
  public data class GetPetFindByStatusSingleExampleResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPetFindByStatusSingleExampleResponse()

  @Serializable
  public data class GetPetFindByStatusSingleExampleResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetPetFindByStatusSingleExampleResponse()

  @Serializable
  public sealed class AddPetResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class AddPetResponseFailure(
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : AddPetResponse()

  @Serializable
  public data class AddPetResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : AddPetResponse()
}

public fun PetClient(configuration: ClientConfiguration = defaultClientConfiguration): PetClient = DefaultPetClient(configuration)

public class DefaultPetClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : PetClient {
  override suspend fun getPetFindByStatusMultipleExamples(
    status: List<PetClient.GetPetFindByStatusMultipleExamplesStatus>,
    test: String?,
    testInt: Long?,
  ): PetClient.GetPetFindByStatusMultipleExamplesResponse {
    try {
      val response = configuration.client.`get`("pet/findByStatus/MultipleExamples") {
        url {
          parameters.appendAll("status", status.map { it.serialName() })
          if (test != null) {
            parameters.append("test", test)
          }
          if (testInt != null) {
            parameters.append("testInt", testInt.toString())
          }
        }
      }
      return when (response.status.value) {
        200 -> PetClient.GetPetFindByStatusMultipleExamplesResponseSuccess(response.body<List<Pet>>(), response.headers)
        400 -> PetClient.GetPetFindByStatusMultipleExamplesResponseFailure(response.headers)
        else -> PetClient.GetPetFindByStatusMultipleExamplesResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PetClient.GetPetFindByStatusMultipleExamplesResponseUnknownFailure(500)
    }
  }

  override suspend fun getPetFindByStatusSingleExample(status: List<PetClient.GetPetFindByStatusSingleExampleStatus>): PetClient.GetPetFindByStatusSingleExampleResponse {
    try {
      val response = configuration.client.`get`("pet/findByStatus/singleExample") {
        url {
          parameters.appendAll("status", status.map { it.serialName() })
        }
      }
      return when (response.status.value) {
        200 -> PetClient.GetPetFindByStatusSingleExampleResponseSuccess(response.body<List<Pet>>(), response.headers)
        400 -> PetClient.GetPetFindByStatusSingleExampleResponseFailure(response.headers)
        else -> PetClient.GetPetFindByStatusSingleExampleResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PetClient.GetPetFindByStatusSingleExampleResponseUnknownFailure(500)
    }
  }

  override suspend fun addPet(request: Pet): PetClient.AddPetResponse {
    try {
      val response = configuration.client.post("pet") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        405 -> PetClient.AddPetResponseFailure(response.headers)
        else -> PetClient.AddPetResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return PetClient.AddPetResponseUnknownFailure(500)
    }
  }
}
