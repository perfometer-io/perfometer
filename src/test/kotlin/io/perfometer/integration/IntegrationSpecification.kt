package io.perfometer.integration

import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.perfometer.dsl.data
import io.perfometer.dsl.scenario
import io.perfometer.http.HttpHeaders
import io.perfometer.http.client.KtorHttpClient
import io.perfometer.runner.CoroutinesScenarioRunner
import io.perfometer.runner.ThreadPoolScenarioRunner
import java.time.Duration
import java.util.concurrent.ThreadLocalRandom
import kotlin.properties.Delegates
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IntegrationSpecification : BaseIntegrationSpecification() {

    @Test
    fun `should properly run scenario with request to real server`() {
        scenario("http://localhost:${port}") {
            var id by Delegates.notNull<Int>()
            val strings =
                "strings with random number: ${ThreadLocalRandom.current().nextInt() % 100}"

            post {
                path("/strings")
                body(strings.toByteArray())
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to listOf("text/plain; charset=UTF-8"))
                    id = it.asString().toInt()
                }
            }
            get {
                name("GET /strings/:id")
                path("/strings/${id}")
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to listOf("text/plain; charset=UTF-8"))
                    it.asString() shouldBe strings
                }
            }
            put {
                name("PUT /strings/:id")
                path("/strings/${id}")
                body("just a strings".toByteArray())
            }
            get {
                name("GET /strings/:id")
                path("/strings/${id}")
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to listOf("text/plain; charset=UTF-8"))
                    it.asString() shouldBe "just a strings"
                }
            }
        }.run(100, Duration.ofSeconds(1))
    }

    data class CsvString(val id: Int, val text: String)

    @Test
    fun `should use data from CSV file`() {
        val strings = data<CsvString> {
            fromCsv(
                CsvString::class,
                this@IntegrationSpecification::class.java.getResource("strings.csv").path
            )
            random()
        }

        scenario("http://localhost:${port}") {
            var id by Delegates.notNull<Int>()

            post {
                path("/strings")
                body(strings.next().text.toByteArray())
                consume {
                    it.headers shouldContain (HttpHeaders.CONTENT_TYPE to listOf("text/plain; charset=UTF-8"))
                    id = it.asString().toInt()
                }
            }
            get {
                name("GET /strings/:id")
                path("/strings/${id}")
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

    @Test
    fun `should run parallel requests`() {
        listOf(
            CoroutinesScenarioRunner { KtorHttpClient() },
            ThreadPoolScenarioRunner { KtorHttpClient() },
        ).forEach { runner ->
            var body: String? = null
            val summary = scenario("http://localhost:${port}") {
                get { path("/strings") }
                parallel {
                    get {
                        name("async-get")
                        path("/delay?time=200")
                    }

                    post {
                        name("async-post")
                        path("/strings")
                        body("body".toByteArray())
                        consume {
                            body = it.asString()
                        }
                    }
                }
            }.runner(runner).run(50, Duration.ofMillis(1000))

            assertNotNull(body)
            assertTrue { summary.summaries.any { s -> s.name == "async-post" } }
            assertTrue { summary.summaries.any { s -> s.name == "async-get" } }
        }

    }

    @Test
    fun `should not run request declared after parallel block, before all the parallel jobs complete`() {
        listOf(
            CoroutinesScenarioRunner { KtorHttpClient() },
            ThreadPoolScenarioRunner { KtorHttpClient() },
        ).forEach { runner ->
            val summary = scenario("http://localhost:${port}") {
                get { path("/strings") }
                parallel {
                    get {
                        name("slow")
                        path("/delay?time=1100")
                    }
                }
                get { name("should-never-run") }
            }.runner(runner).run(1, Duration.ofSeconds(1))
            assertTrue { summary.summaries.none { it.name == "should-never-run" } }
        }
    }

}
