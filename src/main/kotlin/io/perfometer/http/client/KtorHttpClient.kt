package io.perfometer.http.client

import io.ktor.client.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpResponse
import io.perfometer.http.HttpStatus
import java.net.URL
import io.ktor.client.statement.HttpResponse as KtorHttpResponse

class KtorHttpClient : HttpClient {

    private val httpClient = HttpClient {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
    }

    override suspend fun executeHttp(request: HttpRequest): HttpResponse {
        val ktorResponse = httpClient.request<KtorHttpResponse>(
            URL(request.url, request.pathWithParams)
        ) {
            method = HttpMethod.parse(request.method.toString())
            headers {
                request.headers.forEach { (name, value) -> appendAll(name, value) }
            }
            body = request.body
        }
        return HttpResponse(
            status = HttpStatus(ktorResponse.status.value),
            headers = ktorResponse.headers.toMap(),
            body = ktorResponse.content.toByteArray(),
        )
    }
}
