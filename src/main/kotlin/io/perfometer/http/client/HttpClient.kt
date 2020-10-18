package io.perfometer.http.client

import io.perfometer.dsl.RequestBuilder
import io.perfometer.http.HttpResponse

interface HttpClient {

    fun executeHttp(request: RequestBuilder): HttpResponse
}
