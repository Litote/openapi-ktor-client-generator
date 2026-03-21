import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Mirrors the generated ClientConfiguration.kt in split-by-client mode.
class ClientConfiguration(
    val engine: HttpClientEngineFactory<*> = CIO,
    val json: Json = Json { ignoreUnknownKeys = true },
    val httpClientConfig: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) { json(json) }
    },
    val client: HttpClient = HttpClient(engine) { httpClientConfig() },
)
