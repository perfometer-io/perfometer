package io.perfometer.runner

import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.perfometer.dsl.RequestBuilder
import io.perfometer.dsl.scenario
import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpResponse
import io.perfometer.http.HttpStatus
import io.perfometer.http.client.HttpClient
import io.perfometer.statistics.ScenarioSummary
import io.perfometer.statistics.printer.StatisticsPrinter
import org.junit.Before
import java.net.URL
import java.time.Duration
import kotlin.test.Test

@Suppress("FunctionName")
class DefaultScenarioRunnerSpecification {

    private val httpClient = object : HttpClient {
        val requests = mutableListOf<RequestBuilder>()
        override fun executeHttp(request: RequestBuilder): HttpResponse {
            synchronized(this) {
                requests += request
            }
            return HttpResponse(HttpStatus(200))
        }
    }

    private val statsPrinter = object : StatisticsPrinter {
        var calls = 0
        override fun print(scenarioSummary: ScenarioSummary) {
            calls++
        }
    }

    private val runner = DefaultScenarioRunner(httpClient, statsPrinter)

    @Before
    fun beforeEach() {
        statsPrinter.calls = 0
    }

    @Test
    fun `should execute single GET request on a single thread`() {
        val scenario = scenario("https://perfometer.io") {
            get().path { "/" }
        }

        runner.run(scenario, RunnerConfiguration(threads = 1))
        httpClient.requests.size shouldBe 1
        httpClient.requests[0].url shouldBe URL("https://perfometer.io")
        httpClient.requests[0].method shouldBe HttpMethod.GET
        httpClient.requests[0].pathWithParams() shouldBe "/"
        statsPrinter.calls shouldBe 1
    }

    @Test
    fun `should execute 8 requests total on two async jobs`() {
        val scenario = scenario("http://perfometer.io") {
            get().path { "/" }
            get().path { "/" }
            delete().path { "/delete" }
            delete().path { "/delete" }
        }

        runner.run(scenario, RunnerConfiguration(2))
        httpClient.requests.size shouldBe 8
        statsPrinter.calls shouldBe 1
    }

    @Test
    fun `should pause for at least two seconds`() {
        val scenario = scenario("http://perfometer.io") {
            pause(Duration.ofSeconds(2))
        }

        val startTime = System.currentTimeMillis()
        runner.run(scenario, RunnerConfiguration(threads = 1))
        val diff = System.currentTimeMillis() - startTime
        diff shouldBeGreaterThanOrEqualTo 2000L
        statsPrinter.calls shouldBe 1
    }
}
