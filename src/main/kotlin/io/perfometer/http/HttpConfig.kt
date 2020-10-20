package io.perfometer.http

data class HttpConfig(
        val protocol: String,
        val host: String,
        val port: Int
)

fun remote(
        host: String,
        protocol: String = "https",
        port: Int = 443,
) = HttpConfig(
        protocol = protocol,
        host = host,
        port = port,
)

fun localhost(
        protocol: String = "http",
        port: Int = 80,
) = HttpConfig(
        protocol = protocol,
        host = "localhost",
        port = port,
)
