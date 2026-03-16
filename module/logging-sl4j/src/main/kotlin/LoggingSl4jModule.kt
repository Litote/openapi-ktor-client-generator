package org.litote.openapi.ktor.client.generator.module.logging.sl4j

import org.litote.openapi.ktor.client.generator.ApiGeneratorModule
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig

internal class LoggingSl4jModule : ApiGeneratorModule {
    override fun process(generator: ApiConfigurationGeneratorConfig) {
        generator.exceptionLoggingDefaultValue =
            "{ org.slf4j.LoggerFactory.getLogger(ClientConfiguration::class.java).error(\"error\", this) }"
    }
}
