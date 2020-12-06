package io.perfometer.integration

import io.ktor.application.*
import io.ktor.http.ContentType.*
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import kotlinx.coroutines.delay
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class BaseIntegrationSpecification {

    private lateinit var server: ApplicationEngine

    protected val port get() = server.environment.connectors.first().port

    data class User(val username: String)

    private fun findFreePort(): Int {
        ServerSocket(0).use {
            return it.localPort
        }
    }

    @BeforeTest
    fun startServer() {
        val stringStore = ConcurrentHashMap<Int, String>()
        val id = AtomicInteger(0)
        val port = findFreePort()

        server = embeddedServer(Netty, port) {
            install(Sessions) {
                cookie<User>("SESSION", SessionStorageMemory())
            }
            routing {
                get("/strings") {
                    call.respondText("string resource", Text.Plain, OK)
                }
                post("/strings") {
                    val currentId = id.incrementAndGet()
                    stringStore[currentId] = call.receiveText()
                    call.respondText(currentId.toString(), Text.Plain)
                }
                get("/strings/{id}") {
                    val string = stringStore[call.parameters["id"]?.toInt()]
                    if (string != null) {
                        call.respondText(string, Text.Plain)
                    } else {
                        call.respondText("not found", Text.Plain, NotFound)
                    }
                }
                put("/strings/{id}") {
                    val currentId = call.parameters["id"]?.toInt()!!
                    stringStore[currentId] = call.receiveText()
                    call.respond(OK)
                }

                post("/login") {
                    val username = call.parameters["username"]!!
                    call.sessions.set(User(username))
                    call.respond(OK)
                }
                get("/current-user") {
                    call.respondText(call.sessions.get<User>()?.username ?: "", Text.Plain)
                }
                get("delay") {
                    val delayTime = call.parameters["time"]!!.toLong()
                    delay(delayTime)
                    call.respondText("OK", Text.Plain)
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
