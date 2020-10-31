package io.perfometer.integration

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.sessions.*
import java.net.ServerSocket
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
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
        val userStore = ConcurrentHashMap<String, String>()
        val id = AtomicInteger(0)
        val port = findFreePort()

        server = embeddedServer(Netty, port) {
            install(Sessions) {
                cookie<User>("SESSION", SessionStorageMemory())
            }
            routing {
                post("/string") {
                    val currentId = id.incrementAndGet()
                    stringStore[currentId] = call.receiveText()
                    call.respondText(currentId.toString(), ContentType.Text.Plain)
                }
                get("/string/{id}") {
                    val string = stringStore[call.parameters["id"]?.toInt()]
                    if (string != null) {
                        call.respondText(string, ContentType.Text.Plain)
                    } else {
                        call.respondText("not found", ContentType.Text.Plain, HttpStatusCode.NotFound)
                    }
                }
                put("/string/{id}") {
                    val currentId = call.parameters["id"]?.toInt()!!
                    stringStore[currentId] = call.receiveText()
                    call.respond(HttpStatusCode.OK)
                }

                post("/login") {
                    val username = call.parameters["username"]!!
                    call.sessions.set(User(username))
                    call.respond(HttpStatusCode.OK)
                }
                get("/current-user") {
                    call.respondText(call.sessions.get<User>()?.username ?: "", ContentType.Text.Plain)
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
