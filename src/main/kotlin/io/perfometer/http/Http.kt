package io.perfometer.http

import java.net.URL
import java.nio.charset.Charset

enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
}

data class HttpStatus(
        val code: Int,
) {

    val generalError: Boolean
        get() = code == -1

    val isInformative: Boolean
        get() = isCodeIn(100..199)

    val isSuccess: Boolean
        get() = isCodeIn(200..299)

    val isRedirect: Boolean
        get() = isCodeIn(300..399)

    val isClientError: Boolean
        get() = isCodeIn(400..499)

    val isServerError: Boolean
        get() = isCodeIn(500..599)

    val isUnknown: Boolean
        get() = !isCodeIn(100..599)

    private fun isCodeIn(range: IntRange) = code in range
}

object HttpHeaders {
    const val AUTHORIZATION = "Authorization"
    const val CONTENT_TYPE = "Content-Type"
}

data class HttpRequest(val method: HttpMethod,
                       val url: URL,
                       val pathWithParams: String,
                       val headers: Map<String, String>,
                       val body: ByteArray,
                       val consumer: (HttpResponse) -> Unit)

class HttpResponse(val status: HttpStatus, val headers: Map<String, String>, val body: ByteArray = byteArrayOf()) {

    private fun charsetFromContentType(): Charset {
        return headers[HttpHeaders.CONTENT_TYPE]?.substringAfterLast("charset=")?.let {
            if (Charset.isSupported(it)) Charset.forName(it) else null
        } ?: Charsets.UTF_8
    }

    fun asString(): String = body.toString(charsetFromContentType())
}
