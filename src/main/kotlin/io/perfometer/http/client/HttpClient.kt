package io.perfometer.http.client

import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpResponse

typealias HttpClientFactory = () -> HttpClient

interface HttpClient {

    suspend fun executeHttp(request: HttpRequest): HttpResponse
}
