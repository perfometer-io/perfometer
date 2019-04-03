package io.perfometer.dsl

import io.kotlintest.shouldBe
import io.perfometer.http.Get
import io.perfometer.http.Post
import kotlin.test.Test

@Suppress("FunctionName")
internal class HttpDslSpecification {

    @Test
    fun `should create scenario with one get request`() {

        val scenario = scenario("perfometer.io", 443) {
            get("/")
        }

        scenario.requests.size shouldBe 1
        scenario.requests.first() shouldBe Get("perfometer.io", 443, "/")
    }

    @Test
    fun `should create scenario with two get requests`() {

        val scenario = scenario("perfometer.io", 443) {
            get("/")
            get("/path")
        }

        scenario.requests.size shouldBe 2
        scenario.requests[0] shouldBe Get("perfometer.io", 443, "/")
        scenario.requests[1] shouldBe Get("perfometer.io", 443, "/path")
    }

    @Test
    fun `should create scenario with post request`() {

        val body = "foo bar".toByteArray()
        val scenario = scenario("perfometer.io", 443) {
            post("/", body)
        }

        scenario.requests.size shouldBe 1
        scenario.requests[0] shouldBe Post("perfometer.io", 443, "/", body = body)
    }
}
