package org.litote.openapi.ktor.client.generator.module.logging.kotlinlogging

import io.mockk.mockk
import io.mockk.verify
import org.litote.openapi.ktor.client.generator.port.ApiConfigurationGeneratorConfig
import kotlin.test.Test

class LoggingKotlinModuleTest {
    @Test
    fun `GIVEN LoggingKotlinModule WHEN process THEN sets exceptionLoggingDefaultValue using kotlin-logging`() {
        val config = mockk<ApiConfigurationGeneratorConfig>(relaxed = true)
        val module = LoggingKotlinModule()

        module.process(config)

        verify {
            config.exceptionLoggingDefaultValue =
                "{ io.github.oshai.kotlinlogging.KotlinLogging.logger(ClientConfiguration::class.java.name).error(this) { \"error\" } }"
        }
    }
}
