package io.perfometer.http

import java.util.*

data class HttpStatus(val code: Int) {

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

sealed class HttpRequest(val host: String,
                         val port: Int,
                         val path: String,
                         val headers: Map<String, String>,
                         val body: ByteArray) {

    override fun equals(other: Any?): Boolean {
        return other is HttpRequest
                && other::class == this::class
                && host == other.host
                && port == other.port
                && path == other.path
                && headers == other.headers && body.contentEquals(other.body)
    }

    override fun hashCode(): Int {
        var result = host.hashCode()
        result = 31 * result + port
        result = 31 * result + path.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "HttpRequest(host='$host', port=$port, path='$path', headers=$headers, body=${Arrays.toString(body)})"
    }

    val name: String
        get() = this::class.java.simpleName.toUpperCase()
}

class Get(host: String, port: Int, path: String, headers: Map<String, String> = emptyMap(), body: ByteArray = ByteArray(0))
    : HttpRequest(host, port, path, headers, body)

class Post(host: String, port: Int, path: String, headers: Map<String, String> = emptyMap(), body: ByteArray = ByteArray(0))
    : HttpRequest(host, port, path, headers, body)

class Put(host: String, port: Int, path: String, headers: Map<String, String> = emptyMap(), body: ByteArray = ByteArray(0))
    : HttpRequest(host, port, path, headers, body)

class Delete(host: String, port: Int, path: String, headers: Map<String, String> = emptyMap(), body: ByteArray = ByteArray(0))
    : HttpRequest(host, port, path, headers, body)

class Patch(host: String, port: Int, path: String, headers: Map<String, String> = emptyMap(), body: ByteArray = ByteArray(0))
    : HttpRequest(host, port, path, headers, body)
