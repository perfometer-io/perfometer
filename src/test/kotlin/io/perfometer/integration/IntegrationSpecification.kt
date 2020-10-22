package io.perfometer.integration

import io.kotest.matchers.shouldBe
import io.perfometer.dsl.scenario
import io.perfometer.http.client.SimpleHttpClient
import io.perfometer.runner.DefaultScenarioRunner
import io.perfometer.runner.RunnerConfiguration
import io.perfometer.statistics.printer.StdOutStatisticsPrinter
import java.util.concurrent.ThreadLocalRandom
import kotlin.properties.Delegates
import kotlin.test.Test

class IntegrationSpecification : BaseIntegrationSpecification() {

    @Test
    fun `should properly run scenario with request to real server`() {
        val port = startRestServer()

        DefaultScenarioRunner(SimpleHttpClient(trustAllCertificates = true), StdOutStatisticsPrinter())
                .run(scenario("http://localhost:$port") {
                    var id by Delegates.notNull<Int>()
                    val string = "string with random number: ${ThreadLocalRandom.current().nextInt() % 100}"
                    post().path { "/string" }.body { string.toByteArray() }.consume {
                        id = it.body!!.toInt()
                    }
                    get().path { "/string/${id}" }.consume {
                        it.body shouldBe string
                    }
                    put().path { "/string/${id}" }.body { "just a string".toByteArray() }
                    get().path { "/string/${id}" }.consume {
                        it.body shouldBe "just a string"
                    }
                }, RunnerConfiguration(threads = 10))
    }

}
