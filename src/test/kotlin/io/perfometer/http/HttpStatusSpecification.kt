package io.perfometer.http

import io.kotlintest.shouldBe
import kotlin.test.Test

@Suppress("FunctionName")
class HttpStatusSpecification {

    @Test
    fun `should return true for codes in 1xx range, false for others`() {
        setOf(100, 101, 110, 111)
                .all { HttpStatus(it).isInformative } shouldBe true
        HttpStatus(150).isInformative shouldBe true

        HttpStatus(99).isInformative shouldBe false
        HttpStatus(201).isInformative shouldBe false
    }

    @Test
    fun `should return true for codes in 2xx range, false for others`() {
        setOf(200, 201, 202, 203, 204, 205, 206)
                .all { HttpStatus(it).isSuccess } shouldBe true
        HttpStatus(250).isSuccess shouldBe true

        HttpStatus(199).isSuccess shouldBe false
        HttpStatus(301).isSuccess shouldBe false
    }

    @Test
    fun `should return true for codes in 3xx range, false for others`() {
        setOf(300, 301, 302, 303, 304, 305, 306, 307, 310)
                .all { HttpStatus(it).isRedirect } shouldBe true
        HttpStatus(350).isRedirect shouldBe true

        HttpStatus(299).isRedirect shouldBe false
        HttpStatus(401).isRedirect shouldBe false
    }

    @Test
    fun `should return true for codes in 4xx range, false for others`() {
        setOf(400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413, 414, 415, 416, 417, 418, 451)
                .all { HttpStatus(it).isClientError } shouldBe true
        HttpStatus(450).isClientError shouldBe true

        HttpStatus(399).isClientError shouldBe false
        HttpStatus(501).isClientError shouldBe false
    }

    @Test
    fun `should return true for codes in 5xx range, false for others`() {
        setOf(500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511)
                .all { HttpStatus(it).isServerError } shouldBe true
        HttpStatus(550).isServerError shouldBe true

        HttpStatus(499).isServerError shouldBe false
        HttpStatus(601).isServerError shouldBe false
    }

    @Test
    fun `should return true for codes outside 100-599 range, false for others`() {
        HttpStatus(1).isUnknown shouldBe true
        HttpStatus(99).isUnknown shouldBe true
        HttpStatus(600).isUnknown shouldBe true
        (100..599).all { HttpStatus(it).isUnknown } shouldBe false
    }
}
