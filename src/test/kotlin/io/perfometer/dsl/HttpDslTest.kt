package io.perfometer.dsl

import io.perfometer.http.Get
import io.perfometer.http.Post
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class HttpDslTest {
    @Test
    fun `should create scenario with one get request`() {

        val scenario = scenario("perfometer.io", 443) {
            get("/")
        }

        Assertions.assertEquals(1, scenario.requests.size)
        Assertions.assertEquals(Get("perfometer.io", 443, "/"), scenario.requests.first())
    }

    @Test
    fun `should create scenario with two get requests`() {

        val scenario = scenario("perfometer.io", 443) {
            get("/")
            get("/path")
        }

        Assertions.assertEquals(2, scenario.requests.size)
        Assertions.assertEquals(Get("perfometer.io", 443, "/"), scenario.requests[0])
        Assertions.assertEquals(Get("perfometer.io", 443, "/path"), scenario.requests[1])
    }

    @Test
    fun `should create scenario with post request`() {

        val scenario = scenario("perfometer.io", 443) {
            post("/", "foo bar".toByteArray())
        }

        Assertions.assertEquals(1, scenario.requests.size)
        Assertions.assertEquals(Post("perfometer.io", 443, "/", body = "foo bar".toByteArray()), scenario.requests[0])
    }
}