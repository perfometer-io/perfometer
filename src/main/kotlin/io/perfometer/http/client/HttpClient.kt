package io.perfometer.http.client

import io.perfometer.dsl.RequestBuilder
import io.perfometer.http.HttpResponse
import io.perfometer.http.HttpStatus

interface HttpClient {

    fun executeHttp(request: RequestBuilder, response: HttpResponse): HttpStatus
}
