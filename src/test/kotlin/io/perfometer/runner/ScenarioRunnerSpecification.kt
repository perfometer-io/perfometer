package io.perfometer.runner

import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.perfometer.dsl.scenario
import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpResponse
import io.perfometer.http.HttpStatus
import io.perfometer.http.client.HttpClient
import org.junit.Test
import java.time.Duration

@Suppress("FunctionName")
abstract class ScenarioRunnerSpecification {

    private val requests = mutableListOf<HttpRequest>()

    protected val httpClient = object : HttpClient {
        override fun executeHttp(request: HttpRequest): HttpResponse {
            synchronized(this) {
                requests += request
            }
            return HttpResponse(HttpStatus(200))
        }
    }

    protected abstract val runner: ScenarioRunner

    @Test
    fun `should execute single GET request on a single thread`() {
        scenario("https://perfometer.io") {
            get {
                path("/")
            }
        }.runner(runner).run(1)

        requests.size shouldBe  1
        requests[0].url.toString() shouldBe "https://perfometer.io"
        requests[0].method shouldBe HttpMethod.GET
        requests[0].pathWithParams shouldBe "/"

        runner.statistics().finish().statistics.size shouldBe 1
    }

    @Test
    fun `should execute 8 requests total on two async jobs`() {
        scenario("http://perfometer.io") {
            get {
                path("/")
            }
            get {
                path("/")
            }
            delete {
                path("/delete")
            }
            delete {
                path("/delete")
            }
        }.runner(runner).run(2)

        requests.size shouldBe 8
        runner.statistics().finish().statistics.size shouldBe 8
    }

    @Test
    fun `should pause for at least two seconds`() {
        val startTime = System.currentTimeMillis()

        scenario("https://perfometer.io") {
            pause(Duration.ofSeconds(2))
        }.runner(runner).run(1)

        val diff = System.currentTimeMillis() - startTime
        diff shouldBeGreaterThanOrEqualTo  2000L
        runner.statistics().finish().statistics.size shouldBe 1
    }
}
