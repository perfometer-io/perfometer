package io.perfometer.http.client

import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpResponse

interface HttpClient {

    fun executeHttp(request: HttpRequest): HttpResponse
}
