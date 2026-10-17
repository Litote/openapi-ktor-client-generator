package `inline`.response.api.client

import `inline`.response.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import `inline`.response.api.model.Error
import `inline`.response.api.model.Itinerary
import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.http.Headers
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonElement

public interface RoutingClient {
  public suspend fun plan(): PlanResponse

  public suspend fun stops(): StopsResponse

  public suspend fun raw(): RawResponse

  @Serializable
  public data class PlanResponseBody(
    public val debug: Debug? = null,
    public val itineraries: List<Itinerary>,
    public val nextPageCursor: String,
  ) {
    @Serializable
    public data class Debug(
      public val durationMs: Long? = null,
    )
  }

  @Serializable
  public sealed class PlanResponse : OperationResponse<PlanResponseBody> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class PlanResponseSuccess(
    override val body: PlanResponseBody,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PlanResponse(),
      OperationResponseSuccess<PlanResponseBody>

  @Serializable
  public data class PlanResponseFailure(
    override val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PlanResponse(),
      OperationResponseFailure<Error>

  @Serializable
  public data class PlanResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : PlanResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public data class StopsResponse200Body(
    public val count: Long? = null,
  )

  @Serializable
  public data class StopsResponse404Body(
    public val reason: String,
  )

  @Serializable
  public sealed class StopsResponse : OperationResponse<StopsResponse200Body> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class StopsResponseSuccess(
    override val body: StopsResponse200Body,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : StopsResponse(),
      OperationResponseSuccess<StopsResponse200Body>

  @Serializable
  public data class StopsResponseFailure(
    override val body: StopsResponse404Body,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : StopsResponse(),
      OperationResponseFailure<StopsResponse404Body>

  @Serializable
  public data class StopsResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : StopsResponse(),
      OperationResponseUnknownFailure

  @Serializable
  public sealed class RawResponse : OperationResponse<JsonElement> {
    abstract override val headers: Headers
  }

  @Serializable
  public data class RawResponseSuccess(
    override val body: JsonElement,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : RawResponse(),
      OperationResponseSuccess<JsonElement>

  @Serializable
  public data class RawResponseUnknownFailure(
    override val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : RawResponse(),
      OperationResponseUnknownFailure
}

public fun RoutingClient(configuration: ClientConfiguration = defaultClientConfiguration): RoutingClient = DefaultRoutingClient(configuration)

public class DefaultRoutingClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : RoutingClient {
  override suspend fun plan(): RoutingClient.PlanResponse {
    try {
      val response = configuration.client.`get`("plan") {
      }
      return when (response.status.value) {
        200 -> RoutingClient.PlanResponseSuccess(response.body<RoutingClient.PlanResponseBody>(), response.headers)
        400 -> RoutingClient.PlanResponseFailure(response.body<Error>(), response.headers)
        else -> RoutingClient.PlanResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return RoutingClient.PlanResponseUnknownFailure(500)
    }
  }

  override suspend fun stops(): RoutingClient.StopsResponse {
    try {
      val response = configuration.client.`get`("stops") {
      }
      return when (response.status.value) {
        200 -> RoutingClient.StopsResponseSuccess(response.body<RoutingClient.StopsResponse200Body>(), response.headers)
        404 -> RoutingClient.StopsResponseFailure(response.body<RoutingClient.StopsResponse404Body>(), response.headers)
        else -> RoutingClient.StopsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return RoutingClient.StopsResponseUnknownFailure(500)
    }
  }

  override suspend fun raw(): RoutingClient.RawResponse {
    try {
      val response = configuration.client.`get`("raw") {
      }
      return when (response.status.value) {
        200 -> RoutingClient.RawResponseSuccess(response.body<JsonElement>(), response.headers)
        else -> RoutingClient.RawResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return RoutingClient.RawResponseUnknownFailure(500)
    }
  }
}
