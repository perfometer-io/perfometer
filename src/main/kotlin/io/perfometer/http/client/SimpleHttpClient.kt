package io.perfometer.http.client

import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpResponse
import io.perfometer.http.HttpStatus
import java.net.HttpURLConnection

class SimpleHttpClient(
        private val trustAllCertificates: Boolean,
) : HttpClient {

    override fun executeHttp(request: HttpRequest): HttpResponse {
        var headers: Map<String, String> = emptyMap()
        var body: ByteArray = byteArrayOf()
        var connection: HttpURLConnection? = null
        try {
            connection = createHttpConnectionForRequest(request)
            connection.connect()
            headers = connection.headerFields
                    .map { it.key to it.value.joinToString(",") }
                    .toMap()
            body = connection.inputStream.readBytes()
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        } finally {
            connection?.disconnect()
            val httpStatus = HttpStatus(connection?.responseCode ?: -1)
            return HttpResponse(httpStatus, headers, body)
        }
    }

    private fun createHttpConnectionForRequest(request: HttpRequest): HttpURLConnection {
        return httpConnection(request.url, request.pathWithParams) {
            if (trustAllCertificates) {
                trustAllCertificates()
            }
            method(request.method.name)
            headers(request.headers)
            body(request.body)
        }
    }
}
