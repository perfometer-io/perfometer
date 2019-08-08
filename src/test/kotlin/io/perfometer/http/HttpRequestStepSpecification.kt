package io.perfometer.http

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import kotlin.test.Test

@Suppress("FunctionName")
class HttpRequestStepSpecification {

    @Test
    fun `should return false when comparing requests of different type`() {
        val get = Get("perfomerter.io", 443, "/")
        val post = Post("perfomerter.io", 443, "/")

        get shouldNotBe post
    }

    @Test
    fun `should return true when comparing two different objects of the same logical requests`() {
        val get = Get("perfomerter.io", 443, "/")
        val sameGet = Get("perfomerter.io", 443, "/")

        get shouldBe sameGet
    }

    @Test
    fun `name should return uppercased HTTP Verb corresponding to the request class`() {
        val getName = Get("perfometer.io", 443, "/").name
        getName shouldBe "GET"
    }
}
