package io.perfometer.http.client

import io.perfometer.http.HttpHeaders
import io.perfometer.http.HttpRequest
import io.perfometer.http.HttpResponse
import io.perfometer.http.HttpStatus
import java.net.CookieManager
import java.net.HttpCookie
import java.net.HttpURLConnection

class SimpleHttpClient(
    private val trustAllCertificates: Boolean,
) : HttpClient {

    private val cookieManager = CookieManager()

    override suspend fun executeHttp(request: HttpRequest): HttpResponse {
        var headers: Map<String, List<String>> = emptyMap()
        var body: ByteArray = byteArrayOf()
        var connection: HttpURLConnection? = null
        try {
            connection = createHttpConnectionForRequest(request)
            connection.connect()
            headers = connection.headerFields
            body = connection.inputStream.readBytes()

            headers[HttpHeaders.SET_COOKIE]
                ?.flatMap { HttpCookie.parse(it) }
                ?.forEach { cookieManager.cookieStore.add(request.url.toURI(), it) }

        } catch (ignored: Exception) {
            ignored.printStackTrace()
        } finally {
            connection?.disconnect()
            val httpStatus = HttpStatus(connection?.responseCode ?: -1)
            return HttpResponse(httpStatus, headers, body)
        }
    }

    private fun createHttpConnectionForRequest(request: HttpRequest): HttpURLConnection {
        val headers = request.headers.toMutableMap()
        headers.merge(
            HttpHeaders.COOKIE,
            listOf(cookieManager.cookieStore.cookies.joinToString(";"))) { l1, l2 -> l1 + l2 }
        return httpConnection(request.url, request.pathWithParams) {
            if (trustAllCertificates) {
                trustAllCertificates()
            }
            method(request.method.name)
            headers(headers)
            body(request.body)
        }
    }
}
