package io.perfometer.http.client

import io.perfometer.dsl.HttpHeader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object TrustAllSslSocketFactoryProvider {

    val trustAllSslSocketFactory: SSLSocketFactory

    init {
        val dummyTrustManager = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate>? = null
            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, dummyTrustManager, SecureRandom())
        trustAllSslSocketFactory = sslContext.socketFactory
    }
}

internal class HttpConnectionBuilder(protocol: String,
                                     host: String,
                                     port: Int,
                                     path: String) {

    val connection = URL("$protocol://$host:$port$path").openConnection() as HttpURLConnection

    fun trustAllCertificates() {
        if (connection is HttpsURLConnection) {
            connection.sslSocketFactory = TrustAllSslSocketFactoryProvider.trustAllSslSocketFactory
        }
    }

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

    fun authorization(header: HttpHeader?) {
        if (header != null) {
            this.connection.setRequestProperty(header.first, header.second)
        }
    }
}

internal fun httpConnection(protocol: String,
                            host: String,
                            port: Int,
                            path: String,
                            builder: HttpConnectionBuilder.() -> Unit):
        HttpURLConnection = HttpConnectionBuilder(protocol, host, port, path).apply(builder).connection

