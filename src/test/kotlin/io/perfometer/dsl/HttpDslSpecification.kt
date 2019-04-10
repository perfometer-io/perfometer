package io.perfometer.dsl

import io.kotlintest.shouldBe
import io.perfometer.http.Get
import io.perfometer.http.PauseStep
import io.perfometer.http.Post
import java.time.Duration
import kotlin.test.Test

@Suppress("FunctionName")
internal class HttpDslSpecification {

    @Test
    fun `should create scenario with one get request`() {

        val scenario = scenario("perfometer.io", 443) {
            get("/")
        }

        scenario.steps.size shouldBe 1
        scenario.steps.first() shouldBe Get("perfometer.io", 443, "/")
    }

    @Test
    fun `should create scenario with two get requests`() {

        val scenario = scenario("perfometer.io", 443) {
            get("/")
            get("/path")
        }

        scenario.steps.size shouldBe 2
        scenario.steps[0] shouldBe Get("perfometer.io", 443, "/")
        scenario.steps[1] shouldBe Get("perfometer.io", 443, "/path")
    }

    @Test
    fun `should create scenario with post request`() {

        val body = "foo bar".toByteArray()
        val scenario = scenario("perfometer.io", 443) {
            post("/", body)
        }

        scenario.steps.size shouldBe 1
        scenario.steps[0] shouldBe Post("perfometer.io", 443, "/", body = body)
    }

    @Test
    fun `should add pause step`() {
        val scenario = scenario("perfometer.io", 443) {
            pause(Duration.ofMillis(2000))
        }

        scenario.steps.size shouldBe 1
        scenario.steps[0] shouldBe PauseStep(Duration.ofMillis(2000))
    }
}
