package io.perfometer.dsl

import io.perfometer.http.*
import io.perfometer.itnernal.helper.toUrl
import java.net.URL
import java.time.Duration
import java.util.*

typealias HttpHeader = Pair<String, String>
typealias HttpParam = Pair<String, String>

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
