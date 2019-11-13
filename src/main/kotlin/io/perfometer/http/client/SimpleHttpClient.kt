package io.perfometer.http.client

import io.perfometer.dsl.RequestBuilder
import io.perfometer.http.HttpResponse
import io.perfometer.http.HttpStatus
import java.net.HttpURLConnection

class SimpleHttpClient(private val trustAllCertificates: Boolean) : HttpClient {

    override fun executeHttp(request: RequestBuilder, response: HttpResponse): HttpStatus {
        val connection = createHttpConnectionForRequest(request)
        try {
            connection.connect()
            connection.inputStream.bufferedReader().use {
                response.body = it.readText()
            }
        } catch (ignored: Exception) {
            // NOOP for now
        } finally {
            connection.disconnect()
            val httpStatus = HttpStatus(connection.responseCode)
            response.status = httpStatus
            println(response)
            return httpStatus
        }
    }

    private fun createHttpConnectionForRequest(request: RequestBuilder): HttpURLConnection {
        val path = request.pathWithParams()
        println(path)
        return httpConnection("https", request.host, request.port, path) {
            if (trustAllCertificates) {
                trustAllCertificates()
            }
            method(request.method.name)
            headers(request.headers())
            body(request.body())
        }
    }
}
