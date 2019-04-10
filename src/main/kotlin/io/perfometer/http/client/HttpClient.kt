package io.perfometer.http.client

import io.perfometer.http.HttpStatus
import java.net.HttpURLConnection

interface HttpClient {

    fun executeHttp(connection: HttpURLConnection): HttpStatus
}
