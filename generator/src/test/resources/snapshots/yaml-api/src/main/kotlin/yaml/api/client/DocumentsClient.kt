package yaml.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import kotlin.Int
import kotlin.collections.List
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import yaml.api.client.ClientConfiguration.Companion.defaultClientConfiguration
import yaml.api.model.Document

public class DocumentsClient(
  private val configuration: ClientConfiguration = defaultClientConfiguration,
) {
  /**
   * List all documents
   */
  public suspend fun listDocuments(): ListDocumentsResponse {
    try {
      val response = configuration.client.`get`("documents") {
      }
      return when (response.status.value) {
        200 -> ListDocumentsResponseSuccess(response.body<List<Document>>(), response.headers)
        else -> ListDocumentsResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return ListDocumentsResponseUnknownFailure(500)
    }
  }

  /**
   * Create a document
   */
  public suspend fun createDocument(request: Document): CreateDocumentResponse {
    try {
      val response = configuration.client.post("documents") {
        setBody(request)
        contentType(ContentType("application", "yaml"))
      }
      return when (response.status.value) {
        200 -> CreateDocumentResponseSuccess(response.body<Document>(), response.headers)
        else -> CreateDocumentResponseUnknownFailure(response.status.value, response.headers)
      }
    }
    catch(e: CancellationException) {
      throw e
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateDocumentResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class ListDocumentsResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class ListDocumentsResponseSuccess(
    public val body: List<Document>,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : ListDocumentsResponse()

  @Serializable
  public data class ListDocumentsResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : ListDocumentsResponse()

  @Serializable
  public sealed class CreateDocumentResponse {
    public abstract val headers: Headers
  }

  @Serializable
  public data class CreateDocumentResponseSuccess(
    public val body: Document,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateDocumentResponse()

  @Serializable
  public data class CreateDocumentResponseUnknownFailure(
    public val statusCode: Int,
    @Transient
    override val headers: Headers = Headers.Empty,
  ) : CreateDocumentResponse()
}
