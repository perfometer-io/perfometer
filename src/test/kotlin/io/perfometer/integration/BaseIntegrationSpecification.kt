package io.perfometer.integration

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class BaseIntegrationSpecification {

    private lateinit var server: ApplicationEngine

    protected val port get() = server.environment.connectors.first().port

    private fun findFreePort(): Int {
        ServerSocket(0).use {
            return it.localPort
        }
    }

    @BeforeTest
    fun startServer() {
        val store = ConcurrentHashMap<Int, String>()
        val id = AtomicInteger(0)
        val port = findFreePort()

        server = embeddedServer(Netty, port) {
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
    }

    @AfterTest
    fun stopServer() {
        server.stop(0, 0)
    }
}
