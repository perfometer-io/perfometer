package io.perfometer.dsl

import io.perfometer.http.*
import io.perfometer.internal.helper.toUrl
import java.net.URL
import java.time.Duration
import java.util.*

typealias HttpHeader = Pair<String, String>
typealias HttpParam = Pair<String, String>

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

class HttpDsl(
        private val baseURL: URL,
) {
    private val steps: MutableList<Step> = mutableListOf()
    val scenarioSteps: List<Step> = steps

    private val headers: MutableList<() -> HttpHeader> = mutableListOf()

    fun header(header: () -> HttpHeader) {
        headers.add(header)
    }

    private fun request(httpMethod: HttpMethod, urlString: String?): RequestBuilder {
        val requestUrl = urlString?.toUrl() ?: baseURL
        val request = RequestBuilder(requestUrl, httpMethod, headers)
        steps.add(RequestStep(request))
        return request
    }

    fun get(urlString: String? = null) = request(HttpMethod.GET, urlString)
    fun post(urlString: String? = null) = request(HttpMethod.POST, urlString)
    fun put(urlString: String? = null) = request(HttpMethod.PUT, urlString)
    fun delete(urlString: String? = null) = request(HttpMethod.DELETE, urlString)
    fun patch(urlString: String? = null) = request(HttpMethod.PATCH, urlString)

    fun basicAuth(user: String, password: String) {
        val credentialsEncoded = Base64.getEncoder().encodeToString("$user:$password".toByteArray())
        header { HttpHeaders.AUTHORIZATION to "Basic $credentialsEncoded" }
    }

    fun pause(duration: Duration) {
        steps.add(PauseStep(duration))
    }
}

class ScenarioBuilder(
        private val baseURL: URL,
        private val builder: HttpDsl.() -> Unit,
) {
    fun build(): Scenario {
        return Scenario(HttpDsl(baseURL).apply(builder).scenarioSteps)
    }
}

fun scenario(baseUrlString: String,
             builder: HttpDsl.() -> Unit): ScenarioBuilder {
    val baseUrl: URL = baseUrlString.toUrl()
    return ScenarioBuilder(baseUrl, builder)
}
