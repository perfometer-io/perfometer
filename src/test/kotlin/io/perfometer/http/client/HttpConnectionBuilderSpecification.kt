package io.perfometer.http.client

import io.kotest.matchers.shouldBe
import java.net.URL
import kotlin.test.Test

@Suppress("FunctionName")
class HttpConnectionBuilderSpecification {

    @Test
    fun `should return GET request connection`() {
        val connection = httpConnection(URL("https://perfometer.io"), "/") {
            method("GET")
            headers(mapOf(
                    "Content-type" to listOf("application/json"),
                    "Accept" to listOf("text/html"),
            ))
        }

        connection.requestMethod shouldBe "GET"
        connection.url shouldBe URL("https://perfometer.io/")
        connection.getRequestProperty("Content-type") shouldBe "application/json"
        connection.getRequestProperty("Accept") shouldBe "text/html"
    }

}
