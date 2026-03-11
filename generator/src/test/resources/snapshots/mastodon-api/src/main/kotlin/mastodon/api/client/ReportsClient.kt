package mastodon.api.client

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mastodon.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import mastodon.api.model.Error
import mastodon.api.model.Report
import mastodon.api.model.ReportCategoryEnum

public class ReportsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * File a report
   */
  public suspend fun createReport(request: CreateReportRequest): CreateReportResponse {
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
  public data class CreateReportRequest(
    @SerialName("account_id")
    public val accountId: String,
    public val category: ReportCategoryEnum? = null,
    public val comment: String? = null,
    public val forward: Boolean? = false,
    @SerialName("rule_ids")
    public val ruleIds: List<String>? = null,
    @SerialName("status_ids")
    public val statusIds: List<String>? = null,
  )

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
