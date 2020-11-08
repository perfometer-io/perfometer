package io.perfometer.integration

import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.perfometer.dsl.data
import io.perfometer.dsl.scenario
import io.perfometer.http.HttpHeaders
import io.perfometer.http.client.SimpleHttpClient
import io.perfometer.runner.ThreadPoolScenarioRunner
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom
import kotlin.properties.Delegates
import kotlin.test.Test

class IntegrationSpecification : BaseIntegrationSpecification() {

    @Test
    fun `should properly run scenario with request to real server`() {
        scenario("http://localhost:${port}") {
            var id by Delegates.notNull<Int>()
            val string = "string with random number: ${ThreadLocalRandom.current().nextInt() % 100}"

            post {
                path("/string")
                body(string.toByteArray())
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to listOf("text/plain; charset=UTF-8"))
                    id = it.asString().toInt()
                }
            }
            get {
                name("GET /string/:id")
                path("/string/${id}")
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to listOf("text/plain; charset=UTF-8"))
                    it.asString() shouldBe string
                }
            }
            put {
                name("PUT /string/:id")
                path("/string/${id}")
                body("just a string".toByteArray())
            }
            get {
                name("GET /string/:id")
                path("/string/${id}")
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to listOf("text/plain; charset=UTF-8"))
                    it.asString() shouldBe "just a string"
                }
            }
        }.run(100, Duration.ofSeconds(1))
    }

    data class CsvString(val id: Int, val text: String)

    @Test
    fun `should use data from CSV file`() {
        val strings = data<CsvString> {
            fromCsv(CsvString::class, this@IntegrationSpecification::class.java.getResource("strings.csv").path)
            random()
        }

        scenario("http://localhost:${port}") {
            var id by Delegates.notNull<Int>()

            post {
                path("/string")
                body(strings.next().text.toByteArray())
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to listOf("text/plain; charset=UTF-8"))
                    id = it.asString().toInt()
                }
            }
            get {
                name("GET /string/:id")
                path("/string/${id}")
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to listOf("text/plain; charset=UTF-8"))
                    it.asString() shouldStartWith "text "
                }
            }
        }.run(100, Duration.ofSeconds(1))
    }

    @Test
    fun `should keep session information using cookies`() {
        scenario("http://localhost:${port}") {
            val username = "user ${ThreadLocalRandom.current().nextInt() % 100}"

            post {
                path("/login")
                params("username" to username)
            }
            get {
                path("/current-user")
                consume {
                    it.asString() shouldBe username
                }
            }
        }.run(100, Duration.ofSeconds(1))
    }
}
