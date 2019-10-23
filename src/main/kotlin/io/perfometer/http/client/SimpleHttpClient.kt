package io.perfometer.http.client

import io.perfometer.http.HttpStatus
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection

class SimpleHttpClient : HttpClient {

    override fun executeHttp(connection: HttpURLConnection): HttpStatus {
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
}
