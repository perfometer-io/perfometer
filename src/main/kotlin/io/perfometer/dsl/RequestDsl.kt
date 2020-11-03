package io.perfometer.dsl

import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpResponse
import java.net.URL
import java.net.URLEncoder

class RequestDsl(
    private val url: URL,
    private val method: HttpMethod,
    private val initialHeaders: Map<String, List<String>>
) {
    private var name: String? = null
    private var path: String = ""
    private val headers = mutableMapOf<String, List<String>>()
    private val params = mutableListOf<HttpParam>()
    private var body: ByteArray = ByteArray(0)
    private var consumer: (HttpResponse) -> Unit = {}

    fun name(name: String) {
        this.name = name
    }

    fun path(path: String) {
        this.path = path
    }

    fun body(body: ByteArray) {
        this.body = body
    }

    fun headers(vararg headers: HttpHeader) {
        headers.forEach {
            this.headers.merge(it.first, listOf(it.second)) { l1, l2 -> l1 + l2 }
        }
    }

    fun params(vararg params: HttpParam) {
        this.params.addAll(params)
    }

    fun consume(consumer: (HttpResponse) -> Unit) {
        this.consumer = consumer
    }

    private fun pathWithParams(): String {
        return path + paramsToString()
    }

    private fun paramsToString(): String {
        return if (this.params.isNotEmpty())
            this.params.joinToString("&", "?") { "${encode(it.first)}=${encode(it.second)}" }
        else ""
    }

    private fun encode(s: String): String = URLEncoder.encode(s, Charsets.UTF_8.toString())

    private fun headers(): Map<String, List<String>> = initialHeaders + headers

    fun build() =
        HttpRequest(name ?: "$method $path", method, url, pathWithParams(), headers(), body, consumer)
}
