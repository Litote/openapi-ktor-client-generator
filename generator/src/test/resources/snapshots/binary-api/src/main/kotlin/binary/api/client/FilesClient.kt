package binary.api.client

import binary.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import binary.api.model.Document
import binary.api.model.Error
import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.encodeURLPathPart
import kotlin.ByteArray
import kotlin.Int
import kotlin.String
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

public class FilesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  public suspend fun downloadFile(id: String): DownloadFileResponse {
    try {
      val response = configuration.client.`get`("files/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        accept(ContentType.parse("application/octet-stream"))
      }
      return when (response.status.value) {
        200 -> DownloadFileResponseSuccess(response.body<ByteArray>(), response.headers)
        404 -> DownloadFileResponseFailure(response.body<Error>(), response.headers)
        else -> DownloadFileResponseUnknownFailure(response.status.value, response.headers)
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
        200 -> GetImageResponseSuccess(response.body<ByteArray>(), response.headers)
        else -> GetImageResponseUnknownFailure(response.status.value, response.headers)
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
        200 -> GetReportResponseSuccess(response.body<String>(), response.headers)
        else -> GetReportResponseUnknownFailure(response.status.value, response.headers)
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
        200 -> ExportCsvResponseSuccess(response.body<String>(), response.headers)
        else -> ExportCsvResponseUnknownFailure(response.status.value, response.headers)
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
        200 -> DownloadResponseSuccess(response.body<ByteArray>(), response.headers)
        else -> DownloadResponseUnknownFailure(response.status.value, response.headers)
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
        200 -> GetDocumentResponseSuccess(response.body<Document>(), response.headers)
        else -> GetDocumentResponseUnknownFailure(response.status.value, response.headers)
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
  public sealed class DownloadFileResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DownloadFileResponseSuccess(
    public val body: ByteArray,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DownloadFileResponse()

  @Serializable
  public data class DownloadFileResponseFailure(
    public val body: Error,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DownloadFileResponse()

  @Serializable
  public data class DownloadFileResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DownloadFileResponse()

  @Serializable
  public sealed class GetImageResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetImageResponseSuccess(
    public val body: ByteArray,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetImageResponse()

  @Serializable
  public data class GetImageResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetImageResponse()

  @Serializable
  public sealed class GetReportResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetReportResponseSuccess(
    public val body: String,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetReportResponse()

  @Serializable
  public data class GetReportResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetReportResponse()

  @Serializable
  public sealed class ExportCsvResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class ExportCsvResponseSuccess(
    public val body: String,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : ExportCsvResponse()

  @Serializable
  public data class ExportCsvResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : ExportCsvResponse()

  @Serializable
  public sealed class DownloadResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class DownloadResponseSuccess(
    public val body: ByteArray,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DownloadResponse()

  @Serializable
  public data class DownloadResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : DownloadResponse()

  @Serializable
  public sealed class GetDocumentResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class GetDocumentResponseSuccess(
    public val body: Document,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDocumentResponse()

  @Serializable
  public data class GetDocumentResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : GetDocumentResponse()
}
