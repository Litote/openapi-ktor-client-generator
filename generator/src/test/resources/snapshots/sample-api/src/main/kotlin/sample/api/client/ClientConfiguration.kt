package sample.api.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlin.String
import kotlin.Throwable
import kotlin.Unit
import kotlinx.serialization.json.Json
import io.ktor.client.request.`header` as setHeader

public class ClientConfiguration(
  public val baseUrl: String = "http://petstore.swagger.io/v2/",
  public val apiKeyHeader: String? = null,
  public val apiKeyQueryParam: String? = null,
  public val engine: HttpClientEngineFactory<*> = CIO,
  public val json: Json = Json { 
      ignoreUnknownKeys = true
       },
  public val httpClientConfig:
      HttpClientConfig<*>.() -> Unit = defaultHttpClientConfig(baseUrl, json, apiKeyHeader, apiKeyQueryParam),
  public val client: HttpClient = HttpClient(engine) { httpClientConfig() },
  public val exceptionLogger: Throwable.() -> Unit = { printStackTrace() },
) {
  public companion object {
    public val defaultClientConfiguration: ClientConfiguration by lazy { ClientConfiguration() }

    public fun defaultHttpClientConfig(
      baseUrl: String,
      json: Json,
      apiKeyHeader: String?,
      apiKeyQueryParam: String?,
    ): HttpClientConfig<*>.() -> Unit = {
      install(Logging)
      install(ContentNegotiation) {
        json(json)
      }
      defaultRequest {
        url(baseUrl)
        apiKeyHeader?.let { setHeader("X-Api-Key", it) }
        apiKeyQueryParam?.let { url.parameters.append("api_key", it) }
      }
    }

  }
}
