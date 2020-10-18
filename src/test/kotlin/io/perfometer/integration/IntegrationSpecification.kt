package io.perfometer.integration

import io.kotest.matchers.shouldBe
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.perfometer.dsl.scenario
import io.perfometer.http.client.SimpleHttpClient
import io.perfometer.runner.DefaultScenarioRunner
import io.perfometer.runner.RunnerConfiguration
import io.perfometer.statistics.printer.StdOutStatisticsPrinter
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.Delegates
import kotlin.test.Test

class IntegrationSpecification {

    private fun findFreePort(): Int {
        ServerSocket(0).use {
            return it.localPort
        }
    }

    private fun startRestServer(): Int {
        val store = ConcurrentHashMap<Int, String>()
        val id = AtomicInteger(0)
        val port = findFreePort()

        val server = embeddedServer(Netty, port) {
            routing {
                post("/string") {
                    val currentId = id.incrementAndGet()
                    store[currentId] = call.receiveText()
                    call.respondText(currentId.toString(), ContentType.Text.Plain)
                }
                get("/string/{id}") {
                    val string = store[call.parameters["id"]?.toInt()]
                    if (string != null) {
                        call.respondText(string, ContentType.Text.Plain)
                    } else {
                        call.respondText("not found", ContentType.Text.Plain, HttpStatusCode.NotFound)
                    }
                }
                put("/string/{id}") {
                    val currentId = call.parameters["id"]?.toInt()!!
                    store[currentId] = call.receiveText()
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
        server.start()

        return port
    }

    @Test
    fun `should properly run scenario with request to real server`() {
        val port = startRestServer()

        DefaultScenarioRunner(SimpleHttpClient(true), StdOutStatisticsPrinter())
                .run(scenario("http","localhost", port) {
                    var id by Delegates.notNull<Int>()
                    val string = "string with random number: ${ThreadLocalRandom.current().nextInt() % 100}"
                    post().path { "/string" }.body { string.toByteArray() }.consume {
                        id = it.body!!.toInt()
                    }
                    get().path { "/string/${id}" }.consume {
                        it.body shouldBe string
                    }
                    put().path { "/string/${id}" }.body { "just a string".toByteArray() }
                    get().path { "/string/${id}" }.consume {
                        it.body shouldBe "just a string"
                    }
                }, RunnerConfiguration(10))
    }
}
