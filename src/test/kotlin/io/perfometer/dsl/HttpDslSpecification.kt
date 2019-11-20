package io.perfometer.dsl

import io.kotlintest.fail
import io.kotlintest.shouldBe
import io.perfometer.http.HttpMethod
import io.perfometer.http.PauseStep
import io.perfometer.http.RequestStep
import java.time.Duration
import java.util.*
import kotlin.test.Test

@Suppress("FunctionName")
internal class HttpDslSpecification {

    @Test
    fun `should create scenario with one get request`() {

        val scenario = scenario("https", "perfometer.io", 443) {
            get().path { "/" }
        }

        scenario.steps.size shouldBe 1
        when (val step = scenario.steps.first()) {
            is RequestStep -> {
                step.request.method shouldBe HttpMethod.GET
                step.request.protocol shouldBe "https"
                step.request.host shouldBe "perfometer.io"
                step.request.port shouldBe 443
            }
            else           -> fail("Expected RequestStep")
        }
    }

    @Test
    fun `should create scenario with two get requests, the second with params`() {

        val scenario = scenario("https", "perfometer.io", 443) {
            get().path { "/" }
                    .header { HttpHeader("x-foo", "bar") }

            get().path { "/path" }
                    .param { HttpParam("foo", "bar") }
                    .param { HttpParam("bar", "baz") }
        }

        scenario.steps.size shouldBe 2
        when (val step1 = scenario.steps[0]) {
            is RequestStep -> {
                step1.request.pathWithParams() shouldBe "/"
                step1.request.headers() shouldBe mapOf("x-foo" to "bar")
            }
            else           -> fail("Expected RequestStep")
        }

        when (val step2 = scenario.steps[1]) {
            is RequestStep -> {
                step2.request.method shouldBe HttpMethod.GET
                step2.request.pathWithParams() shouldBe "/path?foo=bar&bar=baz"
            }
            else           -> fail("Expected RequestStep")
        }

    }

    @Test
    fun `should create scenario with post request`() {

        val expectedBody = "foo bar".toByteArray()
        val scenario = scenario("https", "perfometer.io", 443) {
            post().path { "/" }
                    .body { expectedBody }
        }

        scenario.steps.size shouldBe 1
        val step = scenario.steps.first() as RequestStep
        step.request.method shouldBe HttpMethod.POST
        step.request.body() shouldBe expectedBody
    }

    @Test
    fun `should add pause step`() {
        val expectedDuration = Duration.ofMillis(2000)
        val scenario = scenario("https", "perfometer.io", 443) {
            pause(expectedDuration)
        }

        scenario.steps.size shouldBe 1
        when (val step = scenario.steps.first()) {
            is PauseStep -> {
                step.duration shouldBe expectedDuration
            }
            else         -> fail("Expected PauseStep")
        }
    }

    @Test
    fun `all request should contain basic auth header`() {
        val user = "user"
        val password = "password"
        val credentialsEncoded = Base64.getEncoder().encodeToString("$user:$password".toByteArray())

        val securedScenario = scenario("http", "perfometer.io", 80) {
            basicAuth(user, password)
            get().path { "/" }
            get().path { "/" }
            post().path { "/post-path" }
        }

        val securedRequestsCount = securedScenario.steps.filterIsInstance<RequestStep>()
                .mapNotNull { it.request.authorization }
                .filter { authHeader -> authHeader.first == "Authorization" && authHeader.second == "Basic $credentialsEncoded" }
                .count()
        securedRequestsCount shouldBe securedScenario.steps.size
    }

}
