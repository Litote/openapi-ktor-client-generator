package yaml.api.client

import io.ktor.client.call.body
import io.ktor.client.request.`get`
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.Int
import kotlin.collections.List
import kotlinx.serialization.Serializable
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
        200 -> ListDocumentsResponseSuccess(response.body<List<Document>>())
        else -> ListDocumentsResponseUnknownFailure(response.status.value)
      }
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
        200 -> CreateDocumentResponseSuccess(response.body<Document>())
        else -> CreateDocumentResponseUnknownFailure(response.status.value)
      }
    }
    catch(e: Exception) {
      configuration.exceptionLogger(e)
      return CreateDocumentResponseUnknownFailure(500)
    }
  }

  @Serializable
  public sealed class ListDocumentsResponse

  @Serializable
  public data class ListDocumentsResponseSuccess(
    public val body: List<Document>,
  ) : ListDocumentsResponse()

  @Serializable
  public data class ListDocumentsResponseUnknownFailure(
    public val statusCode: Int,
  ) : ListDocumentsResponse()

  @Serializable
  public sealed class CreateDocumentResponse

  @Serializable
  public data class CreateDocumentResponseSuccess(
    public val body: Document,
  ) : CreateDocumentResponse()

  @Serializable
  public data class CreateDocumentResponseUnknownFailure(
    public val statusCode: Int,
  ) : CreateDocumentResponse()
}
