package io.perfometer.dsl

import io.perfometer.http.HttpHeaders
import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpResponse
import io.perfometer.http.client.SimpleHttpClient
import io.perfometer.internal.helper.toUrl
import io.perfometer.runner.CoroutinesScenarioRunner
import io.perfometer.runner.ScenarioRunner
import io.perfometer.statistics.ScenarioStatistics
import java.net.URL
import java.time.Duration
import java.util.*

sealed class HttpStep
data class RequestStep(val request: HttpRequest) : HttpStep()
data class PauseStep(val duration: Duration) : HttpStep()

typealias HttpHeader = Pair<String, String>
typealias HttpParam = Pair<String, String>

class RequestDsl(
        private val url: URL,
        private val method: HttpMethod,
        initialHeaders: Map<String, String>
) {
    private var path: String = ""
    private val headers = initialHeaders.toMutableMap()
    private val params = mutableListOf<HttpParam>()
    private var body: ByteArray = ByteArray(0)
    private var consumer: (HttpResponse) -> Unit = {}

    fun path(path: String) {
        this.path = path
    }

    fun body(body: ByteArray) {
        this.body = body
    }

    fun headers(vararg headers: HttpHeader) {
        this.headers.putAll(headers)
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
            this.params.joinToString("&", "?") { "${it.first}=${it.second}" }
        else ""
    }

    fun build() = HttpRequest(method, url, pathWithParams(), headers, body, consumer)
}

class HttpDsl(
        private val baseURL: URL,
        private val scenarioRunner: ScenarioRunner,
) {
    private val headers = mutableMapOf<String, String>()

    fun headers(vararg headers: HttpHeader) {
        this.headers.putAll(headers)
    }

    private fun request(httpMethod: HttpMethod, urlString: String?, builder: RequestDsl.() -> Unit) {
        val requestUrl = urlString?.toUrl() ?: baseURL
        val request = RequestDsl(requestUrl, httpMethod, headers).apply(builder).build()
        scenarioRunner.runStep(RequestStep(request))
    }

    fun get(urlString: String? = null, builder: RequestDsl.() -> Unit)
            = request(HttpMethod.GET, urlString, builder)
    fun post(urlString: String? = null, builder: RequestDsl.() -> Unit)
            = request(HttpMethod.POST, urlString, builder)
    fun put(urlString: String? = null, builder: RequestDsl.() -> Unit)
            = request(HttpMethod.PUT, urlString, builder)
    fun delete(urlString: String? = null, builder: RequestDsl.() -> Unit)
            = request(HttpMethod.DELETE, urlString, builder)
    fun patch(urlString: String? = null, builder: RequestDsl.() -> Unit)
            = request(HttpMethod.PATCH, urlString, builder)

    fun basicAuth(user: String, password: String) {
        val credentialsEncoded = Base64.getEncoder().encodeToString("$user:$password".toByteArray())
        headers(HttpHeaders.AUTHORIZATION to "Basic $credentialsEncoded")
    }

    fun pause(duration: Duration) {
        scenarioRunner.runStep(PauseStep(duration))
    }
}

class Scenario(
        private val baseURL: URL,
        private val builder: HttpDsl.() -> Unit,
) {
    private var runner: ScenarioRunner = CoroutinesScenarioRunner(SimpleHttpClient(false))

    fun runner(runner: ScenarioRunner): Scenario {
        this.runner = runner
        return this
    }

    fun run(userCount: Int): ScenarioStatistics {
        runner.runUsers(userCount) {
            HttpDsl(baseURL, runner).apply(builder)
        }
        return runner.statistics
    }
}

fun scenario(baseUrlString: String,
             builder: HttpDsl.() -> Unit) = Scenario(baseUrlString.toUrl(), builder)
