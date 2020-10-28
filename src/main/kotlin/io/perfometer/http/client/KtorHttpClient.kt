package io.perfometer.http.client

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpResponse
import io.perfometer.http.HttpStatus
import java.net.URL
import io.ktor.client.statement.HttpResponse as KtorHttpResponse

class KtorHttpClient : HttpClient {

    private val httpClient = HttpClient()

    override suspend fun executeHttp(request: HttpRequest): HttpResponse {
        val ktorResponse = httpClient.request<KtorHttpResponse>(
            URL(request.url, request.pathWithParams)
        ) {
            method = HttpMethod.parse(request.method.toString())
            request.headers.forEach { (name, value) -> header(name, value) }
            body = request.body
        }
        return HttpResponse(
            status = HttpStatus(ktorResponse.status.value),
            headers = ktorResponse.headers.toMap().mapValues { it.value.joinToString(",") },
            body = ktorResponse.content.toByteArray()
        )
    }
}
