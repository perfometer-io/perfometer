package io.perfometer.http.client

import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpStatus
import java.net.HttpURLConnection

class SimpleHttpClient(private val trustAllCertificates: Boolean) : HttpClient {

    override fun executeHttp(request: HttpRequest): HttpStatus {
        val connection = createHttpConnectionForRequest(request)
        try {
            connection.connect()
            connection.inputStream.bufferedReader().use { it.readText() }
        } catch (ignored: Exception) {
            // NOOP for now
        } finally {
            connection.disconnect()
            return HttpStatus(connection.responseCode)
        }
    }

    private fun createHttpConnectionForRequest(request: HttpRequest): HttpURLConnection {
        return httpConnection("https", request.host, request.port, request.path) {
            if (trustAllCertificates) {
                trustAllCertificates()
            }
            method(request.name)
            headers(request.headers)
            body(request.body)
        }
    }

}
