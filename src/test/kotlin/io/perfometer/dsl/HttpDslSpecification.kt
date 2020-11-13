package io.perfometer.dsl

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import io.perfometer.http.HttpHeaders
import io.perfometer.http.HttpMethod
import io.perfometer.runner.ScenarioRunner
import io.perfometer.statistics.ConcurrentQueueStatisticsCollector
import io.perfometer.statistics.ScenarioSummary
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.test.Test

@Suppress("FunctionName")
internal class HttpDslSpecification {

    private val runner = object : ScenarioRunner {
        val statisticsCollector = ConcurrentQueueStatisticsCollector()

        val steps = mutableListOf<HttpStep>()
        val asyncSteps = mutableListOf<HttpStep>()

        override fun runUsers(
            userCount: Int,
            duration: Duration,
            action: suspend () -> Unit
        ): ScenarioSummary {
            statisticsCollector.start(Instant.ofEpochMilli(1))
            runBlocking { action() }
            return statisticsCollector.finish(Instant.ofEpochMilli(2))
        }

        override suspend fun runStep(step: HttpStep) {
            steps.add(step)
            if (step is ParallelStep) step.builder(HttpDsl(step.baseURL) { registerAsync(it) })
        }

        override suspend fun registerAsync(step: HttpStep) {
            asyncSteps.add(step)
        }
    }

    @Test
    fun `should create scenario with one get request`() {

        scenario("https://perfometer.io") {
            get { path("/") }
        }.runner(runner).run(1, Duration.ZERO)

        runner.steps.size shouldBe 1
        when (val step = runner.steps.first()) {
            is RequestStep -> {
                step.request.method shouldBe HttpMethod.GET
                step.request.url shouldBe URL("https://perfometer.io")
            }
            else -> fail("Expected RequestStep")
        }
    }

    @Test
    fun `should create scenario with two get requests, the second with params`() {

        scenario("https://perfometer.io") {
            get {
                path("/")
                headers("x-foo" to "bar")
            }
            get {
                path("/path")
                params(
                    "foo" to "bar",
                    "bar" to "baz"
                )
            }
        }.runner(runner).run(1, Duration.ZERO)

        runner.steps.size shouldBe 2
        when (val step1 = runner.steps[0]) {
            is RequestStep -> {
                step1.request.pathWithParams shouldBe "/"
                step1.request.headers shouldBe mapOf("x-foo" to listOf("bar"))
            }
            else -> fail("Expected RequestStep")
        }

        when (val step2 = runner.steps[1]) {
            is RequestStep -> {
                step2.request.method shouldBe HttpMethod.GET
                step2.request.pathWithParams shouldBe "/path?foo=bar&bar=baz"
            }
            else -> fail("Expected RequestStep")
        }

    }

    @Test
    fun `should create scenario with post request`() {

        val expectedBody = "foo bar".toByteArray()
        scenario("https://perfometer.io") {
            post {
                path("/")
                body(expectedBody)
            }
        }.runner(runner).run(1, Duration.ZERO)

        runner.steps.size shouldBe 1
        val step = runner.steps.first() as RequestStep
        step.request.method shouldBe HttpMethod.POST
        step.request.body shouldBe expectedBody
    }

    @Test
    fun `should add pause step`() {
        val expectedDuration = Duration.ofMillis(2000)
        scenario("https://perfometer.io") {
            pause(expectedDuration)
        }.runner(runner).run(1, Duration.ZERO)

        runner.steps.size shouldBe 1
        when (val step = runner.steps.first()) {
            is PauseStep -> {
                step.duration shouldBe expectedDuration
            }
            else -> fail("Expected PauseStep")
        }
    }

    @Test
    fun `all request should contain basic auth header`() {
        val user = "user"
        val password = "password"
        val credentialsEncoded = Base64.getEncoder().encodeToString("$user:$password".toByteArray())

        scenario("http://perfometer.io") {
            basicAuth(user, password)
            get {
                path("/")
            }
            get {
                path("/")
            }
            post {
                path("/post-path")
            }
        }.runner(runner).run(1, Duration.ZERO)

        val securedRequestsCount = runner.steps.filterIsInstance<RequestStep>()
            .flatMap { it.request.headers.entries }
            .filter { header -> header.key == HttpHeaders.AUTHORIZATION && header.value[0] == "Basic $credentialsEncoded" }
            .count()
        securedRequestsCount shouldBe runner.steps.size
    }

    @Test
    fun `should add global header to all requests`() {
        val name = "Header-Name"
        val value = "example value"

        scenario("http://perfometer.io") {
            headers(name to value)
            get {
                path("/")
            }
            get {
                path("/")
            }
            post {
                path("/post-path")
            }
        }.runner(runner).run(1, Duration.ZERO)

        val requestsCount = runner.steps.filterIsInstance<RequestStep>()
            .flatMap { it.request.headers.entries }
            .filter { header -> header.key == name && header.value[0] == value }
            .count()
        requestsCount shouldBe runner.steps.size
    }

    @Test
    fun `should use url provided on method level overwriting base url`() {
        // given two different URLs
        val baseUrl = "http://perfometer.io"
        val expectedUrl = "http://localhost:8080"

        // when the scenario is build with base url, but the URL is overwritten per request
        scenario(baseUrl) {
            get(expectedUrl) {
                path("/")
            }
            post(expectedUrl) {
                path("/")
            }
            put(expectedUrl) {
                path("/")
            }
            delete(expectedUrl) {
                path("/")
            }
            patch(expectedUrl) {
                path("/")
            }
        }.runner(runner).run(1, Duration.ZERO)

        // then, the url set on a request is used instead of the global setting
        runner.steps.filterIsInstance<RequestStep>().forEach {
            it.request.url shouldBe URL(expectedUrl)
        }
    }

    @Test
    fun `should override global header in request`() {
        scenario("https://perfometer.io") {
            headers("Accept" to "*/*")
            get {
                headers("Accept" to "application/json")
            }
        }.runner(runner).run(1, Duration.ZERO);

        val headers = runner.steps.filterIsInstance<RequestStep>()
            .flatMap { it.request.headers.entries }
        headers.size shouldBe 1
        headers[0].key shouldBe "Accept"
        headers[0].value shouldBe listOf("application/json")
    }

    @Test
    fun `should add multiple headers to request`() {
        scenario("https://perfometer.io") {
            get {
                headers(
                    "User-Agent" to "perfometer",
                    "Accept" to "application/json"
                )
            }
        }.runner(runner).run(1, Duration.ZERO)

        val headers = runner.steps.filterIsInstance<RequestStep>()
            .flatMap { it.request.headers.entries }
        headers.size shouldBe 2
    }

    @Test
    fun `should be able to name request`() {
        scenario("https://perfometer.io") {
            get {
                name("test name")
            }
        }.runner(runner).run(1, Duration.ZERO)

        when (val step = runner.steps.first()) {
            is RequestStep -> {
                step.request.name shouldBe "test name"
            }
            else -> fail("Expected RequestStep")
        }
    }
}
