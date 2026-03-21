package org.litote.openapi.ktor.client.generator.module.logging.kotlinlogging

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig

internal class LoggingKotlinModule : ApiGeneratorModule {
    override fun processConfiguration(generator: ApiConfigurationGeneratorConfig) {
        generator.exceptionLoggingDefaultValue =
            "{ io.github.oshai.kotlinlogging.KotlinLogging.logger(ClientConfiguration::class.qualifiedName ?: \"ClientConfiguration\").error(this) { \"error\" } }"
    }
}
