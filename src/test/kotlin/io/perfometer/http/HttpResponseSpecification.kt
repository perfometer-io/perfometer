package io.perfometer.http

import io.kotest.matchers.shouldBe
import kotlin.test.Test

@Suppress("FunctionName")
class HttpResponseSpecification {

    @Test
    fun `should return body as string using charset from Content-Type header`() {
        val iso88592textBytes = byteArrayOf(0xb3.toByte(), 0xb1.toByte(), 0xb6.toByte(), 0x0a)
        val response = HttpResponse(HttpStatus(200),
                                    mapOf("Content-Type" to "text/plain; charset=iso-8859-2"),
                                    iso88592textBytes)

        response.asString() shouldBe "łąś\n"
    }
}
