package binary.api.client

import binary.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import binary.api.model.Document
import binary.api.model.Error
import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.encodeURLPathPart
import kotlin.ByteArray
import kotlin.Int
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable

public class FilesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  public suspend fun downloadFile(id: String): DownloadFileResponse {
    try {
      val response = configuration.client.`get`("files/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        accept(ContentType.parse("application/octet-stream"))
      }
      return when (response.status.value) {
        200 -> DownloadFileResponseSuccess(response.body<ByteArray>())
        404 -> DownloadFileResponseFailure(response.body<Error>())
        else -> DownloadFileResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DownloadFileResponseUnknownFailure(500)
    }
  }

  public suspend fun getImage(id: String): GetImageResponse {
    try {
      val response = configuration.client.`get`("images/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        accept(ContentType.parse("image/png"))
      }
      return when (response.status.value) {
        200 -> GetImageResponseSuccess(response.body<ByteArray>())
        else -> GetImageResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetImageResponseUnknownFailure(500)
    }
  }

  public suspend fun getReport(): GetReportResponse {
    try {
      val response = configuration.client.`get`("report") {
        accept(ContentType.parse("text/plain"))
      }
      return when (response.status.value) {
        200 -> GetReportResponseSuccess(response.body<String>())
        else -> GetReportResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetReportResponseUnknownFailure(500)
    }
  }

  public suspend fun exportCsv(): ExportCsvResponse {
    try {
      val response = configuration.client.`get`("export") {
        accept(ContentType.parse("text/csv"))
      }
      return when (response.status.value) {
        200 -> ExportCsvResponseSuccess(response.body<String>())
        else -> ExportCsvResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return ExportCsvResponseUnknownFailure(500)
    }
  }

  public suspend fun download(): DownloadResponse {
    try {
      val response = configuration.client.`get`("download") {
      }
      return when (response.status.value) {
        200 -> DownloadResponseSuccess(response.body<ByteArray>())
        else -> DownloadResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return DownloadResponseUnknownFailure(500)
    }
  }

  public suspend fun getDocument(id: String): GetDocumentResponse {
    try {
      val response = configuration.client.`get`("documents/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> GetDocumentResponseSuccess(response.body<Document>())
        else -> GetDocumentResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return GetDocumentResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class DownloadFileResponse

  @Serializable
  public data class DownloadFileResponseSuccess(
    public val body: ByteArray,
  ) : DownloadFileResponse()

  @Serializable
  public data class DownloadFileResponseFailure(
    public val body: Error,
  ) : DownloadFileResponse()

  @Serializable
  public data class DownloadFileResponseUnknownFailure(
    public val statusCode: Int,
  ) : DownloadFileResponse()

  @Serializable
  public sealed class GetImageResponse

  @Serializable
  public data class GetImageResponseSuccess(
    public val body: ByteArray,
  ) : GetImageResponse()

  @Serializable
  public data class GetImageResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetImageResponse()

  @Serializable
  public sealed class GetReportResponse

  @Serializable
  public data class GetReportResponseSuccess(
    public val body: String,
  ) : GetReportResponse()

  @Serializable
  public data class GetReportResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetReportResponse()

  @Serializable
  public sealed class ExportCsvResponse

  @Serializable
  public data class ExportCsvResponseSuccess(
    public val body: String,
  ) : ExportCsvResponse()

  @Serializable
  public data class ExportCsvResponseUnknownFailure(
    public val statusCode: Int,
  ) : ExportCsvResponse()

  @Serializable
  public sealed class DownloadResponse

  @Serializable
  public data class DownloadResponseSuccess(
    public val body: ByteArray,
  ) : DownloadResponse()

  @Serializable
  public data class DownloadResponseUnknownFailure(
    public val statusCode: Int,
  ) : DownloadResponse()

  @Serializable
  public sealed class GetDocumentResponse

  @Serializable
  public data class GetDocumentResponseSuccess(
    public val body: Document,
  ) : GetDocumentResponse()

  @Serializable
  public data class GetDocumentResponseUnknownFailure(
    public val statusCode: Int,
  ) : GetDocumentResponse()
}
