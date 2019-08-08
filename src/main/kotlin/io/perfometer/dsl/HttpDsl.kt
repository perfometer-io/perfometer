package io.perfometer.dsl

import io.perfometer.http.*
import java.time.Duration

class HttpDsl(private val host: String, private val port: Int) {
    private val steps: MutableList<Step> = mutableListOf()

    fun get(path: String) {
        steps.add(RequestStep(Get(host, port, path)))
    }

    fun post(path: String, body: ByteArray) {
        steps.add(RequestStep(Post(host, port, path, body = body)))
    }

    fun put(path: String, body: ByteArray) {
        steps.add(RequestStep(Put(host, port, path, body = body)))
    }

    fun delete(path: String) {
        steps.add(RequestStep(Delete(host, port, path)))
    }

    fun patch(path: String) {
        steps.add(RequestStep(Patch(host, port, path)))
    }

    fun pause(duration: Duration) {
        steps.add(PauseStep(duration))
    }

    fun build() = Scenario(steps)
}

fun scenario(host: String,
             port: Int,
             builder: HttpDsl.() -> Unit): Scenario = HttpDsl(host, port).apply(builder).build()
