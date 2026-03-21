import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.Serializable

// Mirrors the generated XxxClient.kt
// - calls configuration.client.get() → HttpClient (has CoroutineScope as supertype)
// - uses response.status.value      → HttpResponse (has CoroutineScope as supertype)
// - calls response.body<T>()        → Ktor body extension
class TestClient(private val configuration: ClientConfiguration) {
    suspend fun get(url: String): GetResponse {
        try {
            val response = configuration.client.get(url)
            return when (response.status.value) {
                200 -> GetResponseSuccess(response.body<TestModel>())
                else -> GetResponseFailure(response.status.value)
            }
        } catch (e: Exception) {
            return GetResponseFailure(500)
        }
    }

    @Serializable
    sealed class GetResponse

    @Serializable
    data class GetResponseSuccess(val body: TestModel) : GetResponse()

    @Serializable
    data class GetResponseFailure(val statusCode: Int) : GetResponse()
}

@Serializable
data class TestModel(val id: String, val name: String)
