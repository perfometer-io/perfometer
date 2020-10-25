package io.perfometer.integration

import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.perfometer.dsl.scenario
import io.perfometer.http.HttpHeaders
import java.util.concurrent.ThreadLocalRandom
import kotlin.properties.Delegates
import kotlin.test.Test

class IntegrationSpecification : BaseIntegrationSpecification() {

    @Test
    fun `should properly run scenario with request to real server`() {
        val port = startRestServer()

        scenario("http://localhost:${port}") {
            var id by Delegates.notNull<Int>()
            val string = "string with random number: ${ThreadLocalRandom.current().nextInt() % 100}"
            post {
                path("/string")
                body(string.toByteArray())
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to "text/plain; charset=UTF-8")
                    id = it.asString().toInt()
                }
            }
            get {
                path("/string/${id}")
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to "text/plain; charset=UTF-8")
                    it.asString() shouldBe string
                }
            }
            put {
                path("/string/${id}")
                body("just a string".toByteArray())
            }
            get {
                path("/string/${id}")
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to "text/plain; charset=UTF-8")
                    it.asString() shouldBe "just a string"
                }
            }
        }.run(10)
    }

}
