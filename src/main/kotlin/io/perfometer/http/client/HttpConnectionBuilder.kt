package io.perfometer.http.client

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

internal class HttpConnectionBuilder(protocol: String,
                                     host: String,
                                     port: Int,
                                     path: String) {

    val connection = URL("$protocol://$host:$port$path").openConnection() as HttpURLConnection

    fun method(method: String) {
        this.connection.requestMethod = method
    }

    fun headers(headers: Map<String, String>) {
        headers.forEach { this.connection.setRequestProperty(it.key, it.value) }
    }

    fun body(body: ByteArray) {
        connection.doOutput = true
        if (body.isNotEmpty()) {
            val osw = OutputStreamWriter(this.connection.outputStream, Charset.forName("UTF-8"))
            osw.write(String(body, Charset.forName("UTF-8")))
            osw.flush()
        }
    }
}

internal fun httpConnection(protocol: String,
                            host: String,
                            port: Int,
                            path: String,
                            builder: HttpConnectionBuilder.() -> Unit):
        HttpURLConnection = HttpConnectionBuilder(protocol, host, port, path).apply(builder).connection
