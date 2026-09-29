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

public interface FilesClient {
  public suspend fun downloadFile(id: String): DownloadFileResponse

  public suspend fun getImage(id: String): GetImageResponse

  public suspend fun getReport(): GetReportResponse

  public suspend fun exportCsv(): ExportCsvResponse

  public suspend fun download(): DownloadResponse

  public suspend fun getDocument(id: String): GetDocumentResponse

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

public fun FilesClient(configuration: ClientConfiguration = defaultClientConfiguration): FilesClient = DefaultFilesClient(configuration)

public class DefaultFilesClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) : FilesClient {
  override suspend fun downloadFile(id: String): FilesClient.DownloadFileResponse {
    try {
      val response = configuration.client.`get`("files/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        accept(ContentType.parse("application/octet-stream"))
      }
      return when (response.status.value) {
        200 -> FilesClient.DownloadFileResponseSuccess(response.body<ByteArray>(), response.headers)
        404 -> FilesClient.DownloadFileResponseFailure(response.body<Error>(), response.headers)
        else -> FilesClient.DownloadFileResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FilesClient.DownloadFileResponseUnknownFailure(500)
    }
  }

  override suspend fun getImage(id: String): FilesClient.GetImageResponse {
    try {
      val response = configuration.client.`get`("images/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
        accept(ContentType.parse("image/png"))
      }
      return when (response.status.value) {
        200 -> FilesClient.GetImageResponseSuccess(response.body<ByteArray>(), response.headers)
        else -> FilesClient.GetImageResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FilesClient.GetImageResponseUnknownFailure(500)
    }
  }

  override suspend fun getReport(): FilesClient.GetReportResponse {
    try {
      val response = configuration.client.`get`("report") {
        accept(ContentType.parse("text/plain"))
      }
      return when (response.status.value) {
        200 -> FilesClient.GetReportResponseSuccess(response.body<String>(), response.headers)
        else -> FilesClient.GetReportResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FilesClient.GetReportResponseUnknownFailure(500)
    }
  }

  override suspend fun exportCsv(): FilesClient.ExportCsvResponse {
    try {
      val response = configuration.client.`get`("export") {
        accept(ContentType.parse("text/csv"))
      }
      return when (response.status.value) {
        200 -> FilesClient.ExportCsvResponseSuccess(response.body<String>(), response.headers)
        else -> FilesClient.ExportCsvResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FilesClient.ExportCsvResponseUnknownFailure(500)
    }
  }

  override suspend fun download(): FilesClient.DownloadResponse {
    try {
      val response = configuration.client.`get`("download") {
      }
      return when (response.status.value) {
        200 -> FilesClient.DownloadResponseSuccess(response.body<ByteArray>(), response.headers)
        else -> FilesClient.DownloadResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FilesClient.DownloadResponseUnknownFailure(500)
    }
  }

  override suspend fun getDocument(id: String): FilesClient.GetDocumentResponse {
    try {
      val response = configuration.client.`get`("documents/{id}".replace("/{id}", "/${id.encodeURLPathPart()}")) {
      }
      return when (response.status.value) {
        200 -> FilesClient.GetDocumentResponseSuccess(response.body<Document>(), response.headers)
        else -> FilesClient.GetDocumentResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return FilesClient.GetDocumentResponseUnknownFailure(500)
    }
  }
}
