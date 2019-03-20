package io.perfometer.dsl

import io.perfometer.http.*

class HttpDsl(val host: String, val port: Int) {

    private val requests: MutableList<HttpRequest> = mutableListOf()

    fun get(path: String) {
        requests.add(Get(host, port, path))
    }

    fun post(path: String, body: ByteArray) {
        requests.add(Post(host, port, path, body = body))
    }

    fun put(path: String, body: ByteArray) {
        requests.add(Put(host, port, path, body = body))
    }

    fun delete(path: String) {
        requests.add(Delete(host, port, path))
    }

    fun patch(path: String) {
        requests.add(Patch(host, port, path))
    }

    fun build() = Scenario(requests.toTypedArray())
}

fun scenario(host: String, port: Int, builder : HttpDsl.() -> Unit): Scenario
        = HttpDsl(host, port).apply(builder).build()
