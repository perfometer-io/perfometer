package io.perfometer.runner

import io.kotlintest.matchers.numerics.shouldBeGreaterThanOrEqual
import io.kotlintest.shouldBe
import io.perfometer.dsl.scenario
import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpStatus
import io.perfometer.http.RequestStep
import io.perfometer.http.client.HttpClient
import io.perfometer.statistics.printer.StatisticsPrinter
import io.perfometer.statistics.ScenarioSummary
import org.junit.Before
import java.time.Duration
import kotlin.test.Test

@Suppress("FunctionName")
class DefaultScenarioRunnerSpecification {

    private val httpClient = object : HttpClient {
        val requests = mutableListOf<HttpRequest>()
        override fun executeHttp(request: HttpRequest): HttpStatus {
            synchronized(this) {
                requests += request
            }
            return HttpStatus(200)
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
        val scenario = scenario("perfomerter.io", 443) {
            get("/")
        }

        val expectedRequest = (scenario.steps[0] as RequestStep).request

        runner.run(scenario, RunnerConfiguration(threadCount = 1))
        httpClient.requests.size shouldBe 1
        httpClient.requests.contains(expectedRequest)
        statsPrinter.calls shouldBe 1
    }

    @Test
    fun `should execute 8 requests total on two async jobs`() {
        val scenario = scenario("perfomerter.io", 80) {
            get("/")
            get("/")
            delete("/delete")
            delete("/delete")
        }

        runner.run(scenario, RunnerConfiguration(2))
        httpClient.requests.size shouldBe 8
        statsPrinter.calls shouldBe 1
    }

    @Test
    fun `should pause for at least two seconds`() {
        val scenario = scenario("perfometer.io", 80) {
            pause(Duration.ofSeconds(2))
        }

        val startTime = System.currentTimeMillis()
        runner.run(scenario, RunnerConfiguration(threadCount = 1))
        val diff = System.currentTimeMillis() - startTime
        diff shouldBeGreaterThanOrEqual 2000L
        statsPrinter.calls shouldBe 1
    }
}
