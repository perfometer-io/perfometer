package io.perfometer.http

import java.util.*

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
