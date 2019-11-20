package io.perfometer.dsl

import io.perfometer.http.*
import java.time.Duration
import java.util.*

typealias HttpHeader = Pair<String, String>
typealias HttpParam = Pair<String, String>

class RequestBuilder(val protocol : String,
                     val host : String,
                     val port : Int,
                     val method : HttpMethod,
                     val authorization : HttpHeader?) {
    private var path : () -> String = { "" }
    private val headers : MutableList<() -> HttpHeader> = mutableListOf()
    private val params : MutableList<() -> HttpParam> = mutableListOf()

    val response = HttpResponse()
    var body : () -> ByteArray = { ByteArray(0) }

    fun path(path : () -> String) : RequestBuilder {
        this.path = path
        return this
    }

    fun body(body : () -> ByteArray) : RequestBuilder {
        this.body = body
        return this
    }

    fun header(header : () -> HttpHeader) : RequestBuilder {
        headers.add(header)
        return this
    }

    fun param(param : () -> HttpParam) : RequestBuilder {
        params.add(param)
        return this
    }

    fun pathWithParams() : String {
        return path() + paramsToString()
    }

    private fun paramsToString() : String {
        return if (this.params.isNotEmpty())
            this.params.map { it() }.joinToString("&", "?") { "${it.first}=${it.second}" }
        else ""
    }

    fun headers() : Map<String, String> {
        return this.headers.map { it() }
                .groupBy({ it.first }, { it.second })
                .mapValues { it.value.joinToString(",") }
    }
}

class HttpDsl(private val protocol : String,
              private val host : String,
              private val port : Int) {
    private val steps : MutableList<Step> = mutableListOf()

    private var authorizationHeader : HttpHeader? = null

    private fun request(httpMethod : HttpMethod, authorization: HttpHeader?) : RequestBuilder {
        val request = RequestBuilder(protocol, host, port, httpMethod, authorization)
        steps.add(RequestStep(request, request.response))
        return request
    }

    fun get() = request(HttpMethod.GET, authorizationHeader)
    fun post() = request(HttpMethod.POST, authorizationHeader)
    fun put() = request(HttpMethod.PUT, authorizationHeader)
    fun delete() = request(HttpMethod.DELETE, authorizationHeader)
    fun patch() = request(HttpMethod.PATCH, authorizationHeader)

    fun basicAuth(user: String, password: String) {
        val credentialsEncoded = Base64.getEncoder().encodeToString("$user:$password".toByteArray())
        this.authorizationHeader = HttpHeader("Authorization", "Basic $credentialsEncoded")
    }

    fun pause(duration : Duration) {
        steps.add(PauseStep(duration))
    }

    fun build() = Scenario(steps)
}

fun scenario(protocol : String,
             host : String,
             port : Int,
             builder : HttpDsl.() -> Unit) : Scenario = HttpDsl(protocol, host, port).apply(builder).build()
