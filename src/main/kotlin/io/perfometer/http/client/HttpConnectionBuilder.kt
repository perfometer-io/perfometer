package io.perfometer.http.client

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

internal class HttpConnectionBuilder(
    url: URL,
    path: String,
) {

    val connection = URL(url, path).openConnection() as HttpURLConnection

    fun trustAllCertificates() {
        if (connection is HttpsURLConnection) {
            connection.sslSocketFactory = TrustAllSslSocketFactoryProvider.trustAllSslSocketFactory
        }
    }

    fun method(method: String) {
        this.connection.requestMethod = method
    }

    fun headers(headers: Map<String, List<String>>) {
        headers
            .flatMap { entry -> entry.value.map { Pair(entry.key, it) } }
            .forEach { (name, value) -> this.connection.addRequestProperty(name, value) }
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

internal fun httpConnection(
    baseUrl: URL,
    path: String,
    builder: HttpConnectionBuilder.() -> Unit
): HttpURLConnection = HttpConnectionBuilder(baseUrl, path).apply(builder).connection

