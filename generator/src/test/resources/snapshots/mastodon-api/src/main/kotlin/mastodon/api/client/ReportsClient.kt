package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Report

public class ReportsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * File a report
   */
  public suspend fun createReport(request: JsonElement): CreateReportResponse {
    try {
      val response = configuration.client.post("api/v1/reports") {
        setBody(request)
        contentType(ContentType.Application.Json)
      }
      return when (response.status.value) {
        200 -> CreateReportResponseSuccess(response.body<Report>())
        401, 404, 422, 429, 503 -> CreateReportResponseFailure401(response.body<Error>())
        410 -> CreateReportResponseFailure
        else -> CreateReportResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateReportResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class CreateReportResponse

  @Serializable
  public data class CreateReportResponseSuccess(
    public val body: Report,
  ) : CreateReportResponse()

  @Serializable
  public data class CreateReportResponseFailure401(
    public val body: Error,
  ) : CreateReportResponse()

  @Serializable
  public object CreateReportResponseFailure : CreateReportResponse()

  @Serializable
  public data class CreateReportResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateReportResponse()
}
