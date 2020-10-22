package io.perfometer.dsl

import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpResponse
import java.net.URL

class RequestBuilder(
        val url: URL,
        val method: HttpMethod,
        initialHeaders: List<() -> HttpHeader>
) {
    private var path: () -> String = { "" }
    private val headers: MutableList<() -> HttpHeader> = initialHeaders.toMutableList()
    private val params: MutableList<() -> HttpParam> = mutableListOf()

    var body: () -> ByteArray = { ByteArray(0) }
        private set

    var consumer: (HttpResponse) -> Unit = {}
        private set

    fun path(path: () -> String): RequestBuilder {
        this.path = path
        return this
    }

    fun body(body: () -> ByteArray): RequestBuilder {
        this.body = body
        return this
    }

    fun header(header: () -> HttpHeader): RequestBuilder {
        headers.add(header)
        return this
    }

    fun param(param: () -> HttpParam): RequestBuilder {
        params.add(param)
        return this
    }

    fun consume(consumer: (HttpResponse) -> Unit): RequestBuilder {
        this.consumer = consumer
        return this
    }

    fun pathWithParams(): String {
        return path() + paramsToString()
    }

    private fun paramsToString(): String {
        return if (this.params.isNotEmpty())
            this.params.map { it() }.joinToString("&", "?") { "${it.first}=${it.second}" }
        else ""
    }

    fun headers(): Map<String, String> {
        return this.headers.map { it() }
                .groupBy({ it.first }, { it.second })
                .mapValues { it.value.joinToString(",") }
    }
}
