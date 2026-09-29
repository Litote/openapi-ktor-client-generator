package org.litote.openapi.ktor.client.generator

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.api.createClientPlugin
import simple.api.client.Client
import simple.api.client.ClientConfiguration
import simple.api.model.TestRequest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CancellationTest {
    @Test
    fun `GIVEN request cancelled WHEN calling generated client THEN CancellationException is rethrown and not logged`() {
        val cancellingPlugin =
            createClientPlugin("CancellingPlugin") {
                onRequest { _, _ -> throw CancellationException("cancelled") }
            }
        var exceptionLogged = false
        val configuration =
            ClientConfiguration(
                client = HttpClient(CIO) { install(cancellingPlugin) },
                exceptionLogger = { exceptionLogged = true },
            )
        val client = Client(configuration)

        val latch = CountDownLatch(1)
        var result: Result<Any>? = null
        suspend { client.postTestWithTestId(TestRequest("name"), "id") }
            .startCoroutine(
                Continuation(EmptyCoroutineContext) {
                    result = it
                    latch.countDown()
                },
            )

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Call should complete")
        assertIs<CancellationException>(result?.exceptionOrNull(), "CancellationException must be rethrown")
        assertFalse(exceptionLogged, "Cancellation must not be reported to exceptionLogger")
    }
}
