package org.example.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlin.String
import kotlin.Throwable
import kotlin.Unit
import kotlinx.serialization.json.Json

public class ClientConfiguration(
  public val baseUrl: String = "http://localhost:8080",
  public val engine: HttpClientEngineFactory<*> = CIO,
  public val json: Json = Json { 
      ignoreUnknownKeys = true
      coerceInputValues = true
       },
  public val httpClientConfig: HttpClientConfig<*>.() -> Unit = defaultHttpClientConfig(json),
  public val client: HttpClient = HttpClient(engine) { httpClientConfig() },
  public val exceptionLogger:
      Throwable.() -> Unit = { org.slf4j.LoggerFactory.getLogger(ClientConfiguration::class.java).error("error", this) },
) {
  public companion object {
    public val defaultClientConfiguration: ClientConfiguration = ClientConfiguration()

    public fun defaultHttpClientConfig(json: Json): HttpClientConfig<*>.() -> Unit = {
      install(Logging)
      install(ContentNegotiation) {
        json(json)
      }
    }

  }
}
